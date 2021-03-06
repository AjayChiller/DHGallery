package com.technofreak.projetcv15.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.paging.PagedListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.technofreak.projetcv15.R
import com.technofreak.projetcv15.database.cachedb.FlickerPhoto
import kotlinx.android.extensions.LayoutContainer
import kotlinx.android.synthetic.main.flicker_image.view.*

class FlickerAdapter() :  PagedListAdapter<FlickerPhoto, ImageViewHolder3>(
    FlickerPhoto.DiffCallback) {
    private lateinit var onClick: (FlickerPhoto) -> Unit
    fun setOnClickListener(onClick: (FlickerPhoto) -> Unit) {
        this.onClick = onClick
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ImageViewHolder3 {
        val vh = ImageViewHolder3(LayoutInflater.from(parent.context).inflate(R.layout.flicker_image, parent, false))

        vh.containerView.setOnClickListener {
            val position = vh.adapterPosition
            val picture = getItem(position)
            onClick(picture!!)
        }
        return vh
    }

    override fun onBindViewHolder(holder: ImageViewHolder3, position: Int) {
        val flickerPhoto = getItem(position)
        holder.pos=position
        holder.rootView.tag = flickerPhoto
        Glide.with(holder.imageView)
            .load(flickerPhoto?.url)
            .thumbnail(0.33f)
            .centerCrop()
            .into(holder.imageView)
    }
}
/**
 * Basic [RecyclerView.ViewHolder] for our gallery.
 */
class ImageViewHolder3( override val containerView: View) :    RecyclerView.ViewHolder(containerView),LayoutContainer {
    val rootView = containerView
    var pos: Int? = null
    val imageView: ImageView = containerView.image_view

}

