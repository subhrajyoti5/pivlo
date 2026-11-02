package com.subhrajyoti.pivlo.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import com.subhrajyoti.pivlo.core.storage.FileCategory
import com.subhrajyoti.pivlo.data.local.dao.FileDao
import com.subhrajyoti.pivlo.data.local.entity.FileEntity
import com.subhrajyoti.pivlo.data.local.entity.FileHistoryEntity

class Converters {
    @TypeConverter
    fun fromCategory(category: FileCategory): String = category.name

    @TypeConverter
    fun toCategory(value: String): FileCategory = try {
        FileCategory.valueOf(value)
    } catch (e: Exception) {
        FileCategory.OTHER
    }
}

@Database(
    entities = [
        FileEntity::class,
        FileHistoryEntity::class
    ],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class PivloDatabase : RoomDatabase() {

    abstract fun fileDao(): FileDao

    companion object {
        @Volatile
        private var INSTANCE: PivloDatabase? = null

        fun getDatabase(context: Context): PivloDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    PivloDatabase::class.java,
                    "pivlo_database"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}

