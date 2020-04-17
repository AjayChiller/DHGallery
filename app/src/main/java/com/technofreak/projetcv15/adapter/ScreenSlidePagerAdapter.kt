package com.technofreak.projetcv15.adapter

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.technofreak.projetcv15.R
import com.technofreak.projetcv15.model.PhotoEntity
import com.technofreak.projetcv15.viewpager.ScreenSlidePagerActivity
import kotlinx.android.extensions.LayoutContainer


class ScreenSlidePagerAdapter(
    private val context: Context

) : RecyclerView.Adapter<ViewPagerImageViewHolder>() {
    private val inflater: LayoutInflater
    var  photoEntity: List<PhotoEntity> = listOf()

    fun setItem(list:List<PhotoEntity>){
        photoEntity=list
    }
    private lateinit var onClick: (PhotoEntity) -> Unit
    fun setOnClickListener(onClick: (PhotoEntity) -> Unit) {
        this.onClick = onClick
    }
    private lateinit var onClickPlay: ( String) -> Unit
    fun setOnClickListenerplay(onclick: ( String) -> Unit) {
        this.onClickPlay = onclick
    }



    init {
        inflater = LayoutInflater.from(context)
    }

    override fun getItemCount(): Int = photoEntity.size
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewPagerImageViewHolder {

        val vh = ViewPagerImageViewHolder(
            LayoutInflater.from(parent.context).inflate(
                R.layout.screen_slide_page,
                parent,
                false
            )
        )

        vh.containerView.setOnClickListener {
            val picture=photoEntity[vh.adapterPosition]
            onClick(picture)
        }
        vh.play_button.setOnClickListener {
            val photo= photoEntity[vh.adapterPosition]
            onClickPlay(photo.contentUri)
        }
        return vh
    }

    override fun onBindViewHolder(holder: ViewPagerImageViewHolder, position: Int) {

        val image = photoEntity[position]
            holder.photoEntity=image

        Glide.with(holder.imageView)
            .load(image.contentUri)
            .into(holder.imageView)
        val uri=photoEntity[position].contentUri
        val index=uri.lastIndexOf(".")
        if (index > 0 && uri.substring(index) == ".mp4" )
            holder.play_button.visibility=View.VISIBLE

    }
}

class ViewPagerImageViewHolder(
override val containerView: View) :    RecyclerView.ViewHolder(containerView), LayoutContainer {
    lateinit var photoEntity:PhotoEntity
    val imageView: ImageView = containerView.findViewById(R.id.image)
    val play_button : ImageButton = containerView.findViewById(R.id.play_button)
}

