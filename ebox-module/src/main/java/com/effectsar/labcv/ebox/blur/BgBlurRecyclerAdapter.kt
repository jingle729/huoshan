package com.effectsar.labcv.ebox.blur

import android.graphics.Typeface
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.effectsar.labcv.ebox.R
import com.effectsar.labcv.ebox.blur.BgBlurRecyclerAdapter.BgBlurItemHolder
import com.google.android.material.imageview.ShapeableImageView
import com.volcengine.effectone.EffectOneSdk
import com.volcengine.effectone.image.ImageOption
import com.volcengine.effectone.singleton.AppSingleton
import com.volcengine.effectone.widget.DownloadView
import com.volcengine.effectone.widget.DownloadView.DownloadState.DOWNLOADING
import com.volcengine.effectone.widget.DownloadView.DownloadState.REMOTE

/**
 *Author: gaojin
 *Time: 2023/11/2 17:37
 */

class BgBlurRecyclerAdapter(
    private val data: List<BgBlurItem>
) : RecyclerView.Adapter<BgBlurItemHolder>() {

    private var selectedPos = data.indexOfFirst { it.selected }
    var selectedAction: ((position: Int, item: BgBlurItem) -> Unit)? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BgBlurItemHolder {
        return BgBlurItemHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.one_base_layout_filter_item, parent, false)
        )
    }

    override fun onBindViewHolder(holder: BgBlurItemHolder, position: Int) {
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

    fun clearState() {
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

    inner class BgBlurItemHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        private val imageView: ShapeableImageView = itemView.findViewById(R.id.one_editor_filter_icon_view)
        private val selectedView: ImageView = itemView.findViewById(R.id.eo_base_filter_icon_selected_view)
        private val downloadView: DownloadView = itemView.findViewById(R.id.one_editor_download_view)
        private val itemName: TextView = itemView.findViewById(R.id.one_editor_filter_name)

        init {
            itemView.setOnClickListener {
                val position = absoluteAdapterPosition
                if (position != selectedPos) {
                    selectedAction?.invoke(position, data[position])
                }
            }
        }

        fun bind(item: BgBlurItem) {
            if (item.isClear()) {
                bindNone(item)
            } else {
                bindNormalFilter(item)
            }
        }

        private fun bindNone(item: BgBlurItem) {
            itemName.text = AppSingleton.instance.getString(R.string.eo_base_item_none)
            imageView.setImageResource(R.drawable.eo_base_item_none)
            imageView.scaleType = ImageView.ScaleType.CENTER_INSIDE

            if (item.selected) {
                imageView.strokeColor = ContextCompat.getColorStateList(AppSingleton.instance, R.color.Primary)
                itemName.typeface = Typeface.DEFAULT_BOLD
                itemName.setTextColor(ContextCompat.getColor(AppSingleton.instance, R.color.Primary))
                selectedView.visibility = View.VISIBLE
            } else {
                imageView.strokeColor = null
                itemName.typeface = Typeface.DEFAULT
                itemName.setTextColor(ContextCompat.getColor(AppSingleton.instance, R.color.ConstTextInverse5))
                selectedView.visibility = View.INVISIBLE
            }
            downloadView.visibility = View.GONE
        }

        private fun bindNormalFilter(item: BgBlurItem) {
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
                imageView.strokeColor = ContextCompat.getColorStateList(AppSingleton.instance, R.color.Primary)
                itemName.typeface = Typeface.DEFAULT_BOLD
                itemName.setTextColor(ContextCompat.getColor(AppSingleton.instance, R.color.Primary))
                selectedView.visibility = View.VISIBLE
            } else {
                imageView.strokeColor = null
                itemName.typeface = Typeface.DEFAULT
                itemName.setTextColor(ContextCompat.getColor(AppSingleton.instance, R.color.ConstTextInverse5))
                selectedView.visibility = View.INVISIBLE
            }

            when (item.state) {

                BgBlurItem.STATE_REMOTE -> {
                    downloadView.visibility = View.VISIBLE
                    downloadView.setState(REMOTE)
                }

                BgBlurItem.STATE_DOWNLOADING -> {
                    downloadView.visibility = View.VISIBLE
                    downloadView.setState(DOWNLOADING)
                }

                BgBlurItem.STATE_DOWNLOADED -> {
                    downloadView.visibility = View.GONE
                }

                else -> {
                    //do nothing
                }
            }
        }
    }
}