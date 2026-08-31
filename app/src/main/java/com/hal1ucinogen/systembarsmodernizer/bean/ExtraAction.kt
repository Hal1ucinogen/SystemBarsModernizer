package com.hal1ucinogen.systembarsmodernizer.bean

import kotlinx.serialization.Serializable

@Serializable
data class ExtraAction(
    val viewId: String,
    val isGroup: Boolean = false,
    val isTop: Boolean = false,
    val isPadding: Boolean = true,
    val useSystemInsets: Boolean = false,
    val customInset: Int = -1,
    val self: Boolean = true,
    val childIndex: Int = -1,
    val isGone: Boolean = false,
    val delay: Long = 100L
)
