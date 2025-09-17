package com.effectsar.labcv.algorithm.ui;

import android.view.View;

import com.effectsar.labcv.algorithm.model.AlgorithmItem;
import com.effectsar.labcv.common.view.PropertyTextView;
import com.effectsar.labcv.core.algorithm.C2AlgorithmTask;
import com.effectsar.labcv.core.algorithm.base.AlgorithmTaskKey;
import com.effectsar.labcv.core.util.LogUtils;
import com.effectsar.labcv.cv.R;
import com.effectsar.labcv.effectsdk.BefC2Info;

import java.util.Arrays;

public class C2UI extends BaseAlgorithmUI<BefC2Info> {
    public static final int SHOW_NUM = 3;

    public static final String[] C2_TYPES = {
            "Shoes",
            "Sunglasses",
            "Dress",
            "miniskirt",
            "kimono",
            "Uniforms",
            "Hanfuphoto",
            "underwear",
            "Swimsuit",
            "Weddingdress",
            "Ethniccustoms",
            "cheongsam",
            "physicaleducation",
            "Yoga",
            "LuYing",
            "Ski",
            "Mountaineering",
            "Swimming",
            "PaoBu",
            "Dancer",
            "Halloween",
            "Christmas",
            "Party",
            "birthday",
            "jiehunzhao",
            "newYear",
            "Graduation",
            "Militarytraining",
            "concert",
            "Cosplay",
            "shopping",
            "reading",
            "Holdinghands",
            "Performance",
            "Cardgame",
            "shineijiudianjucan",
            "selfie",
            "Familyphotography",
            "QinZiSheYing",
            "group",
            "yinger",
            "ertong",
            "Oldman",
            "crowd",
            "Two",
            "alone",
            "student",
            "Pregnantwoman",
            "desert",
            "hill",
            "mountainsnowy",
            "Cave",
            "moutainpath",
            "Terracedfield",
            "formalgarden",
            "farm",
            "lawn",
            "jungle",
            "Flowersea",
            "islet",
            "Underwater",
            "lake",
            "ocean",
            "pond",
            "Waterdroplets",
            "creek",
            "waterfall",
            "reef",
            "beach",
            "sky",
            "Galaxy",
            "lightning",
            "Sunset",
            "rainbow",
            "Cloudsea",
            "Blueskyandwhiteclouds",
            "darkclouds",
            "moon",
            "nightscape",
            "Fireworks",
            "Bonfire",
            "Neon",
            "Rainyday",
            "xuejing",
            "fog",
            "flower",
            "tree",
            "tulip",
            "sunflower",
            "cactus",
            "Lotus",
            "Cherryblossoms",
            "Rose",
            "lavender",
            "Dandelion",
            "ginkgo",
            "reed",
            "DuoRouZhiWu",
            "Rapeflower",
            "Pottedplant",
            "Mapleleaf",
            "mianshi",
            "zhongcan",
            "hotpot",
            "AmericalFastFood",
            "HaiXian",
            "grill",
            "sushi",
            "ShaLa",
            "snacks",
            "dessert",
            "cake",
            "fruit",
            "drink",
            "breadnew",
            "bookstore",
            "restaurant",
            "hospital",
            "bar",
            "themall",
            "school",
            "shiwaiyoulechagn",
            "carrousel",
            "park",
            "Site",
            "Ferriswheel",
            "fountain",
            "CBD",
            "street",
            "foodcourt",
            "Interiordesign",
            "amusementarcade",
            "ktv",
            "movietheater",
            "playground",
            "soccerfield",
            "badmintonindoor",
            "Snooker",
            "golfcourse",
            "baseball",
            "basketballcourtindoor",
            "tennisoutdoor",
            "Swimming",
            "gymnasimum",
            "ruin",
            "Royalpalace",
            "Tower",
            "Stele",
            "gujianzhu",
            "castle",
            "WesternArch",
            "statue",
            "xiangcunjianzhu",
            "bicycle",
            "motorcycle",
            "Trafficflow",
            "car",
            "Bus",
            "aircraft",
            "Ship",
            "subway",
            "subwayplatform",
            "GaoTie",
            "LightRail",
            "trainstation",
            "bridge",
            "ZhanDao",
            "road",
            "doll",
            "srcamera",
            "guitar",
            "book",
            "Crafts",
            "screen",
            "tv",
            "mobilephone",
            "Pcgame",
            "Anime",
            "scrawlmark",
            "calligraphy",
            "sketch",
            "bankcard",
            "carddata",
            "qrcode",
            "text",
            "puzzle",
            "solidcolorbg",
            "Jellyfish",
            "goldfish",
            "Tortoise",
            "KingPenguin",
            "bird",
            "Peacock",
            "cat",
            "dog",
            "monkey",
            "deer",
            "Panda",
            "sheep",
            "camel",
            "horse",
            "Cattle",
            "Tiger",
            "Giraffe",
            "Hamster",
            "Elepant",
            "Pig",
            "rabbit"
    };

    private PropertyTextView ptv;

    @Override
    void initView() {
        super.initView();

        addLayout(R.layout.layout_c1_info, R.id.fl_algorithm_info);

        if (!checkAvailable(provider())) return;
        ptv = provider().findViewById(R.id.ptv_c1);
    }

    @Override
    public void onReceiveResult(BefC2Info befC2Info) {
        runOnUIThread(new Runnable() {
            @Override
            public void run() {
                if (!checkAvailable(provider())) return;
                if (befC2Info == null) return;

                LogUtils.e(Arrays.toString(befC2Info.topN(SHOW_NUM)));
                BefC2Info.BefC2CategoryItem[] items = befC2Info.topN(SHOW_NUM);
                if (items.length > 0) {
                    BefC2Info.BefC2CategoryItem item = items[0];
                    ptv.setTitle(C2_TYPES[item.getId()]);
                    ptv.setValue(String.format("%.2f", item.getConfidence()));
                } else {
                    ptv.setTitle(provider().getString(R.string.tab_c2));
                    ptv.setValue(provider().getString(R.string.video_cls_no_results));
                }
            }
        });
    }

    @Override
    public void onEvent(AlgorithmTaskKey key, boolean flag) {
        super.onEvent(key, flag);

        ptv.setVisibility(flag ? View.VISIBLE : View.INVISIBLE);
        if (!flag) {
            ptv.setTitle("");
            ptv.setValue("");
        }
    }

    @Override
    public AlgorithmItem getAlgorithmItem() {
        return (AlgorithmItem) new AlgorithmItem(C2AlgorithmTask.C2)
                .setTitle(R.string.tab_c2)
                .setDesc(R.string.c2_desc)
                .setIcon(R.drawable.ic_c2);
    }
}
