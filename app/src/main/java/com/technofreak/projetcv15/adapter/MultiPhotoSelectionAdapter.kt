package com.technofreak.projetcv15.adapter

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Pair
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.technofreak.projetcv15.R
import ja.burhanrashid52.photoeditor.PhotoFilter
import java.io.IOException
import java.io.InputStream


class MultiPhotoSelectionAdapter(val photos:List<Uri>, val imageSelectListner: ImageSelectListner) :
    RecyclerView.Adapter<MultiPhotoSelectionAdapter.PhotoViewHolder>() {


    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): PhotoViewHolder {
        val view: View = LayoutInflater.from(parent.context)
                .inflate(R.layout.multi_selection_image_view, parent, false)
        return PhotoViewHolder(view)
    }

    override fun onBindViewHolder(
        holder: PhotoViewHolder,
        position: Int
    ) {
        val uri = photos[position]
        Glide.with(holder.imageView)
            .load(uri)
            .thumbnail(0.33f)
            .centerCrop()
            .into(holder.imageView)
    }

    override fun getItemCount(): Int {
        return photos.size
    }

    inner class PhotoViewHolder(itemView: View) :
        RecyclerView.ViewHolder(itemView) {

           val imageView: ImageView = itemView.findViewById(R.id.image_view)
        init {
            itemView.setOnClickListener {
                imageSelectListner.onImageSelected(photos[layoutPosition])
            }
        }

        }
    }



interface ImageSelectListner {
    fun onImageSelected(selectedImage: Uri )
}