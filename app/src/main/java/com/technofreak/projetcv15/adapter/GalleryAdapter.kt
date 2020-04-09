package com.technofreak.projetcv15.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.technofreak.projetcv15.R
import com.technofreak.projetcv15.model.PhotoEntity
import kotlinx.android.extensions.LayoutContainer

class GalleryAdapter() :          ListAdapter<PhotoEntity, ImageViewHolder>(
    PhotoEntity.DiffCallback) {
    private lateinit var onClick: (PhotoEntity, Int) -> Unit
    fun setOnClickListener(onClick: (PhotoEntity, Int) -> Unit) {
        this.onClick = onClick
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ImageViewHolder {
        val vh = ImageViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.gallery_layout, parent, false))

        vh.containerView.setOnClickListener {
            val position = vh.adapterPosition
            val picture = getItem(position)
            onClick(picture,position)
        }

        return vh
    }

    override fun onBindViewHolder(holder: ImageViewHolder, position: Int) {

        val photoEntity = getItem(position)
        holder.pos=position
        holder.rootView.tag = photoEntity
        Glide.with(holder.imageView)
            .load(photoEntity.contentUri)
            .thumbnail(0.33f)
            .centerCrop()
            .into(holder.imageView)
    }
}
/**
 * Basic [RecyclerView.ViewHolder] for our gallery.
 */
class ImageViewHolder( override val containerView: View) :    RecyclerView.ViewHolder(containerView),LayoutContainer {
    val rootView = containerView
    var pos: Int? = null
    val imageView: ImageView = containerView.findViewById(R.id.image)

}

