package com.technofreak.projetcv15.adapter

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Pair
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.technofreak.projetcv15.R
import ja.burhanrashid52.photoeditor.PhotoFilter
import java.io.IOException
import java.io.InputStream


class FilterAdapter(val mPairList:List<Pair<String, PhotoFilter>>,val mFilterListener: FilterListener) :
    RecyclerView.Adapter<FilterAdapter.FilterViewHolder>() {


    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): FilterViewHolder {
        val view: View = LayoutInflater.from(parent.context)
            .inflate(R.layout.filter_view, parent, false)
        return FilterViewHolder(view)
    }

    override fun onBindViewHolder(
        holder: FilterViewHolder,
        position: Int
    ) {
        val filterPair = mPairList[position]
        val fromAsset = getBitmapFromAsset(holder.itemView.getContext(), filterPair.first)
        holder.mImageFilterView.setImageBitmap(fromAsset)
        holder.mTxtFilterName.setText(filterPair.second.name.replace("_", " "))
    }

    override fun getItemCount(): Int {
        return mPairList.size
    }

    inner class FilterViewHolder(itemView: View) :
        RecyclerView.ViewHolder(itemView) {
        var mImageFilterView: ImageView
        var mTxtFilterName: TextView

        init {
            mImageFilterView = itemView.findViewById(R.id.imgFilterView)
            mTxtFilterName = itemView.findViewById(R.id.txtFilterName)
            itemView.setOnClickListener {
                mFilterListener.onFilterSelected(mPairList[layoutPosition].second)
            }
        }
    }

    private fun getBitmapFromAsset(
        context: Context,
        strName: String
    ): Bitmap? {
        val assetManager = context.assets
     //   var istr: InputStream? = null
        return try {
       //     istr = assetManager.open(strName)
            BitmapFactory.decodeStream(assetManager.open(strName))
        } catch (e: IOException) {
            e.printStackTrace()
            null
        }
    }
}


interface FilterListener {
    fun onFilterSelected(photoFilter: PhotoFilter?)
}