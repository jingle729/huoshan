package com.effectsar.labcv.ebox.matting

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.activity.result.ActivityResultLauncher
import com.bytedance.creativex.mediaimport.repository.api.IMediaItem
import com.effectsar.labcv.ebox.R
import com.volcengine.ck.LocalAlbumActivityContract
import com.volcengine.ck.album.AlbumEntrance
import com.volcengine.ck.album.base.AlbumConfig
import com.volcengine.effectone.permission.Scene.ALBUM
import com.volcengine.effectone.singleton.AppSingleton
import com.volcengine.effectone.ui.BaseBottomSheetDialogFragment
import com.volcengine.effectone.utils.EOUtils
import com.volcengine.effectone.widget.EOLoadingState
import com.volcengine.effectone.widget.EOLoadingView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject

/**
 *Author: gaojin.ivy
 *Time: 2025/6/15 16:15
 */

class MattingPanel : BaseBottomSheetDialogFragment() {

    companion object {
        const val TAG = "MattingPanel"
    }

    private lateinit var closeContainer: View
    private lateinit var closeImage: View
    private lateinit var closeText: View
    private lateinit var uploadContainer: View
    private lateinit var loadingView: EOLoadingView
    private lateinit var titleView: TextView
    private var titleResId = 0

    private val albumActivityContract: LocalAlbumActivityContract = LocalAlbumActivityContract()
    private var albumLauncher: ActivityResultLauncher<AlbumConfig>? = null

    private val mattingViewModel by lazy { EboxMattingViewModel.get(requireActivity()) }

    override fun getFragmentTag() = TAG

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.ebox_layout_matting_panel, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        closeContainer = view.findViewById(R.id.ebox_matting_close)
        closeImage = view.findViewById(R.id.ebox_matting_close_image)
        closeText = view.findViewById(R.id.ebox_matting_close_text)
        uploadContainer = view.findViewById(R.id.ebox_matting_upload)
        loadingView = view.findViewById(R.id.ebox_matting_loading_view)
        loadingView.setOnClickListener {
            //拦截点击事件
        }
        titleView = view.findViewById(R.id.ebox_matting_panel_name)
        if (titleResId != 0) {
            titleView.text = AppSingleton.instance.getString(titleResId)
        }
        albumLauncher = registerForActivityResult(albumActivityContract) { result ->
            if (result.isNotEmpty()) {
                val mediaItem = result.first() as? IMediaItem ?: return@registerForActivityResult
                mattingViewModel.updateCustomBg(mediaItem)
                dismiss()
            }
        }

        closeContainer.setOnClickListener {
            mattingViewModel.changeMattingState()
        }

        uploadContainer.setOnClickListener {
            EOUtils.permission.checkPermissions(requireActivity(), ALBUM, {
                albumLauncher?.launch(
                    AlbumConfig(
                        allEnable = false,
                        videoEnable = true,
                        imageEnable = true,
                        maxSelectCount = 1,
                        showGif = false
                    )
                )
            }, {
                AlbumEntrance.showAlbumPermissionTips(requireActivity())
            })
        }

        requireCoroutineScope().launch(Dispatchers.Main) {
            initPanel()
        }

        initObserver()
    }

    private fun initObserver() {
        mattingViewModel.mattingSelectedItem.observe(viewLifecycleOwner) {
            it?.let { item ->
                if (item.isEmpty()) {
                    closeImage.alpha = 0.5F
                    closeText.alpha = 0.5F
                } else {
                    closeImage.alpha = 1F
                    closeText.alpha = 1F
                }
            }
        }
    }

    private suspend fun initPanel() {
        if (!mattingViewModel.hasData()) {
            loadingView.visibility = View.VISIBLE
            val eoResourceItem = withContext(Dispatchers.IO) {
                val resource = mattingViewModel.loadResourceList(customRequestKey).firstOrNull()?.subItems?.firstOrNull()
                val result = resource?.let {
                    mattingViewModel.loadResourceItem(it)
                }
                result
            }
            if (eoResourceItem == null) {
                loadingView.setState(EOLoadingState.NETWORK_ERROR)
                return
            }

            var defaultBgPath = ""
            runCatching {
                val configStr = (eoResourceItem.extra?.get("config") as? String) ?: ""
                val jsonObject = JSONObject(configStr)
                defaultBgPath = jsonObject.optString("default_bg_path")
            }
            val fullPath = "${eoResourceItem.absPath}${defaultBgPath}"
            mattingViewModel.initMattingItem(MattingItem(fullPath, null, eoResourceItem))
            loadingView.visibility = View.GONE
        }
    }

    fun setTitleResId(id: Int) {
        titleResId = id
    }
}