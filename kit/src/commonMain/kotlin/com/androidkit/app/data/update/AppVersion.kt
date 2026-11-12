package com.androidkit.app.data.update

/**
 * 版本号（语义化版本，宽松解析）。
 *
 * 为什么不用字符串比大小：按字典序 `"1.10.0" < "1.9.0"`，按语义正好相反 ——
 * 版本比较必须拆成数字段逐位比。
 *
 * 支持的写法：`1.2.3`、`v1.2.3`、`1.2`、`1.2.3-beta.1`、`1.2.3+build.7`。
 * 规则按 [semver](https://semver.org/)：
 * - 数字段逐位比较，缺的位补 0（`1.2` == `1.2.0`）
 * - 同核心版本下，带 pre-release 的比正式版小（`1.2.3-beta` < `1.2.3`）
 * - pre-release 标识符按 semver 比（数字段比数值，数字 < 字母，字母按字典序）
 * - build metadata（`+` 后面）只做展示，不参与比较
 *
 * 解析不出来就返回 null（[parseOrNull]），不抛异常 —— 版本号来自网络，
 * 不能让一条脏数据把 app 打崩。
 */
data class AppVersion(
    val numbers: List<Int>,
    val preRelease: List<String> = emptyList(),
    val buildMetadata: String? = null,
) : Comparable<AppVersion> {

    init {
        require(numbers.isNotEmpty()) { "版本号至少要有一段数字" }
    }

    override fun compareTo(other: AppVersion): Int {
        // 1) 核心数字段：位数不同时短的一方补 0
        val size = maxOf(numbers.size, other.numbers.size)
        for (i in 0 until size) {
            val left = numbers.getOrElse(i) { 0 }
            val right = other.numbers.getOrElse(i) { 0 }
            if (left != right) return left.compareTo(right)
        }

        // 2) 同核心版本：正式版 > 任何 pre-release
        if (preRelease.isEmpty() && other.preRelease.isEmpty()) return 0
        if (preRelease.isEmpty()) return 1
        if (other.preRelease.isEmpty()) return -1

        // 3) pre-release 标识符逐个比
        val common = minOf(preRelease.size, other.preRelease.size)
        for (i in 0 until common) {
            val left = preRelease[i]
            val right = other.preRelease[i]
            val leftNumber = left.toIntOrNull()
            val rightNumber = right.toIntOrNull()
            val result = when {
                leftNumber != null && rightNumber != null -> leftNumber.compareTo(rightNumber)
                leftNumber != null -> -1
                rightNumber != null -> 1
                else -> left.compareTo(right)
            }
            if (result != 0) return result
        }
        return preRelease.size.compareTo(other.preRelease.size)
    }

    override fun toString(): String = buildString {
        append(numbers.joinToString("."))
        if (preRelease.isNotEmpty()) {
            append('-')
            append(preRelease.joinToString("."))
        }
        if (!buildMetadata.isNullOrEmpty()) {
            append('+')
            append(buildMetadata)
        }
    }

    companion object {
        /** 能解析就返回 [AppVersion]，否则返回 null。 */
        fun parseOrNull(raw: String): AppVersion? {
            val trimmed = raw.trim().removePrefix("v").removePrefix("V")
            if (trimmed.isEmpty()) return null

            // 先切 build metadata（`+`），再切 pre-release（`-`）——
            // 两者都可能有，顺序不能反：1.2.3-beta+001
            val withoutBuild = trimmed.substringBefore('+')
            val buildMetadata = trimmed.substringAfter('+', missingDelimiterValue = "")
                .takeIf { trimmed.contains('+') && it.isNotEmpty() }

            val corePart = withoutBuild.substringBefore('-')
            val numbers = corePart.split('.').map { it.toIntOrNull() ?: return null }
            if (numbers.isEmpty()) return null

            val hasPreRelease = withoutBuild.contains('-')
            val preRelease = if (hasPreRelease) {
                withoutBuild.substringAfter('-').split('.')
            } else {
                emptyList()
            }
            // 空标识符（`1.2.3-`、`1.2.3-alpha..2`）不是合法 semver
            if (preRelease.any { it.isEmpty() }) return null

            return AppVersion(
                numbers = numbers,
                preRelease = preRelease,
                buildMetadata = buildMetadata,
            )
        }
    }
}
