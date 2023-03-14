package com.nhd.rtlab_android_test.database

import android.content.Context

class DB(context: Context) {
    private val database = Database.getInstance(context)
    private val fileRepository = FileRepository(database)

    fun insertFile(item: FileEntity) {
        fileRepository.insert(item)
    }

    fun isFileExist(instanceID: String): Boolean {
        return fileRepository.isExists(instanceID)
    }

    fun getFileByInstanceID(instanceID: String): FileEntity? {
        return fileRepository.getByInstanceID(instanceID)
    }
}