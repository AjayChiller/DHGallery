package com.technofreak.projetcv15.flicker.cachedb

import android.content.Context
import androidx.paging.DataSource
import androidx.room.*

@Dao
interface FlickerPhotoDao {
    @Query("select * from flickerphoto")
    fun getPhotos(): DataSource.Factory<Int, FlickerPhoto>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAll( photos: List<FlickerPhoto>)

    @Query("DELETE FROM flickerphoto")
    fun clearPhotos()
}



@Database(entities = [FlickerPhoto::class], version = 1)
abstract class FlickersDatabase: RoomDatabase() {
    abstract val flickerPhotoDao: FlickerPhotoDao
}

private lateinit var INSTANCE: FlickersDatabase

fun getDatabase(context: Context): FlickersDatabase {
    synchronized(FlickersDatabase::class.java) {
        if (!::INSTANCE.isInitialized) {
            INSTANCE = Room.databaseBuilder(context.applicationContext,
                FlickersDatabase::class.java,
                    "photos").build()
        }
    }
    return INSTANCE
}
