package com.hal1ucinogen.systembarsmodernizer.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.hal1ucinogen.systembarsmodernizer.constant.Constants
import com.hal1ucinogen.systembarsmodernizer.database.converter.ConfigConverters
import com.hal1ucinogen.systembarsmodernizer.database.dao.SBMItemDao
import com.hal1ucinogen.systembarsmodernizer.database.entity.SBMItem

@Database(entities = [SBMItem::class], version = 3, exportSchema = false)
@TypeConverters(ConfigConverters::class)
abstract class SBMDatabase : RoomDatabase() {

    abstract fun sbmItemDao(): SBMItemDao

    companion object {
        @Volatile
        private var INSTANCE: SBMDatabase? = null

        fun getDatabase(context: Context): SBMDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    SBMDatabase::class.java,
                    Constants.RULES_DATABASE_NAME
                ).fallbackToDestructiveMigration().build()
                INSTANCE = instance
                instance
            }
        }
    }
}
