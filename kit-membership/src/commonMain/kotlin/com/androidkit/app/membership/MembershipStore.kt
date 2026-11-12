package com.androidkit.app.membership

import okio.Path

/**
 * 会员状态的本地持久化：一个 JSON 文件（app 内部存储），三端通用（okio）。
 *
 * 目录由调用方给（Android 传 filesDir，iOS 传 Documents，Desktop 传用户目录）。
 */
class MembershipStore(rootDir: Path) {

    private val file = JsonFile(
        rootDir / FILE_NAME,
        Membership.serializer(),
        Membership.free(),
    )

    /** 读会员状态；文件不存在或读坏了都返回免费档。 */
    suspend fun load(): Membership = file.read()

    /** 写会员状态（覆盖整个文件）。 */
    suspend fun save(membership: Membership) = file.write(membership)

    private companion object {
        const val FILE_NAME = "membership.json"
    }
}
