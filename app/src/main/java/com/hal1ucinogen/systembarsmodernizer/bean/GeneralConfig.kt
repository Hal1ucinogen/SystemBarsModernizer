package com.hal1ucinogen.systembarsmodernizer.bean

import kotlinx.serialization.Serializable

@Serializable
data class GeneralConfig(
    val config: PageConfig,
    val exclusive: List<String> = emptyList()
)
