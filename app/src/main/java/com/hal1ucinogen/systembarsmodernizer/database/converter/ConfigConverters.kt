package com.hal1ucinogen.systembarsmodernizer.database.converter

import androidx.room.TypeConverter
import com.hal1ucinogen.systembarsmodernizer.bean.AppConfig
import com.hal1ucinogen.systembarsmodernizer.bean.GeneralConfig
import com.hal1ucinogen.systembarsmodernizer.bean.PageConfig
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class ConfigConverters {

    @TypeConverter
    fun fromAppConfig(value: AppConfig?): String? {
        return value?.let { Json.encodeToString(it) }
    }

    @TypeConverter
    fun toAppConfig(value: String?): AppConfig? {
        return value?.let {
            runCatching { Json.decodeFromString<AppConfig>(it) }.getOrNull()
        }
    }

    @TypeConverter
    fun fromPageConfigMap(value: Map<String, PageConfig>): String {
        return Json.encodeToString(value)
    }

    @TypeConverter
    fun toPageConfigMap(value: String): Map<String, PageConfig> {
        return Json.decodeFromString(value)
    }

    @TypeConverter
    fun fromGeneralConfig(value: GeneralConfig?): String? {
        return value?.let { Json.encodeToString(it) }
    }

    @TypeConverter
    fun toGeneralConfig(value: String?): GeneralConfig? {
        return value?.let { Json.decodeFromString(it) }
    }
}
