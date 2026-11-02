package com.androidkit.app.data.storage

import android.content.ContentResolver
import android.net.Uri
import android.provider.DocumentsContract
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.io.InputStream

/**
 * SAF 存储引擎：用户在系统里选一个目录（Storage Access Framework），app 往那儿读写。
 *
 * 磁盘格式由 [codec] 决定，引擎本身跟格式无关：
 * `SafStorageEngine(resolver, rootUri)` 默认写 `.md`（frontmatter + 正文），
 * 换 `JsonCodec()` 就写 `.json`。
 *
 * @param contentResolver Used for SAF document tree access
 * @param rootUri        The user-selected directory Uri (persistent permission required)
 * @param codec          实体 ↔ 文件文本的编解码器
 */
class SafStorageEngine(
    private val contentResolver: ContentResolver,
    private val rootUri: Uri,
    private val codec: StoreCodec = MarkdownCodec()
) : EntityStore {

    override suspend fun write(subdir: String, entity: StoredEntity) = withContext(Dispatchers.IO) {
        val filename = entity.id + codec.extension
        // Reuse the existing file if present; createDocument would otherwise
        // spawn duplicates like "entityId (1).md" on every update.
        val fileUri = findFileUri(subdir, filename)
            ?: DocumentsContract.createDocument(
                contentResolver,
                ensureSubdir(subdir),
                mimeOf(codec.extension),
                filename
            )
            ?: throw IllegalStateException("Failed to create document: $filename")

        val fileContent = codec.encode(entity)
        contentResolver.openOutputStream(fileUri, "wt")?.use { os ->
            os.write(fileContent.toByteArray(Charsets.UTF_8))
        } ?: throw IllegalStateException("Failed to open output stream: $filename")
    }

    override suspend fun read(subdir: String, entityId: String): StoredEntity? =
        withContext(Dispatchers.IO) {
            val fileUri = findFileUri(subdir, entityId + codec.extension) ?: return@withContext null
            parseFile(entityId, fileUri)
        }

    override suspend fun delete(subdir: String, entityId: String): Boolean =
        deleteFile(subdir, entityId + codec.extension)

    override suspend fun deleteFile(subdir: String, filename: String): Boolean =
        withContext(Dispatchers.IO) {
            val fileUri = findFileUri(subdir, filename) ?: return@withContext false
            DocumentsContract.deleteDocument(contentResolver, fileUri)
        }

    override suspend fun listIds(subdir: String): List<String> = withContext(Dispatchers.IO) {
        queryChildren(subdir)
            .filter { it.name.endsWith(codec.extension) }
            .map { it.name.removeSuffix(codec.extension) }
    }

    override suspend fun listBinaryFilenames(subdir: String): List<String> = withContext(Dispatchers.IO) {
        queryChildren(subdir)
            .filterNot { doc -> entityExtensions.any { doc.name.endsWith(it) } }
            .map { it.name }
    }

    override suspend fun loadAll(subdir: String): List<StoredEntity> = withContext(Dispatchers.IO) {
        queryChildren(subdir)
            .filter { it.name.endsWith(codec.extension) }
            .mapNotNull { child ->
                val id = child.name.removeSuffix(codec.extension)
                parseFile(id, child.uri)
            }
    }

    override suspend fun writeBinary(subdir: String, filename: String, bytes: ByteArray, mimeType: String) =
        withContext(Dispatchers.IO) {
            val fileUri = findFileUri(subdir, filename)
                ?: DocumentsContract.createDocument(
                    contentResolver,
                    ensureSubdir(subdir),
                    mimeType,
                    filename
                )
                ?: throw IllegalStateException("Failed to create document: $filename")

            contentResolver.openOutputStream(fileUri, "wt")?.use { os ->
                os.write(bytes)
            } ?: throw IllegalStateException("Failed to open output stream: $filename")
        }

    override suspend fun readBinary(subdir: String, filename: String): ByteArray? =
        withContext(Dispatchers.IO) {
            val fileUri = findFileUri(subdir, filename) ?: return@withContext null
            runCatching {
                contentResolver.openInputStream(fileUri)?.use { readBytes(it) }
            }.getOrNull()
        }

    // ─── Internal helpers ───────────────────────────────────────────────────

    /** A child document of a subdirectory: display name + direct document Uri. */
    private data class ChildDoc(val name: String, val uri: Uri)

    /** 实体文件的后缀集合：当前 codec 的 + 旧版本遗留的 md。 */
    private val entityExtensions = setOf(codec.extension, ".md", ".markdown")

    private fun mimeOf(extension: String): String = when (extension) {
        ".md" -> "text/markdown"
        ".json" -> "application/json"
        else -> "text/plain"
    }

    /**
     * Ensures a (possibly nested, "/"-separated) subdirectory exists under the
     * root, creating missing levels. Returns the deepest directory Uri.
     */
    private fun ensureSubdir(subdir: String): Uri {
        var dirUri = rootUri
        for (segment in subdir.split('/').filter { it.isNotEmpty() }) {
            val existing = queryChildDocs(dirUri).find { it.name == segment }
            dirUri = existing?.uri
                ?: DocumentsContract.createDocument(
                    contentResolver, dirUri,
                    DocumentsContract.Document.MIME_TYPE_DIR, segment
                )
                ?: throw IllegalStateException("Failed to create subdirectory: $segment")
        }
        return dirUri
    }

    /** Finds the Uri of a subdirectory, or null. */
    private fun findSubdirUri(subdir: String): Uri? {
        var dirUri: Uri = rootUri
        for (segment in subdir.split('/').filter { it.isNotEmpty() }) {
            dirUri = queryChildDocs(dirUri).find { it.name == segment }?.uri ?: return null
        }
        return dirUri
    }

    /** Finds the Uri of a specific file in a subdirectory, or null. */
    private fun findFileUri(subdir: String, filename: String): Uri? =
        queryChildren(subdir).find { it.name == filename }?.uri

    override fun imageUri(subdir: String, filename: String): String? =
        findFileUri(subdir, filename)?.toString()

    /**
     * Queries the direct children of a subdirectory (or of the root when the
     * subdirectory is empty) in a single cursor pass.
     */
    private fun queryChildren(subdir: String): List<ChildDoc> {
        val dirUri = if (subdir.isBlank()) rootUri else findSubdirUri(subdir) ?: return emptyList()
        return queryChildDocs(dirUri)
    }

    /** Queries child documents (name + Uri) of the given directory Uri. */
    private fun queryChildDocs(dirUri: Uri): List<ChildDoc> {
        val childrenUri = DocumentsContract.buildChildDocumentsUriUsingTree(
            rootUri,
            docIdOf(dirUri)
        )
        return try {
            contentResolver.query(
                childrenUri,
                arrayOf(
                    DocumentsContract.Document.COLUMN_DOCUMENT_ID,
                    DocumentsContract.Document.COLUMN_DISPLAY_NAME
                ),
                null, null, null
            )?.use { cursor ->
                val idCol = cursor.getColumnIndexOrThrow(DocumentsContract.Document.COLUMN_DOCUMENT_ID)
                val nameCol = cursor.getColumnIndexOrThrow(DocumentsContract.Document.COLUMN_DISPLAY_NAME)
                buildList {
                    while (cursor.moveToNext()) {
                        val docId = cursor.getString(idCol)
                        val name = cursor.getString(nameCol)
                        add(ChildDoc(name, DocumentsContract.buildDocumentUriUsingTree(rootUri, docId)))
                    }
                }
            } ?: emptyList()
        } catch (_: Exception) {
            emptyList()
        }
    }

    /**
     * Document id of a directory: the tree root's id for a tree Uri, the
     * document id for a subdirectory Uri. `getDocumentId()` throws on tree
     * Uris, `getTreeDocumentId()` returns the *tree* id for every Uri in the
     * tree — which would list the root's children instead of the directory's.
     */
    private fun docIdOf(dirUri: Uri): String = try {
        DocumentsContract.getDocumentId(dirUri)
    } catch (_: IllegalArgumentException) {
        DocumentsContract.getTreeDocumentId(dirUri)
    }

    /** 读文件并按 codec 解码。 */
    private fun parseFile(id: String, uri: Uri): StoredEntity? {
        return try {
            val raw = contentResolver.openInputStream(uri)?.use { readStream(it) } ?: return null
            codec.decode(id, raw)
        } catch (_: Exception) {
            null
        }
    }

    private fun readStream(inputStream: InputStream): String {
        val buf = ByteArrayOutputStream()
        val b = ByteArray(4096)
        var n: Int
        while (inputStream.read(b).also { n = it } != -1) buf.write(b, 0, n)
        return buf.toString(Charsets.UTF_8.name())
    }

    private fun readBytes(inputStream: InputStream): ByteArray {
        val buf = ByteArrayOutputStream()
        val b = ByteArray(4096)
        var n: Int
        while (inputStream.read(b).also { n = it } != -1) buf.write(b, 0, n)
        return buf.toByteArray()
    }
}
