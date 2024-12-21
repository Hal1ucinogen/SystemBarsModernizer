package com.hal1ucinogen.systembarsmodernizer.bean

import android.graphics.Color
import androidx.annotation.ColorInt
import kotlinx.serialization.Serializable

@Serializable
data class PageConfig(
    val edgeToEdge: Boolean = false,
    val clearTranslucent: Boolean = false,
    @ColorInt val windowBackgroundColor: Int? = null,
    @ColorInt val statusColor: Int = Color.TRANSPARENT,
    @ColorInt val navigationColor: Int = Color.TRANSPARENT,
    val extraActions: List<ExtraAction> = emptyList()
)
