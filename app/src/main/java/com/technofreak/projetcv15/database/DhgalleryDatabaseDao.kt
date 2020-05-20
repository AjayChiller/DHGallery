package com.technofreak.projetcv15.database

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.technofreak.projetcv15.model.PhotoEntity
@Dao
interface DhgalleryDatabaseDao{

    @Insert
    fun insert(photo: PhotoEntity)



    @Query("SELECT * from dhgallery ORDER BY dateTaken DESC")
    fun getall(): LiveData<List<PhotoEntity>>

    @Query("SELECT * from dhgallery where liked=1 ORDER BY dateTaken DESC " )
    fun getliked(): LiveData<List<PhotoEntity>>


    @Update
    fun liketoggle(photo: PhotoEntity)

    @Query("UPDATE dhgallery SET liked = :like WHERE id = :id")
      fun likeupdate(id: Long, like :Boolean)

}