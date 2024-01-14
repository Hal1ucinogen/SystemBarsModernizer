package com.hal1ucinogen.systembarsmodernizer.bean

import kotlinx.serialization.Serializable

@Serializable
data class AppConfig(
    val packageName: String,
    val enableLog: Boolean,
    val configVersion: Int,
    val scope: Map<String, PageConfig>,
    val general: GeneralConfig? = null
)
