package com.hal1ucinogen.systembarsmodernizer.bean

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.Serializable

@Parcelize
@Serializable
data class GeneralConfig(
    val config: PageConfig,
    val exclusive: List<String> = emptyList()
) : Parcelable
