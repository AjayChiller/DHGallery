package com.technofreak.projetcv15.model

import androidx.recyclerview.widget.DiffUtil
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "dhgallery")
data class PhotoEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long,
    @ColumnInfo(name="displayName")
    var displayName: String,
    @ColumnInfo(name="dateTaken")
    val dateTaken: Long?,
    @ColumnInfo(name="contentUri")
    val contentUri: String,
    @ColumnInfo(name="tags")
    var tags: String?  =null,
    @ColumnInfo(name="liked")
    var liked: Boolean=false
) {
    companion object {
        val DiffCallback = object : DiffUtil.ItemCallback<PhotoEntity>() {
            override fun areItemsTheSame(oldItem: PhotoEntity, newItem: PhotoEntity) =
                oldItem.id == newItem.id

            override fun areContentsTheSame(oldItem: PhotoEntity, newItem: PhotoEntity)=
               oldItem.liked == newItem.liked && oldItem.displayName==newItem.displayName && oldItem.tags==newItem.tags
        }
    }
}