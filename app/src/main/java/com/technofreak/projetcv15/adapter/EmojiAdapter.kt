package com.technofreak.projetcv15.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.technofreak.projetcv15.R
import kotlinx.android.extensions.LayoutContainer

class EmojiAdapter(val emoji:List<String>) : RecyclerView.Adapter<EmojiViewHolder>()  {
    private lateinit var onClick: (String) -> Unit
    fun setOnClickListener(onClick: (String) -> Unit) {
        this.onClick = onClick
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EmojiViewHolder {
        val vh = EmojiViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.emoji_holder, parent, false)
        )

        vh.containerView.setOnClickListener {
            onClick(vh.emj!!)
        }
        return vh
    }

    override fun getItemCount()=emoji.size


    override fun onBindViewHolder(holder: EmojiViewHolder, position: Int) {
            val emj = emoji[position]
            holder.emj=emj
            holder.emjholder.setText(emj)
    }

}

class EmojiViewHolder( override val containerView: View) :    RecyclerView.ViewHolder(containerView),LayoutContainer {
    var emj:String?=null
    val emjholder: TextView = containerView.findViewById(R.id.txtEmoji)
}



