package com.technofreak.projetcv15.adapter

import android.graphics.Bitmap
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.technofreak.projetcv15.R
import kotlinx.android.extensions.LayoutContainer

class StickerAdapter(val sticker:List<Bitmap>) : RecyclerView.Adapter<StickerViewHolder>()  {
    private lateinit var onClick: (Bitmap) -> Unit
    fun setOnClickListener(onClick: (Bitmap) -> Unit) {
        this.onClick = onClick
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StickerViewHolder {
        val vh = StickerViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.gallery_layout, parent, false)
        )

        vh.containerView.setOnClickListener {

            onClick(vh.bitmap!!)
        }
        return vh
    }

    override fun getItemCount()=sticker.size


    override fun onBindViewHolder(holder: StickerViewHolder, position: Int) {
        Log.i("DDDD","BINFING")
           val stick = sticker[position]

            holder.bitmap=stick
            holder.imageView.setImageBitmap(stick)
    }

}

class StickerViewHolder( override val containerView: View) :    RecyclerView.ViewHolder(containerView),LayoutContainer {
    var bitmap:Bitmap?=null
    val imageView: ImageView = containerView.findViewById(R.id.image)
}



