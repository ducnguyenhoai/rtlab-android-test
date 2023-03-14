package com.nhd.rtlab_android_test.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "file_table")
data class FileEntity(
    @PrimaryKey(autoGenerate = true) val id: Int,
    val instanceID: String,
    val path: String,
)