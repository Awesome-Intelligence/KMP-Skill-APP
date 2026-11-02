package com.androidkit.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.ClickableText
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.layout.ContentScale
import org.jetbrains.compose.resources.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.tooling.preview.Preview
import coil3.compose.AsyncImage
import com.androidkit.app.res.*
import com.androidkit.app.ui.preview.DemoData
import com.androidkit.app.ui.theme.AppTheme

/**
 * Markdown 编辑器组件
 *
 * 支持功能：
 * - 编辑/预览模式切换
 * - Markdown 格式输入（粗体、斜体、标题、列表、引用、链接）
 * - 目标/人脉链接插入
 * - Obsidian 兼容的 Markdown 格式
 */
@Composable
fun MarkdownEditor(
    content: String,
    onContentChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String = stringResource(Res.string.markdown_editor_placeholder),
    onTagClick: (() -> Unit)? = null,
    onImageClick: (() -> Unit)? = null,
    isPreviewMode: Boolean = false,
    onPreviewModeChange: ((Boolean) -> Unit)? = null,
    /** 本次会话新选、还没落盘的图片：占位符 uuid → 可直接加载的 uri（预览用） */
    pendingImageUris: Map<String, String> = emptyMap(),
    onGoalClick: ((String) -> Unit)? = null,
    onContactClick: ((String) -> Unit)? = null,
    onGoalLinkClick: (() -> Unit)? = null,
    onContactLinkClick: (() -> Unit)? = null,
    onGoalNavigate: ((String) -> Unit)? = null,
    onContactNavigate: ((String) -> Unit)? = null,
    /** 编辑会话标识：换了记录/x记 会变，用于重置撤销历史 */
    sessionKey: String? = null,
    /** 是否为当前可见的编辑器：只有可见的才记录撤销历史（多个页签同时存在时避免串台） */
    isActive: Boolean = true
) {
    // External preview mode takes priority; default to local state only when no callback provided
    var localPreviewMode by remember { mutableStateOf(false) }
    val effectivePreviewMode = if (onPreviewModeChange != null) isPreviewMode else localPreviewMode
    val togglePreview: () -> Unit = if (onPreviewModeChange != null) {
        { onPreviewModeChange.invoke(!effectivePreviewMode) }
    } else {
        { localPreviewMode = !localPreviewMode }
    }
    // ponytail: 只初始化一次。用 remember(content) 会在每次输入后重建 TextFieldValue，
    // 光标被弹到文末（无法在中间编辑）。这里改为：content 变化时只在"外部改动"
    // （切换记录、撤销恢复）且与本地文本不同时才同步。
    var textFieldValue by remember {
        mutableStateOf(TextFieldValue(text = content, selection = TextRange(content.length)))
    }
    // 撤销/重做历史（每个编辑器实例独立）
    val history = remember { UndoHistory() }
    LaunchedEffect(content, sessionKey, isActive) {
        // 非当前页签的编辑器不参与历史，避免多页签互相污染
        if (!isActive) return@LaunchedEffect
        if (history.isEmpty || history.sessionKey != sessionKey) {
            // 换了记录/x记：新会话，历史清空
            val value = TextFieldValue(text = content, selection = TextRange(content.length))
            textFieldValue = value
            history.startSession(sessionKey, value)
        } else if (content != textFieldValue.text) {
            // 外部改动（目标/联系人/标签插入）：记成一步，可撤销
            val value = TextFieldValue(text = content, selection = TextRange(content.length))
            history.push(value)
            textFieldValue = value
        }
    }

    // 工具栏的格式插入必须回传内容，否则格式改动只停留在屏幕上，保存时丢失
    fun applyMarkdownSyntax(prefix: String, suffix: String) {
        val updated = insertMarkdownSyntax(textFieldValue, prefix, suffix)
        if (updated.text == textFieldValue.text) return
        history.push(updated)
        textFieldValue = updated
        onContentChange(updated.text)
    }

    Column(modifier = modifier) {
        // 编辑/预览区域
        // ponytail: 不要 heightIn(min)：键盘弹起时可用高度变小，固定下限会把编辑器挤出屏幕。
        // 编辑区自适应收缩
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f, fill = true)
                .background(
                    color = MaterialTheme.colorScheme.surface,
                    shape = RoundedCornerShape(12.dp)
                )
                .padding(12.dp)
        ) {
            if (effectivePreviewMode) {
                // 预览模式
                MarkdownPreview(
                    content = textFieldValue.text,
                    modifier = Modifier.fillMaxSize(),
                    pendingImageUris = pendingImageUris,
                    onGoalClick = onGoalNavigate ?: onGoalClick,
                    onContactClick = onContactNavigate ?: onContactClick
                )
            } else {
                // 编辑模式
                BasicTextField(
                    value = textFieldValue,
                    onValueChange = { newValue ->
                        if (newValue.text != textFieldValue.text) {
                            // 逐字输入合并成一步，其他改动（粘贴/替换）单独成步
                            history.push(newValue, typing = isTypingEdit(textFieldValue, newValue))
                        }
                        textFieldValue = newValue
                        if (newValue.text != content) {
                            onContentChange(newValue.text)
                        }
                    },
                    modifier = Modifier.fillMaxSize(),
                    textStyle = MaterialTheme.typography.bodyLarge.copy(
                        color = MaterialTheme.colorScheme.onSurface
                    ),
                    cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                    decorationBox = { innerTextField ->
                        Box {
                            if (textFieldValue.text.isEmpty()) {
                                Text(
                                    text = placeholder,
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                                )
                            }
                            innerTextField()
                        }
                    }
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // 工具栏（置底：格式插入是写作中的高频动作，单手拇指够得着；
        // 键盘弹起时由 MainActivity 统一把页面抬到键盘上沿，工具栏贴着键盘）
        MarkdownToolbar(
            canUndo = history.canUndo && !effectivePreviewMode,
            canRedo = history.canRedo && !effectivePreviewMode,
            onUndoClick = {
                history.undo { value ->
                    textFieldValue = value
                    onContentChange(value.text)
                }
            },
            onRedoClick = {
                history.redo { value ->
                    textFieldValue = value
                    onContentChange(value.text)
                }
            },
            onTagClick = onTagClick,
            onBoldClick = { applyMarkdownSyntax("**", "**") },
            onItalicClick = { applyMarkdownSyntax("*", "*") },
            onHeadingClick = { applyMarkdownSyntax("# ", "") },
            onQuoteClick = { applyMarkdownSyntax("> ", "") },
            onBulletListClick = { applyMarkdownSyntax("- ", "") },
            onNumberListClick = { applyMarkdownSyntax("1. ", "") },
            onImageClick = onImageClick,
            goalLinkClick = onGoalLinkClick,
            contactLinkClick = onContactLinkClick,
            isPreviewMode = effectivePreviewMode,
            onTogglePreview = togglePreview
        )
    }
}

/**
 * 判断这次改动是不是"敲/删了一个字"：
 * 差一个字符（插入、删除、覆盖一个字符）算逐字输入，粘贴/大段替换不算。
 */
private fun isTypingEdit(old: TextFieldValue, new: TextFieldValue): Boolean {
    val o = old.text
    val n = new.text
    val limit = minOf(o.length, n.length)
    var prefix = 0
    while (prefix < limit && o[prefix] == n[prefix]) prefix++
    var suffix = 0
    while (suffix < limit - prefix && o[o.length - 1 - suffix] == n[n.length - 1 - suffix]) suffix++
    val removed = limit - prefix - suffix
    val added = removed + (n.length - o.length)
    return removed <= 1 && added <= 1
}

/**
 * 插入 Markdown 语法
 * 如果有选中文本，则在选中文本前后添加标记；否则在光标位置插入标记
 */
private fun insertMarkdownSyntax(
    textFieldValue: TextFieldValue,
    prefix: String,
    suffix: String
): TextFieldValue {
    val text = textFieldValue.text
    val selection = textFieldValue.selection

    return if (selection.collapsed) {
        // 无选中文本，在光标位置插入标记对
        val newText = text.substring(0, selection.start) + prefix + suffix + text.substring(selection.start)
        val newCursorPos = selection.start + prefix.length
        TextFieldValue(text = newText, selection = TextRange(newCursorPos))
    } else {
        // 有选中文本，在选中文本前后添加标记
        val selectedText = text.substring(selection.start, selection.end)
        val newText = text.substring(0, selection.start) + prefix + selectedText + suffix + text.substring(selection.end)
        val newSelectionEnd = selection.end + prefix.length + suffix.length
        TextFieldValue(text = newText, selection = TextRange(selection.start, newSelectionEnd))
    }
}

/**
 * Markdown 预览组件
 * 使用 compose-markdown 渲染 Markdown 内容
 * Wiki 链接 ([[goals/xxx|名称]] / [[contacts/xxx|名称]]) 可点击跳转
 */
@Composable
fun MarkdownPreview(
    content: String,
    modifier: Modifier = Modifier,
    pendingImageUris: Map<String, String> = emptyMap(),
    onLinkClick: ((String) -> Unit)? = null,
    onGoalClick: ((String) -> Unit)? = null,
    onContactClick: ((String) -> Unit)? = null
) {
    val scrollState = rememberScrollState()
    // ponytail: compose-markdown doesn't support file:// URI — extract images and render with Coil
    val (imageBlocks, contentWithoutImages) = extractInlineImages(content, pendingImageUris)

    Box(
        modifier = modifier
            .verticalScroll(scrollState)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // Render text with clickable wiki links, then remaining markdown
            val parts = splitWikiLinksAndMarkdown(contentWithoutImages)
            parts.forEach { part ->
                when (part) {
                    is ContentPart.WikiLink -> {
                        val prefix = if (part.type == WikiLinkType.GOAL) "🏁" else "👤"
                        Row(
                            modifier = Modifier
                                .clickable {
                                    println("clickable onGoalClick called, part.id=${part.id}, onGoalClick=$onGoalClick")
                                    when (part.type) {
                                        WikiLinkType.GOAL -> onGoalClick?.invoke(part.id)
                                        WikiLinkType.CONTACT -> onContactClick?.invoke(part.id)
                                    }
                                }
                                .padding(vertical = 2.dp)
                        ) {
                            Text(
                                text = prefix,
                                style = MaterialTheme.typography.bodyLarge
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = part.displayName,
                                style = MaterialTheme.typography.bodyLarge.copy(
                                    color = MaterialTheme.colorScheme.primary,
                                    textDecoration = TextDecoration.Underline
                                )
                            )
                        }
                    }
                    is ContentPart.Markdown -> {
                        // ponytail: 公共层不带 markdown 渲染器（KMP 版依赖重、样式不可控）——
                        // 先按纯文本渲染；真要富文本再上 com.mikepenz:multiplatform-markdown-renderer。
                        Text(
                            text = part.text,
                            style = MaterialTheme.typography.bodyLarge,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
            // Render extracted images below the text
            imageBlocks.forEach { block ->
                AsyncImage(
                    model = block.uri,
                    contentDescription = null,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    contentScale = ContentScale.FillWidth
                )
            }
        }
    }
}

private enum class WikiLinkType { GOAL, CONTACT }

private data class WikiLinkPart(
    val id: String,
    val displayName: String,
    val displayText: String,
    val type: WikiLinkType
)

private sealed class ContentPart {
    data class WikiLink(val id: String, val displayName: String, val displayText: String, val type: WikiLinkType) : ContentPart()
    data class Markdown(val text: String) : ContentPart()
}

/**
 * 分割内容，返回 Wiki 链接和普通 Markdown 文本的交替片段
 * 支持 [[goals/id]] 和 [[goals/id|name]] 两种格式
 */
private fun splitWikiLinksAndMarkdown(content: String): List<ContentPart> {
    val parts = mutableListOf<ContentPart>()
    val wikiLinkRegex = """\[\[(goals/[\w-]+|contacts/[\w-]+)(?:\|([^\]]+))?\]\]""".toRegex()
    var lastEnd = 0

    wikiLinkRegex.findAll(content).forEach { match ->
        if (match.range.first > lastEnd) {
            parts.add(ContentPart.Markdown(content.substring(lastEnd, match.range.first)))
        }
        val inner = match.groupValues[1]
        val displayName = match.groupValues[2].ifEmpty { inner.substringAfter("/") }
        val type = if (inner.startsWith("goals/")) WikiLinkType.GOAL else WikiLinkType.CONTACT
        val id = inner.substringAfter("/")
        parts.add(ContentPart.WikiLink(id, displayName, displayName, type))
        lastEnd = match.range.last + 1
    }

    if (lastEnd < content.length) {
        parts.add(ContentPart.Markdown(content.substring(lastEnd)))
    }

    return parts
}

/**
 * 从 markdown 内容中提取所有 `![image](file://...)` / `![image](pending://uuid)` 格式的图片，
 * 返回 (图片块列表, 去掉图片 markdown 的纯文本)。
 *  compose-markdown 自己也会渲染图片 markdown，留着会显示两次，
 *  所以这里把图片从正文里摘干净，只用 Coil 渲染图片块。
 *  pending:// 是本次会话还没落盘的新图片，用传入的 uuid → uri 映射渲染。
 */
private data class InlineImageBlock(
    val uri: String // 提取出的 URI
)

private fun extractInlineImages(
    content: String,
    pendingUris: Map<String, String>
): Pair<List<InlineImageBlock>, String> {
    val blocks = mutableListOf<InlineImageBlock>()
    val regex = """!\[([^\]]*)\]\((file|pending)://([^)]+)\)""".toRegex()
    val text = regex.replace(content) { match ->
        val scheme = match.groupValues[2]
        val rest = match.groupValues[3]
        val uri = when (scheme) {
            "file" -> "file://$rest"
            else -> pendingUris[rest]
        }
        // pending:// 找不到对应图片（还没选完/已被移除）就保留原样
        if (uri.isNullOrEmpty()) return@replace match.value
        blocks.add(InlineImageBlock(uri))
        ""
    }
    return blocks to text
}

@Preview(showBackground = true)
@Composable
private fun MarkdownEditorPreview() {
    AppTheme {
        MarkdownEditor(
            content = DemoData.Text.MARKDOWN_EDITOR, // ponytail: demo data only
            onContentChange = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun MarkdownPreviewPreview() {
    AppTheme {
        MarkdownPreview(
            content = DemoData.Text.MARKDOWN_PREVIEW, // ponytail: demo data only
            onGoalClick = {},
            onContactClick = {}
        )
    }
}

