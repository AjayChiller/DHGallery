package com.technofreak.projetcv15.database.cachedb

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.paging.DataSource
import androidx.room.*

@Dao
interface FlickerPhotoDao {
    @Query("select * from flickerphoto")
    fun getPhotos(): DataSource.Factory<Int, FlickerPhoto>

    @Query("select * from flickerphoto")
    fun getStickers(): LiveData<List< FlickerPhoto>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAll( photos: List<FlickerPhoto>)

    @Query("DELETE FROM flickerphoto")
    fun clearPhotos()
}
/*
@Dao
interface StickerItemDao {
    @Query("select * from stickeritem")
    fun getStickers(): LiveData<List< StickerItem>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertStickers( photos: List<StickerItem>)

    @Query("DELETE FROM stickeritem")
    fun clearStickers()
}

 */



@Database(entities = [FlickerPhoto::class], version = 1)
abstract class FlickersDatabase: RoomDatabase() {
    abstract val flickerPhotoDao: FlickerPhotoDao
}

/*
@Database(entities = [StickerItem::class], version = 1)
abstract class StickerDatabase: RoomDatabase() {
    abstract val stickerItemDao: StickerItemDao
}


 */

private lateinit var INSTANCE: FlickersDatabase

private lateinit var STICKER_INSTANCE: FlickersDatabase

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
fun getStickerDatabase(context: Context): FlickersDatabase {
    synchronized(FlickersDatabase::class.java) {
        if (!::STICKER_INSTANCE.isInitialized) {
            STICKER_INSTANCE = Room.databaseBuilder(context.applicationContext,
                FlickersDatabase::class.java,
                "stickers").build()
        }
    }
    return STICKER_INSTANCE
}
/*
fun getStickerDatabase(context: Context): StickerDatabase {
    synchronized(StickerDatabase::class.java) {
        if (!::STICKER_INSTANCE.isInitialized) {
            STICKER_INSTANCE = Room.databaseBuilder(context.applicationContext,
                StickerDatabase::class.java,
                "stickers").build()
        }
    }
    return STICKER_INSTANCE
}


 */