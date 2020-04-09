package com.technofreak.projetcv15.database

import android.content.Context
import androidx.room.*
import com.technofreak.projetcv15.model.PhotoEntity
import kotlinx.coroutines.CoroutineScope


@Database(entities = [PhotoEntity::class], version = 2)
abstract class DhgalleryDatabase : RoomDatabase() {
    abstract fun dhgalleryDatabaseDao(): DhgalleryDatabaseDao
    companion object {
        @Volatile
        private var INSTANCE: DhgalleryDatabase? = null

        fun getDatabase(
            context: Context
        ): DhgalleryDatabase {
            // if the INSTANCE is not null, then return it,
            // if it is, then create the database
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    DhgalleryDatabase::class.java,
                    "dhgallery"
                )
                    // Wipes and rebuilds instead of migrating if no Migration object.
                    // Migration is not part of this codelab.
                    .fallbackToDestructiveMigration()
                    //  .addCallback(DhgalleryDatabaseCallback.(scope))
                    .build()
                INSTANCE = instance
                // return instance
                instance
            }
        }




    }
}

