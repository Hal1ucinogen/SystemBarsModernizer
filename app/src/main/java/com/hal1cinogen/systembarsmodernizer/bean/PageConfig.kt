package com.hal1cinogen.systembarsmodernizer.bean

import android.graphics.Color
import androidx.annotation.ColorInt
import kotlinx.serialization.Serializable

@Serializable
data class PageConfig(
    val edgeToEdge: Boolean = false,
    val clearTranslucent: Boolean = false,
    @ColorInt val windowBackgroundColor: Int = Color.WHITE,
    @ColorInt val statusColor: Int = Color.TRANSPARENT,
    @ColorInt val navigationColor: Int = Color.TRANSPARENT
)
