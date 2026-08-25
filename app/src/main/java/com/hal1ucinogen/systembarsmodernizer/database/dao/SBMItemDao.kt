package com.hal1ucinogen.systembarsmodernizer.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.hal1ucinogen.systembarsmodernizer.database.entity.SBMItem
import kotlinx.coroutines.flow.Flow

@Dao
interface SBMItemDao {

    @Query("SELECT * FROM item_table ORDER BY features DESC, label ASC")
    fun getAllItems(): Flow<List<SBMItem>>

    @Query("SELECT * FROM item_table WHERE packageName = :packageName LIMIT 1")
    fun getItemByPackageName(packageName: String): SBMItem?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertItem(item: SBMItem)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertItems(items: List<SBMItem>)

    @Update
    fun updateItem(item: SBMItem)

    @Delete
    fun deleteItem(item: SBMItem)

    @Query("DELETE FROM item_table")
    fun deleteAll()

    @Query("SELECT COUNT(*) FROM item_table")
    fun getCount(): Int

    @Query("SELECT * FROM item_table ORDER BY features DESC, label ASC")
    fun getAllItemsSync(): List<SBMItem>

    @Query("SELECT * FROM item_table WHERE features > 0 ORDER BY label ASC")
    fun getConfiguredItemsSync(): List<SBMItem>

    @Query("DELETE FROM item_table WHERE packageName = :packageName")
    fun deleteByPackageName(packageName: String): Int

    @Query("DELETE FROM item_table WHERE packageName IN (:packageNames)")
    fun deleteByPackageNames(packageNames: List<String>): Int
}
