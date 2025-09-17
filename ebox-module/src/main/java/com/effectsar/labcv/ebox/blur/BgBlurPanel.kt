package com.effectsar.labcv.ebox.blur

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.SeekBar
import android.widget.SeekBar.OnSeekBarChangeListener
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.effectsar.labcv.ebox.R
import com.volcengine.effectone.base.ComposerNode
import com.volcengine.effectone.data.IEOResourceItem
import com.volcengine.effectone.ui.BaseBottomSheetDialogFragment
import com.volcengine.effectone.widget.EOLoadingState.NETWORK_ERROR
import com.volcengine.effectone.widget.EOLoadingView
import com.volcengine.effectone.widget.HorizontalItemDecoration
import com.volcengine.effectone.widget.nodes.EOSeekBarNodeSelectedListener
import com.volcengine.effectone.widget.nodes.EOSeekBarWithNodeView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 *Author: gaojin.ivy
 *Time: 2025/6/19 11:11
 */

class BgBlurPanel : BaseBottomSheetDialogFragment() {

    companion object {
        const val TAG = "BackgroundBlurPanel"
    }

    private lateinit var seekBarWithNode: EOSeekBarWithNodeView
    private lateinit var loadingView: EOLoadingView
    private lateinit var closeButton: ImageView
    private var bgBlurAdapter: BgBlurRecyclerAdapter? = null

    private val viewModel by lazy { BgBlurViewModel.get(requireActivity()) }

    override fun getFragmentTag() = TAG

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.ebox_layout_bg_blur_panel, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        seekBarWithNode = view.findViewById(R.id.ebox_bg_blur_seekbar)
        loadingView = view.findViewById(R.id.ebox_bg_blur_loading_view)
        closeButton = view.findViewById(R.id.ebox_bg_blur_close_item)
        requireCoroutineScope().launch(Dispatchers.Main) {
            initPanel(view)
        }
        initObserver()
    }

    private fun initObserver() {
        viewModel.currentVisibleItem.observe(viewLifecycleOwner) { item ->
            if (item == null || item.isClear()) {
                if (item == null) {
                    bgBlurAdapter?.clearState()
                }
                seekBarWithNode.visibility = View.GONE
                viewModel.setCurrentVisibleComposerNode(null)
                closeButton.setImageResource(R.drawable.eo_base_unselect)
            } else {
                seekBarWithNode.visibility = View.VISIBLE
                seekBarWithNode.updateState(item.composeNodeList)
                closeButton.setImageResource(R.drawable.eo_base_select)
            }
        }

        viewModel.currentVisibleComposerNode.observe(viewLifecycleOwner) {
            it?.let { node ->
                val range = node.range
                if (range.size == 2) {
                    seekBarWithNode.invalidateSeekBar(range, node.value)
                }
            }
        }
    }

    private suspend fun initPanel(rootView: View) {
        if (viewModel.itemList.isEmpty()) {
            loadingView.visibility = View.VISIBLE
            val itemList = withContext(Dispatchers.IO) {
                viewModel.loadResourceList(customRequestKey).firstOrNull()?.subItems ?: emptyList()
            }
            if (itemList.isEmpty()) {
                loadingView.setState(NETWORK_ERROR)
                return
            }
            val bgBlurItemList = itemList.map {
                it.toBackgroundBlurItem().apply {
                    state = if (viewModel.checkResourceExist(it)) {
                        BgBlurItem.STATE_DOWNLOADED
                    } else {
                        BgBlurItem.STATE_REMOTE
                    }
                }
            }
            viewModel.itemList.addAll(bgBlurItemList)
            loadingView.visibility = View.GONE
        }

        seekBarWithNode.setOnSeekBarChangeListener(object : OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                if (fromUser) {
                    val currentComposerNode = viewModel.currentVisibleComposerNode.value ?: return
                    val value = seekBarWithNode.getIntensity()
                    currentComposerNode.value = value
                    viewModel.updateIntensity(currentComposerNode)
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {
            }

            override fun onStopTrackingTouch(seekBar: SeekBar) {
                val currentComposerNode = viewModel.currentVisibleComposerNode.value ?: return
                val value = seekBarWithNode.getIntensity()
                currentComposerNode.value = value
                viewModel.updateIntensity(currentComposerNode)
            }
        })

        bgBlurAdapter = BgBlurRecyclerAdapter(viewModel.itemList)

        rootView.findViewById<RecyclerView>(R.id.ebox_bg_blur_list).apply {
            setHasFixedSize(true)
            layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
            adapter = bgBlurAdapter
            addItemDecoration(HorizontalItemDecoration(requireContext().resources.getDimension(R.dimen.eo_editor_filter_item_spacing)))
        }

        bgBlurAdapter?.selectedAction = { position, item ->
            if (item.isClear()) {
                viewModel.selectItem(item)
                viewModel.setCurrentVisibleItem(item)
                bgBlurAdapter?.selectPos(position)
            } else {
                requireCoroutineScope().launch(Dispatchers.Main) {
                    var resourceItem: IEOResourceItem? = null
                    item.resource?.let {
                        val exist = viewModel.checkResourceExist(it)
                        if (!exist) {
                            bgBlurAdapter?.updateState(position, BgBlurItem.STATE_DOWNLOADING)
                            resourceItem = withContext(Dispatchers.IO) {
                                viewModel.loadResourceItem(it)
                            }
                        } else {
                            resourceItem = it
                        }
                    }
                    resourceItem?.let {
                        bgBlurAdapter?.updateState(position, BgBlurItem.STATE_DOWNLOADED)
                        bgBlurAdapter?.selectPos(position)
                        viewModel.selectItem(item)
                        viewModel.setCurrentVisibleItem(item)
                    } ?: run {
                        bgBlurAdapter?.updateState(position, BgBlurItem.STATE_REMOTE)
                    }
                }
            }
        }

        seekBarWithNode.setNodeSelectedListener(object : EOSeekBarNodeSelectedListener {
            override fun onNodeSelected(node: ComposerNode?) {
                viewModel.setCurrentVisibleComposerNode(node)
            }
        })

        closeButton.setOnClickListener {
            viewModel.selectItem(BgBlurItem(null, emptyList()))
            viewModel.setCurrentVisibleItem(null)
        }
    }
}