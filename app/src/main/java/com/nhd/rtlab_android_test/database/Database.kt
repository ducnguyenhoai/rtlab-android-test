package com.nhd.rtlab_android_test.database

import android.content.Context
import androidx.room.Room
import androidx.room.RoomDatabase

@androidx.room.Database(entities = [FileEntity::class], version = 1)
abstract class Database : RoomDatabase() {
    abstract fun fileDao(): FileDao

    companion object {
        private var instance: Database? = null

        @Synchronized
        fun getInstance(ctx: Context): Database {
            if (instance == null)
                instance = Room.databaseBuilder(
                    ctx.applicationContext, Database::class.java,
                    "database"
                ).build()
            return instance!!
        }
    }
}