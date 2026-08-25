package com.hal1ucinogen.systembarsmodernizer.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.hal1ucinogen.systembarsmodernizer.bean.AppConfig

@Entity(tableName = "item_table")
data class SBMItem(
    @PrimaryKey val packageName: String,
    val label: String,
    val versionName: String,
    val versionCode: Long,
    val installedTime: Long,
    val lastUpdatedTime: Long,
    val isSystem: Boolean,
    val features: Int,
    val targetApi: Short,
    val config: AppConfig? = null
) {
    fun hasConfig(): Boolean = config != null && features > 0
}
