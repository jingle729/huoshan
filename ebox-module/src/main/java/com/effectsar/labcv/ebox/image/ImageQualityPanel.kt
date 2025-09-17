package com.effectsar.labcv.ebox.image

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SeekBar
import android.widget.SeekBar.OnSeekBarChangeListener
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.effectsar.labcv.ebox.R
import com.effectsar.labcv.ebox.blur.BgBlurItem
import com.volcengine.effectone.base.ComposerNode
import com.volcengine.effectone.data.IEOResourceItem
import com.volcengine.effectone.ui.BaseBottomSheetDialogFragment
import com.volcengine.effectone.widget.EOLoadingState.NETWORK_ERROR
import com.volcengine.effectone.widget.EOLoadingView
import com.volcengine.effectone.widget.nodes.EOSeekBarNodeSelectedListener
import com.volcengine.effectone.widget.nodes.EOSeekBarWithNodeView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 *Author: gaojin.ivy
 *Time: 2025/6/12 18:19
 */

class ImageQualityPanel : BaseBottomSheetDialogFragment() {

    companion object {
        const val TAG = "ImageQuality"
    }

    private val imageQualityViewModel by lazy { ImageQualityViewModel.get(requireActivity()) }

    private lateinit var seekBarWithNode: EOSeekBarWithNodeView
    private lateinit var loadingView: EOLoadingView

    override fun getFragmentTag() = TAG

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.ebox_layout_image_quality_panel, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        seekBarWithNode = view.findViewById(R.id.ebox_image_quality_seekbar)
        loadingView = view.findViewById(R.id.ebox_image_quality_loading_view)
        requireCoroutineScope().launch(Dispatchers.Main) {
            initPanel(view)
        }
        initObserver()
    }

    private suspend fun initPanel(rootView: View) {
        if (imageQualityViewModel.imageQualityItemList.isEmpty()) {
            loadingView.visibility = View.VISIBLE
            val imageQualityResourceList = withContext(Dispatchers.IO) {
                imageQualityViewModel.loadResourceList(customRequestKey).firstOrNull()?.subItems ?: emptyList()
            }
            if (imageQualityResourceList.isEmpty()) {
                loadingView.setState(NETWORK_ERROR)
                return
            }
            val imageQualityItemList = imageQualityResourceList.map {
                it.toImageQualityItem().apply {
                    state = if (imageQualityViewModel.checkResourceExist(it)) {
                        ImageQualityItem.STATE_DOWNLOADED
                    } else {
                        ImageQualityItem.STATE_REMOTE
                    }
                }
            }
            imageQualityViewModel.imageQualityItemList.add(ImageQualityItem(null, emptyList()))
            imageQualityViewModel.imageQualityItemList.addAll(imageQualityItemList)
            loadingView.visibility = View.GONE
        }

        seekBarWithNode.setOnSeekBarChangeListener(object : OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                if (fromUser) {
                    val currentComposerNode = imageQualityViewModel.currentVisibleComposerNode.value ?: return
                    val value = seekBarWithNode.getIntensity()
                    currentComposerNode.value = value
                    imageQualityViewModel.updateIntensity(currentComposerNode)
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {
            }

            override fun onStopTrackingTouch(seekBar: SeekBar) {
                val currentComposerNode = imageQualityViewModel.currentVisibleComposerNode.value ?: return
                val value = seekBarWithNode.getIntensity()
                currentComposerNode.value = value
                imageQualityViewModel.updateIntensity(currentComposerNode)
            }
        })

        val imageQualityAdapter = ImageQualityRecyclerAdapter(imageQualityViewModel.imageQualityItemList)

        rootView.findViewById<RecyclerView>(R.id.ebox_image_quality_list).apply {
            setHasFixedSize(true)
            layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
            adapter = imageQualityAdapter
            addItemDecoration(ImageQualityItemDecoration(requireContext().resources.getDimension(R.dimen.eo_editor_filter_item_spacing)))
        }

        imageQualityAdapter.selectedAction = { position, item ->
            if (item.isClear()) {
                imageQualityAdapter.selectPos(position)
                imageQualityViewModel.setCurrentVisibleItem(item)
                imageQualityViewModel.itemChanged()
            } else {
                requireCoroutineScope().launch(Dispatchers.Main) {
                    var resourceItem: IEOResourceItem? = null
                    item.resource?.let {
                        val exist = imageQualityViewModel.checkResourceExist(it)
                        if (!exist) {
                            imageQualityAdapter.updateState(position, BgBlurItem.STATE_DOWNLOADING)
                            resourceItem = withContext(Dispatchers.IO) {
                                imageQualityViewModel.loadResourceItem(it)
                            }
                        } else {
                            resourceItem = it
                        }
                    }
                    resourceItem?.let {
                        imageQualityAdapter.updateState(position, ImageQualityItem.STATE_DOWNLOADED)
                        imageQualityAdapter.selectPos(position)
                        item.open = true
                        imageQualityViewModel.itemChanged()
                        imageQualityViewModel.setCurrentVisibleItem(item)
                    } ?: run {
                        imageQualityAdapter.updateState(position, ImageQualityItem.STATE_REMOTE)
                    }
                }
            }
        }

        seekBarWithNode.setNodeSelectedListener(object : EOSeekBarNodeSelectedListener {
            override fun onNodeSelected(node: ComposerNode?) {
                imageQualityViewModel.setCurrentVisibleComposerNode(node)
            }
        })
    }

    private fun initObserver() {
        imageQualityViewModel.currentVisibleItem.observe(viewLifecycleOwner) { item ->
            if (item == null || item.isClear()) {
                seekBarWithNode.visibility = View.GONE
                imageQualityViewModel.setCurrentVisibleComposerNode(null)
            } else {
                seekBarWithNode.visibility = View.VISIBLE
                seekBarWithNode.updateState(item.composeNodeList)
            }
        }

        imageQualityViewModel.currentVisibleComposerNode.observe(viewLifecycleOwner) {
            it?.let { node ->
                val range = node.range
                if (range.size == 2) {
                    seekBarWithNode.invalidateSeekBar(range, node.value)
                }
            }
        }
    }
}