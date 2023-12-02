package com.hal1cinogen.systembarsmodernizer.bean

import androidx.annotation.ColorInt
import kotlinx.serialization.Serializable

@Serializable
data class PageConfig(
    val edgeToEdge: Boolean,
    val clearTranslucent: Boolean,
    @ColorInt val windowBackgroundColor: Int
)
