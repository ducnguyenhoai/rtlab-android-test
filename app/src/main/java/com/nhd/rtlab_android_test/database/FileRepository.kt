package com.nhd.rtlab_android_test.database

class FileRepository(database: Database) {
    private val fileDao = database.fileDao()

    fun insert(item: FileEntity) {
        subscribeOnBackground {
            fileDao.insert(item)
        }
    }

    fun isExists(instanceID: String): Boolean {
        return fileDao.isExists(instanceID)
    }

    fun getByInstanceID(instanceID: String): FileEntity? {
        return fileDao.getByInstanceID(instanceID)
    }
}