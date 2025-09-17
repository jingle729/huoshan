package com.effectsar.labcv.ebox.makeup

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.SeekBar
import android.widget.SeekBar.OnSeekBarChangeListener
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.effectsar.labcv.ebox.R
import com.effectsar.labcv.ebox.blur.BgBlurItem
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
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
 *Time: 2025/6/19 15:09
 */

class StyleMakeUpPanel : BaseBottomSheetDialogFragment() {

    companion object {
        const val TAG = "StyleMakeUpPanel"
    }

    private lateinit var seekBarWithNode: EOSeekBarWithNodeView
    private lateinit var loadingView: EOLoadingView
    private lateinit var closeButton: ImageView
    private var styleAdapter: StyleMakeUpRecyclerAdapter? = null


    private val viewModel by lazy { StyleMakeUpViewModel.get(requireActivity()) }

    override fun getFragmentTag() = TAG

    override fun onStart() {
        super.onStart()
        val dialog = dialog as? BottomSheetDialog
        val bottomSheet = dialog?.findViewById<View>(com.google.android.material.R.id.design_bottom_sheet)

        bottomSheet?.let {
            val behavior = BottomSheetBehavior.from(it)
            behavior.isDraggable = false
            behavior.state = BottomSheetBehavior.STATE_EXPANDED
            behavior.skipCollapsed = true
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.ebox_layout_style_makeup_panel, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        seekBarWithNode = view.findViewById(R.id.ebox_style_makeup_seekbar)
        loadingView = view.findViewById(R.id.ebox_style_makeup_loading_view)
        closeButton = view.findViewById(R.id.ebox_style_makeup_close_item)
        requireCoroutineScope().launch(Dispatchers.Main) {
            initPanel(view)
        }
        initObserver()
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
            val styleMakeUpItemList = itemList.map {
                it.toStyleMakeUpItem().apply {
                    state = if (viewModel.checkResourceExist(it)) {
                        StyleMakeUpItem.STATE_DOWNLOADED
                    } else {
                        StyleMakeUpItem.STATE_REMOTE
                    }
                }
            }
            viewModel.itemList.addAll(styleMakeUpItemList)
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

        styleAdapter = StyleMakeUpRecyclerAdapter(viewModel.itemList)

        rootView.findViewById<RecyclerView>(R.id.ebox_style_makeup_list).apply {
            layoutManager = GridLayoutManager(context, 5).apply {
                orientation = RecyclerView.VERTICAL
            }
            adapter = styleAdapter
        }

        styleAdapter?.selectedAction = { position, item ->
            if (item.isClear()) {
                viewModel.selectItem(item)
                viewModel.setCurrentVisibleItem(item)
                styleAdapter?.selectPos(position)
            } else {
                requireCoroutineScope().launch(Dispatchers.Main) {
                    var resourceItem: IEOResourceItem? = null
                    item.resource?.let {
                        val exist = viewModel.checkResourceExist(it)
                        if (!exist) {
                            styleAdapter?.updateState(position, BgBlurItem.STATE_DOWNLOADING)
                            resourceItem = withContext(Dispatchers.IO) {
                                viewModel.loadResourceItem(it)
                            }
                        } else {
                            resourceItem = it
                        }
                    }
                    resourceItem?.let {
                        styleAdapter?.updateState(position, BgBlurItem.STATE_DOWNLOADED)
                        styleAdapter?.selectPos(position)
                        viewModel.selectItem(item)
                        viewModel.setCurrentVisibleItem(item)
                    } ?: run {
                        styleAdapter?.updateState(position, BgBlurItem.STATE_REMOTE)
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
            viewModel.selectItem(StyleMakeUpItem(null, emptyList()))
            viewModel.setCurrentVisibleItem(null)
        }
    }

    private fun initObserver() {
        viewModel.currentVisibleItem.observe(viewLifecycleOwner) { item ->
            if (item == null || item.isClear()) {
                if (item == null) {
                    styleAdapter?.cancelPos()
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
}