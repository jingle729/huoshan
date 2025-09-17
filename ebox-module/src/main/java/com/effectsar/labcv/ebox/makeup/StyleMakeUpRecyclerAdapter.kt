package com.effectsar.labcv.ebox.makeup

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.effectsar.labcv.ebox.R
import com.effectsar.labcv.ebox.makeup.StyleMakeUpRecyclerAdapter.StyleMakeUpViewHolder
import com.google.android.material.imageview.ShapeableImageView
import com.volcengine.effectone.EffectOneSdk
import com.volcengine.effectone.image.ImageOption
import com.volcengine.effectone.widget.DownloadView
import com.volcengine.effectone.widget.DownloadView.DownloadState.DOWNLOADING
import com.volcengine.effectone.widget.DownloadView.DownloadState.REMOTE

/**
 *Author: gaojin
 *Time: 2023/11/2 17:37
 */

class StyleMakeUpRecyclerAdapter(private val data: List<StyleMakeUpItem>) : RecyclerView.Adapter<StyleMakeUpViewHolder>() {

    private var selectedPos = data.indexOfFirst { it.selected }
    var selectedAction: ((position: Int, item: StyleMakeUpItem) -> Unit)? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StyleMakeUpViewHolder {
        return StyleMakeUpViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.ebox_layout_style_makeup_item, parent, false)
        )
    }

    override fun onBindViewHolder(holder: StyleMakeUpViewHolder, position: Int) {
        holder.bind(data[position])
    }

    override fun getItemCount() = data.size

    fun selectPos(position: Int) {
        val oldSelect = selectedPos
        selectedPos = position

        data.getOrNull(oldSelect)?.selected = false
        data.getOrNull(selectedPos)?.selected = true

        notifyItemChanged(oldSelect)
        notifyItemChanged(selectedPos)
    }

    fun cancelPos() {
        data.getOrNull(selectedPos)?.selected = false
        notifyItemChanged(selectedPos)
        selectedPos = -1
    }

    fun updateState(position: Int, state: Int) {
        if (position == -1) {
            return
        }
        data.getOrNull(position)?.state = state
        notifyItemChanged(position)
    }

    inner class StyleMakeUpViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        private val imageView: ShapeableImageView = itemView.findViewById(R.id.ebox_style_makeup_icon_view)
        private val borderView: ImageView = itemView.findViewById(R.id.ebox_style_makeup_border_view)
        private val downloadView: DownloadView = itemView.findViewById(R.id.ebox_style_makeup_download_view)
        private val itemName: TextView = itemView.findViewById(R.id.ebox_style_makeup_name)

        init {
            itemView.setOnClickListener {
                val position = absoluteAdapterPosition
                if (position != selectedPos) {
                    selectedAction?.invoke(position, data[position])
                }
            }
        }

        fun bind(item: StyleMakeUpItem) {
            bindNormalFilter(item)
        }

        private fun bindNormalFilter(item: StyleMakeUpItem) {
            val itemSize = itemView.context.resources.getDimension(R.dimen.eo_editor_filter_item_size)
            imageView.scaleType = ImageView.ScaleType.CENTER_CROP
            item.resource?.let {
                EffectOneSdk.imageLoader.loadImageView(
                    imageView,
                    it,
                    ImageOption.Builder()
                        .width(itemSize.toInt())
                        .height(itemSize.toInt())
                        .build()
                )
            }

            itemName.text = item.name()

            if (item.selected) {
                borderView.visibility = View.VISIBLE
            } else {
                borderView.visibility = View.INVISIBLE
            }

            when (item.state) {

                StyleMakeUpItem.STATE_REMOTE -> {
                    downloadView.visibility = View.VISIBLE
                    downloadView.setState(REMOTE)
                }

                StyleMakeUpItem.STATE_DOWNLOADING -> {
                    downloadView.visibility = View.VISIBLE
                    downloadView.setState(DOWNLOADING)
                }

                StyleMakeUpItem.STATE_DOWNLOADED -> {
                    downloadView.visibility = View.GONE
                }

                else -> {
                    //do nothing
                }
            }
        }
    }
}