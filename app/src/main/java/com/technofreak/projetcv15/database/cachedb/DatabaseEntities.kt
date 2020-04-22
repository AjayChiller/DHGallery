package com.technofreak.projetcv15.database.cachedb

import androidx.recyclerview.widget.DiffUtil
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class FlickerPhoto constructor(
        @PrimaryKey
        val id : Long,
        val url: String,
        val title: String){

                companion object {
                val DiffCallback = object : DiffUtil.ItemCallback<FlickerPhoto>() {
                        override fun areItemsTheSame(oldItem: FlickerPhoto, newItem: FlickerPhoto) =
                                oldItem.id == newItem.id

                        override fun areContentsTheSame(oldItem: FlickerPhoto, newItem: FlickerPhoto)=
                                oldItem.title==newItem.title && oldItem.url==newItem.url
                }
        }
}


