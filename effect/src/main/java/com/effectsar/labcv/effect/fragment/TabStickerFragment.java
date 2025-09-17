package com.effectsar.labcv.effect.fragment;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import android.view.View;

import com.effectsar.labcv.common.fragment.TabBoardFragment;
import com.effectsar.labcv.common.utils.LocaleUtils;
import com.effectsar.labcv.core.effect.EffectResourceHelper;
import com.effectsar.labcv.core.util.LogUtils;
import com.effectsar.labcv.effect.R;
import com.effectsar.labcv.effect.manager.EffectDataManager;
import com.effectsar.labcv.effect.resource.StickerGroup;
import com.effectsar.labcv.effect.resource.StickerItem;
import com.effectsar.labcv.resource.MaterialResource;
import com.effectsar.platform.struct.CategoryData;
import com.effectsar.platform.struct.CategoryTabItem;
import com.effectsar.platform.struct.Material;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

;

public class TabStickerFragment extends TabBoardFragment implements View.OnClickListener {

    private OnTabStickerFramentCallback mCallback;

    private int mSelectedTab = 0;

    public interface OnTabStickerFramentCallback {
        void onStickerSelected(MaterialResource item, int tabIndex, int contentIndex);
        void onClickEvent(View view);
    }


    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view,savedInstanceState);
    }

    @Override
    public void onViewPagerSelected(int position) {

    }

    @Override
    public void onClickEvent(View view) {
        if (mCallback == null) {
            LogUtils.e("mEffectCallback == null!!");

            return;
        }
        mCallback.onClickEvent(view);
    }

    @Override
    public void setData() {

    }

    public void setData(ArrayList<MaterialResource> localMaterialList){
        if (localMaterialList == null) {
            return;
        }
        ArrayList<Fragment> fragments = new ArrayList<>();
        ArrayList<String> titles = new ArrayList<>();
        localMaterialList.add(0, new MaterialResource("", getContext().getString(R.string.close), "", R.drawable.clear));
        fragments.add(new StickerFragment()
                .setData(localMaterialList)
                .setCallback(new StickerFragment.StickerFragmentCallback() {
                    @Override
                    public void onItemClick(MaterialResource item, int position) {
                        mCallback.onStickerSelected(item, 0, position);
                    }
                }));

        titles.add("Local Test");
        refreshTabPageAdapterData(fragments,titles);
    }

    public void setData(CategoryData categoryData) {
        if (categoryData == null) {
            return;
        }

        if (getContext() == null) {
            LogUtils.e("getContext() == null");
            return;
        }

        ArrayList<Fragment> fragments = new ArrayList<>();
        ArrayList<String> titles = new ArrayList<>();

        List<CategoryTabItem> tabList = categoryData.getTabs();

        for (int i = 0; i < tabList.size(); i++) {
            int tabIndex = i;
            CategoryTabItem categoryTabItem = categoryData.getTabs().get(tabIndex);

            List<MaterialResource> materialResourceList = new ArrayList<>();
            materialResourceList.add(new MaterialResource("", requireContext().getString(R.string.close), "",R.drawable.clear));
            for (Material material : categoryTabItem.getItems()) {
                materialResourceList.add(new MaterialResource(material));
            }

            // hide tabs in local version
            if (LocaleUtils.getCurrentLocale(getContext()).getLanguage().equals("zh") &&
                    EffectDataManager.getTranslationId(categoryTabItem.getTitle()) == R.string.tab_beauty_sticker
            ) {
                continue;
            }

            // constructs fragments & titles
            fragments.add(new StickerFragment()
                    .setData(materialResourceList)
                    .setCallback(new StickerFragment.StickerFragmentCallback() {
                        @Override
                        public void onItemClick(MaterialResource item, int position) {
                            mCallback.onStickerSelected(item, tabIndex, position);
                        }
                    }));

            titles.add(EffectDataManager.translateText(requireContext(), categoryTabItem.getTitle()));
        }
        refreshTabPageAdapterData(fragments,titles);
    }

//    public void setData(List<StickerGroup> groups) {
//        ArrayList<Fragment> fragments = new ArrayList<>();
//        ArrayList<String> titles = new ArrayList<>();
//
//        for (int i = 0; i < groups.size(); i++) {
//            StickerGroup group = groups.get(i);
//            int finalI = i;
//            fragments.add(new StickerFragment()
//                        .setCallback(new StickerFragment.StickerFragmentCallback() {
//                            @Override
//                            public void onItemClick(StickerItem item, int position) {
//                                mCallback.onStickerSelected(item, finalI, position);
//                            }
//                        }).setData(Arrays.asList(group.getItems())));
//            titles.add(group.getTitle(getContext()));
//        }
//
//        refreshTabPageAdapterData(fragments,titles);
//    }

    public void setCallback(OnTabStickerFramentCallback callback) {
        mCallback = callback;
    }

    public void resetDefault() {
        StickerFragment fragment = (StickerFragment) getCurrentFragment();
        if (fragment != null) {
            fragment.setSelected(0);
        }
    }

    public void setSelected(int pos) {
        StickerFragment fragment = (StickerFragment) getCurrentFragment();
        if(fragment != null){
            fragment.setSelected(pos);
        }
    }

    public void refresh() {
        StickerFragment fragment = (StickerFragment) getCurrentFragment();
        if(fragment != null){
            fragment.refresh();
        }
    }

    public void selectItem(int tabIndex, int contentIndex) {
        if (mSelectedTab != tabIndex) {
            ((StickerFragment) getFragment(mSelectedTab)).setSelected(0);
            mSelectedTab = tabIndex;
        }
        StickerFragment fragment = (StickerFragment) getFragment(tabIndex);
        if (fragment != null) {
            fragment.setSelected(contentIndex);
        }
    }

    public void refreshItem(int tabIndex, int contentIndex) {
        StickerFragment fragment = (StickerFragment) getFragment(tabIndex);
        if (fragment != null) {
            fragment.refreshItem(contentIndex);
        }
    }

}
