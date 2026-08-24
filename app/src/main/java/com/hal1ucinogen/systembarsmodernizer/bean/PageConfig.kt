package com.hal1ucinogen.systembarsmodernizer.bean

import android.graphics.Color
import android.os.Parcelable
import androidx.annotation.ColorInt
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.Serializable

@Parcelize
@Serializable
data class PageConfig(
    val edgeToEdge: Boolean = false,
    val clearTranslucent: Boolean = false,
    @ColorInt val windowBackgroundColor: Int? = null,
    @ColorInt val statusColor: Int = Color.TRANSPARENT,
    @ColorInt val navigationColor: Int = Color.TRANSPARENT,
    val extraActions: List<ExtraAction> = emptyList(),
    val uiModeWBC: Pair<Int, Int>? = null
) : Parcelable
