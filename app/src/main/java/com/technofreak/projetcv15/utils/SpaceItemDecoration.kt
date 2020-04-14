package com.technofreak.projetcv15.utils

import android.graphics.Rect

import android.view.View
import androidx.recyclerview.widget.RecyclerView

class SpaceItemDecoration(val top: Int,val bottom: Int,val right: Int,val left: Int) : RecyclerView.ItemDecoration() {

    override fun getItemOffsets(outRect: Rect, view: View,
                                parent: RecyclerView, state: RecyclerView.State) {
        outRect.left = left
        outRect.right = right
        outRect.bottom = bottom

        // Add top margin only for the top items to avoid double space between items
        if (parent.getChildLayoutPosition(view) == 0 ) {
            outRect.top = top
        } else {
            outRect.top = 0
        }
    }
}