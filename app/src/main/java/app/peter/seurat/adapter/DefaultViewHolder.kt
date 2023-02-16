package app.peter.seurat.adapter

import android.view.View
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import app.peter.seurat.R

class DefaultViewHolder(private val view: View): RecyclerView.ViewHolder(view) {

    fun bind(value: String) {
        val textItem: TextView = view.findViewById(R.id.text_item)
        textItem.text = value
    }
}