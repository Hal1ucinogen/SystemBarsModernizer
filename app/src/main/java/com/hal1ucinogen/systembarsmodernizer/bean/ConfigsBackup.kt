package com.hal1ucinogen.systembarsmodernizer.bean

import kotlinx.serialization.Serializable

@Serializable
data class ConfigsBackup(
    val version: Int = 1,
    val timestamp: Long = System.currentTimeMillis(),
    val configs: List<AppConfig>
)

typealias RulesBackup = ConfigsBackup
