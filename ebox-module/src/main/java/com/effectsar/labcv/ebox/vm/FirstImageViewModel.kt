package com.effectsar.labcv.ebox.vm

import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.MutableLiveData
import com.bytedance.creativex.mediaimport.repository.api.BuiltInMaterialType
import com.bytedance.creativex.mediaimport.repository.api.IMaterialItem
import com.bytedance.creativex.mediaimport.repository.internal.cursor.DefaultQueryParams
import com.bytedance.creativex.mediaimport.repository.internal.main.DefaultMaterialRepositoryFactory
import com.volcengine.ck.album.init.AlbumInit
import com.volcengine.effectone.permission.Scene
import com.volcengine.effectone.singleton.AppSingleton
import com.volcengine.effectone.utils.EOUtils
import com.volcengine.effectone.viewmodel.BaseViewModel
import com.volcengine.effectone.viewmodel.EffectOneViewModelFactory
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.addTo
import io.reactivex.schedulers.Schedulers

/**
 *Author: gaojin
 *Time: 2023/12/18 16:13
 */

class FirstImageViewModel(activity: FragmentActivity) : BaseViewModel(activity) {

    companion object {
        fun get(activity: FragmentActivity): FirstImageViewModel {
            return EffectOneViewModelFactory.viewModelProvider(activity).get(FirstImageViewModel::class.java)
        }
    }


    private val disposable = CompositeDisposable()

    val firstMaterialItem = MutableLiveData<IMaterialItem?>()

    fun checkPermissions(successAction: () -> Unit, failedAction: (deniedList: List<String>) -> Unit) {
        activity?.let {
            EOUtils.permission.checkPermissions(it, Scene.ALBUM, successAction, failedAction)
        }
    }

    fun queryFirstMedia(lifecycleOwner: LifecycleOwner) {
        val media = firstMaterialItem.value
        if (media == null) {
            activity?.let {
                if (EOUtils.permission.checkAlbumPermission(it)) {
                    queryInternal(lifecycleOwner)
                }
            }
        }
    }

    private fun queryInternal(lifecycleOwner: LifecycleOwner) {
        AlbumInit.init(AppSingleton.instance)
        val repo = DefaultMaterialRepositoryFactory(lifecycleOwner).apply {
            setImageQueryParamProvider {
                DefaultQueryParams.DEFAULT_IMAGE_QUERY_PARAM
            }
            setVideoQueryParamProvider {
                DefaultQueryParams.DEFAULT_VIDEO_QUERY_PARAM
            }
        }.create()
        repo.iterator(BuiltInMaterialType.ALL)
            .next(2)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { materialList ->
                firstMaterialItem.value = materialList.maxByOrNull { it.date }
                repo.release()
            }.addTo(disposable)
    }

    override fun onDestroy(owner: LifecycleOwner) {
        super.onDestroy(owner)
        disposable.clear()
    }
}