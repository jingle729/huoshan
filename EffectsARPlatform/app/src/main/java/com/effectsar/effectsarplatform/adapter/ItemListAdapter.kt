package com.effectsar.effectsarplatform.adapter

import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.effectsar.platform.struct.Material
import com.effectsar.effectsarplatform.R
import com.effectsar.effectsarplatform.utils.inflate

class ItemListAdapter(
    private val itemList: List<Material>
) : RecyclerView.Adapter<ItemListAdapter.ItemListViewHolder>() {

    var clickAction: ((Int, Material) -> Unit)? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemListViewHolder {
        return ItemListViewHolder(parent.inflate(R.layout.layout_list_item))
    }

    override fun onBindViewHolder(holder: ItemListViewHolder, position: Int) {
        holder.bindData(itemList[position])
    }

    override fun getItemCount() = itemList.size

    inner class ItemListViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val content: TextView = itemView.findViewById(R.id.item_content)

        init {
            itemView.setOnClickListener {
                val position = absoluteAdapterPosition
                if (position in itemList.indices) {
                    clickAction?.invoke(position, itemList[position])
                }
            }
        }

        fun bindData(material: Material) {
            content.text = material.title
        }
    }
}