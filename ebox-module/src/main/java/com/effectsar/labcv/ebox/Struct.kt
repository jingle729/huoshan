package com.effectsar.labcv.ebox

import android.os.Parcelable
import androidx.annotation.Keep
import com.effectsar.labcv.common.utils.LocaleUtils
import com.google.gson.annotations.SerializedName
import com.volcengine.effectone.base.TitleDict
import com.volcengine.effectone.singleton.AppSingleton
import kotlinx.parcelize.Parcelize

/**
 *Author: gaojin.ivy
 *Time: 2025/5/28 15:03
 */

@Keep
@Parcelize
data class MainPageConfig(
    @SerializedName("groups")
    val eboxPageConfigGroups: List<EBoxPageConfigGroup> = emptyList(),

    @SerializedName("pageDetails")
    val pageDetails: List<PageDetail> = emptyList()
) : Parcelable

@Keep
@Parcelize
data class EBoxPageConfigGroup(
    @SerializedName("id")
    val id: String = "",

    @SerializedName("titleDict")
    val titleDict: TitleDict? = null,

    @SerializedName("pages")
    val pages: List<Page> = emptyList()
) : Parcelable

@Keep
@Parcelize
data class Page(
    @SerializedName("id")
    val id: String = "",

    @SerializedName("titleDict")
    val titleDict: TitleDict? = null
) : Parcelable

@Keep
@Parcelize
data class PageDetail(
    @SerializedName("id")
    val id: String = "",

    @SerializedName("title")
    var title: String = "",

    @SerializedName("panels")
    val panels: List<Panel> = emptyList()
) : Parcelable {
    companion object {
        const val PAGE_DETAIL_KEY = "page_detail_key"
    }
}

@Keep
@Parcelize
data class Panel(
    @SerializedName("key")
    val key: String = "",

    @SerializedName("bizLists")
    val bizLists: List<BizList> = emptyList(),

    @SerializedName("uiTree")
    val uiTree: UiTree? = null
) : Parcelable

@Keep
@Parcelize
data class BizList(
    @SerializedName("key")
    val key: String = "",

    @SerializedName("titleDict")
    val titleDict: TitleDict? = null
) : Parcelable

@Keep
@Parcelize
data class UiTree(
    @SerializedName("uniqueId")
    val uniqueId: String = "",

    @SerializedName("subItems")
    val subItems: List<SubItem> = emptyList()
) : Parcelable

@Keep
@Parcelize
data class SubItem(
    @SerializedName("uniqueId")
    val uniqueId: String = "",

    @SerializedName("titleDict")
    val titleDict: TitleDict? = null,

    @SerializedName("subItems")
    val subItems: List<SubItem> = emptyList()
) : Parcelable