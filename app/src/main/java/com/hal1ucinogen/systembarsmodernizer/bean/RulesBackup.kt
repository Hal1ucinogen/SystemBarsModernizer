package com.hal1ucinogen.systembarsmodernizer.bean

import kotlinx.serialization.Serializable

@Serializable
data class RulesBackup(
    val version: Int = 1,
    val timestamp: Long = System.currentTimeMillis(),
    val rules: List<AppConfig>
)
