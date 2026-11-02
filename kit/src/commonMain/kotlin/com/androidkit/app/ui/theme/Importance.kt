package com.androidkit.app.ui.theme

/**
 * 重要度档位 —— kit 自带的最小枚举。
 *
 * 这个枚举原本在业务层的 `data.model` 里（跟 Contact 放一起，纯属放错地方）。
 * 复制到新项目后：直接改这里的包名/文件位置，或者删掉本文件、
 * 把 [com.androidkit.app.ui.theme.Color.kt] 里的 import 指向自己的模型。
 */
enum class Importance { LOW, MEDIUM, HIGH }
