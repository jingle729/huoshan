package com.effectsar.labcv.ebox.image

import android.annotation.SuppressLint
import android.graphics.Typeface
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.effectsar.labcv.ebox.R
import com.effectsar.labcv.ebox.image.ImageQualityRecyclerAdapter.ImageQualityItemHolder
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

class ImageQualityRecyclerAdapter(
    private val data: List<ImageQualityItem>
) : RecyclerView.Adapter<ImageQualityItemHolder>() {


    private var selectedPos = data.indexOfFirst { it.selected }
    var selectedAction: ((position: Int, item: ImageQualityItem) -> Unit)? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ImageQualityItemHolder {
        return ImageQualityItemHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.one_base_layout_filter_item, parent, false)
        )
    }

    override fun onBindViewHolder(holder: ImageQualityItemHolder, position: Int) {
        holder.bind(data[position])
    }

    override fun getItemCount() = data.size

    @SuppressLint("NotifyDataSetChanged")
    fun selectPos(position: Int) {
        val oldSelect = selectedPos
        selectedPos = position

        data.getOrNull(oldSelect)?.selected = false
        data.getOrNull(selectedPos)?.selected = true

        notifyDataSetChanged()
    }

    fun updateState(position: Int, state: Int) {
        if (position == -1) {
            return
        }
        data.getOrNull(position)?.state = state
        notifyItemChanged(position)
    }

    inner class ImageQualityItemHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        private val imageView: ShapeableImageView = itemView.findViewById(R.id.one_editor_filter_icon_view)
        private val itemSelectedView: ImageView = itemView.findViewById(R.id.eo_base_filter_icon_selected_view)
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

        fun bind(item: ImageQualityItem) {
            if (item.isClear()) {
                bindNone(item)
            } else {
                bindNormalFilter(item)
            }
        }

        private fun bindNone(item: ImageQualityItem) {
            itemName.text = AppSingleton.instance.getString(R.string.eo_base_item_none)
            imageView.setImageResource(R.drawable.eo_base_item_none)
            imageView.scaleType = ImageView.ScaleType.CENTER_INSIDE

            setAlpha(1F)

            if (item.selected) {
                imageView.strokeColor = ContextCompat.getColorStateList(AppSingleton.instance, R.color.Primary)
                itemName.typeface = Typeface.DEFAULT_BOLD
                itemName.setTextColor(ContextCompat.getColor(AppSingleton.instance, R.color.Primary))
                itemSelectedView.visibility = View.VISIBLE
            } else {
                imageView.strokeColor = null
                itemName.typeface = Typeface.DEFAULT
                itemName.setTextColor(ContextCompat.getColor(AppSingleton.instance, R.color.ConstTextInverse5))
                itemSelectedView.visibility = View.INVISIBLE
            }
            downloadView.visibility = View.GONE
        }

        private fun bindNormalFilter(item: ImageQualityItem) {
            val itemSize = itemView.context.resources.getDimension(R.dimen.eo_editor_image_quality_custom_item_size)
            imageView.scaleType = ImageView.ScaleType.CENTER_INSIDE
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

            if (selectedPos == 0) {
                setAlpha(0.34F)
            } else {
                setAlpha(1F)
            }

            if (item.selected) {
                imageView.strokeColor = ContextCompat.getColorStateList(AppSingleton.instance, R.color.Primary)
                itemName.typeface = Typeface.DEFAULT_BOLD
                itemName.setTextColor(ContextCompat.getColor(AppSingleton.instance, R.color.Primary))
                itemSelectedView.visibility = View.VISIBLE
            } else {
                imageView.strokeColor = null
                itemName.typeface = Typeface.DEFAULT
                itemName.setTextColor(ContextCompat.getColor(AppSingleton.instance, R.color.ConstTextInverse5))
                itemSelectedView.visibility = View.INVISIBLE
            }

            when (item.state) {

                ImageQualityItem.STATE_REMOTE -> {
                    downloadView.visibility = View.VISIBLE
                    downloadView.setState(REMOTE)
                }

                ImageQualityItem.STATE_DOWNLOADING -> {
                    downloadView.visibility = View.VISIBLE
                    downloadView.setState(DOWNLOADING)
                }

                ImageQualityItem.STATE_DOWNLOADED -> {
                    downloadView.visibility = View.GONE
                }

                else -> {
                    //do nothing
                }
            }
        }


        private fun setAlpha(alpha: Float) {
            imageView.alpha = alpha
            itemSelectedView.alpha = alpha
            downloadView.alpha = alpha
            itemName.alpha = alpha
        }
    }
}