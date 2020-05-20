package com.technofreak.projetcv15.adapter

import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.paging.PagedListAdapter
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.technofreak.projetcv15.R
import com.technofreak.projetcv15.database.cachedb.FlickerPhoto
import kotlinx.android.extensions.LayoutContainer




class StickerAdapter(): ListAdapter<FlickerPhoto, StickerViewHolder>(
    FlickerPhoto.DiffCallback) {
    private lateinit var onClick: (Bitmap) -> Unit
    fun setOnClickListener(onClick: (Bitmap) -> Unit) {
        this.onClick = onClick
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StickerViewHolder {
        val vh = StickerViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.gallery_layout, parent, false)
        )

        vh.containerView.setOnClickListener {
            vh.bitmap?.let { it1 -> onClick(it1) }
        }
        return vh
    }



    override fun onBindViewHolder(holder: StickerViewHolder, position: Int) {
        val sticker = getItem(position)


        Glide.with(holder.imageView)
            .asBitmap()
            .load(sticker?.url)
            .into(object : CustomTarget<Bitmap>(){
               override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
                    holder.imageView.setImageBitmap(resource)
                    holder.bitmap=resource
                }
                override fun onLoadCleared(placeholder: Drawable?) {

                }
            })
    }

}

class StickerViewHolder( override val containerView: View) :    RecyclerView.ViewHolder(containerView),LayoutContainer {
    var bitmap:Bitmap?=null
    val imageView: ImageView = containerView.findViewById(R.id.image)
}



