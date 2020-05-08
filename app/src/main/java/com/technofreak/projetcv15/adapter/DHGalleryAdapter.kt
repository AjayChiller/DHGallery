package com.technofreak.projetcv15.adapter

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.technofreak.projetcv15.R
import com.technofreak.projetcv15.model.PhotoEntity
import com.varunest.sparkbutton.SparkButton
import com.varunest.sparkbutton.SparkEventListener
import kotlinx.android.extensions.LayoutContainer


class DHGalleryAdapter :    ListAdapter<PhotoEntity, ImageViewHolder2>(
    PhotoEntity.DiffCallback) {

    private lateinit var onClickImage: ( Int) -> Unit
    fun setOnClickListenerimage(onclick: ( Int) -> Unit) {
         this.onClickImage = onclick
    }
    private lateinit var onClickLike: (PhotoEntity) -> Unit
    fun setOnClickListenerlike(onclick: (PhotoEntity) -> Unit) {
       this.onClickLike = onclick
    }
    private lateinit var onClickPlay: ( String) -> Unit
    fun setOnClickListenerplay(onclick: ( String) -> Unit) {
        this.onClickPlay = onclick
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ImageViewHolder2 {
        val vh = ImageViewHolder2(LayoutInflater.from(parent.context).inflate(R.layout.liked_images_blog_item, parent, false))


        vh.imageView.setOnClickListener {
            val position = vh.adapterPosition
            onClickImage(position)
        }
        vh.play_button.setOnClickListener {
            val photo= getItem(vh.adapterPosition)
            onClickPlay(photo.contentUri)
        }
        return vh
    }


    override fun onBindViewHolder(holder: ImageViewHolder2, position: Int) {
        val photoEntity = getItem(position)
        holder.pos=position
        holder.rootView.tag = photoEntity
        holder.title.text=photoEntity.displayName
        holder.tags.text=photoEntity.tags

        if(photoEntity.liked)
            holder.like_button.isChecked=true
        else
            holder.like_button.isChecked=false


        holder.like_button.setEventListener(object : SparkEventListener {
            override fun onEventAnimationEnd(button: ImageView?, buttonState: Boolean) {
             if( buttonState==true) {
                 photoEntity.liked = !photoEntity.liked
                 onClickLike(photoEntity)
                 notifyDataSetChanged()
             }
            }
            override fun onEvent(button: ImageView?, buttonState: Boolean) {
                if( buttonState==false) {
                    photoEntity.liked = !photoEntity.liked
                    onClickLike(photoEntity)
                    notifyDataSetChanged()
                }
            }
            override fun onEventAnimationStart(button: ImageView?, buttonState: Boolean) {
            }
        })


        val uri=photoEntity.contentUri
        val index=uri.lastIndexOf(".")
        if (index > 0 && uri.substring(index) == ".mp4" )
            holder.play_button.visibility=View.VISIBLE
        else
            holder.play_button.visibility=View.GONE

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
class ImageViewHolder2( override val containerView: View) :    RecyclerView.ViewHolder(containerView),LayoutContainer {

    val rootView = containerView
    var pos: Int? = null
    val imageView: ImageView = containerView.findViewById(R.id.liked_image)
    val title: TextView = containerView.findViewById(R.id.liked_title)
    val tags : TextView=containerView.findViewById(R.id.liked_tags)
    val like_button:SparkButton=containerView.findViewById(R.id.liked_image_button)
    val play_button : ImageButton = containerView.findViewById(R.id.play_button)

}




