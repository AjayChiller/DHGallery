package com.technofreak.projetcv15.flicker.cachedb

import androidx.recyclerview.widget.DiffUtil
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.technofreak.projetcv15.model.PhotoEntity

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


