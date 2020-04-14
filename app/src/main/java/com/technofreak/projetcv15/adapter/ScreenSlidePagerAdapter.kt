package com.technofreak.projetcv15.adapter

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.technofreak.projetcv15.R
import com.technofreak.projetcv15.model.PhotoEntity
import com.technofreak.projetcv15.viewpager.ScreenSlidePagerActivity
import kotlinx.android.extensions.LayoutContainer


class ScreenSlidePagerAdapter(
    private val context: Context,
    private val photoEntity: List<PhotoEntity>
) : RecyclerView.Adapter<ViewPagerImageViewHolder>() {
    private val inflater: LayoutInflater
    private lateinit var onClick: (PhotoEntity,Int) -> Unit
    fun setOnClickListener(onClick: (PhotoEntity,Int) -> Unit) {
        this.onClick = onClick
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
            val position = vh.adapterPosition
            val picture=photoEntity[position]

            onClick(picture,position)


        }
        return vh
    }


    override fun onBindViewHolder(holder: ViewPagerImageViewHolder, position: Int) {

        val image = photoEntity[position]
            holder.photoEntity=image

        Glide.with(holder.imageView)
            .load(image.contentUri)
            .into(holder.imageView)
    }



    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        super.onAttachedToRecyclerView(recyclerView)
        Log.i("DDDD","onAttachedToRecyclerView")
            (context as ScreenSlidePagerActivity).setInitialPos()

        }
}

class ViewPagerImageViewHolder(
override val containerView: View) :    RecyclerView.ViewHolder(containerView), LayoutContainer {
    lateinit var photoEntity:PhotoEntity
    val imageView: ImageView = containerView.findViewById(R.id.image)

}

