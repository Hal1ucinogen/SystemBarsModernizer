package com.hal1ucinogen.systembarsmodernizer.bean

import kotlinx.serialization.Serializable

@Serializable
data class ExtraAction(
    val viewId: String,
    val isGroup: Boolean,
    val isTop: Boolean,
    val isPadding: Boolean,
    val useSystemInsets: Boolean,
    val customInset: Int = -1,
    val self: Boolean = true,
    val childIndex: Int = -1
)
