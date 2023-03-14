package com.nhd.rtlab_android_test.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface FileDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(item: FileEntity): Long

    @Query("SELECT EXISTS (SELECT id FROM file_table WHERE instanceID=:instanceID)")
    fun isExists(instanceID: String): Boolean

    @Query("SELECT * FROM file_table WHERE instanceID=:instanceID")
    fun getByInstanceID(instanceID: String): FileEntity?
}