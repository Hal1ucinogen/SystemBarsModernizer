package com.hal1cinogen.systembarsmodernizer.bean

import kotlinx.serialization.Serializable

@Serializable
data class AppConfig(
    val enableLog: Boolean,
    val configVersion: Int,
    val scope: Map<String, PageConfig>
)
