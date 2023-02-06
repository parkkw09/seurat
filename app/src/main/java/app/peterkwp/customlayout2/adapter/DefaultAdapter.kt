package app.peterkwp.customlayout2.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import app.peterkwp.customlayout2.R

class DefaultAdapter(private val list: List<String>): RecyclerView.Adapter<DefaultViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DefaultViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val view = inflater.inflate(R.layout.item_default, parent, false)
        return DefaultViewHolder(view)
    }

    override fun getItemCount(): Int {
        return list.size
    }

    override fun onBindViewHolder(holder: DefaultViewHolder, position: Int) {
        holder.bind(list[position])
    }
}