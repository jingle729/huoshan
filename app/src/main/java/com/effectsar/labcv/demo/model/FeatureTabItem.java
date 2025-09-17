package com.effectsar.labcv.demo.model;

public class FeatureTabItem {
    private final String id;
    private final String title;
    private final int iconId;
    private final FeatureConfig content;
    public FeatureTabItem(String id, String title, int iconId, FeatureConfig config) {
        this.id = id;
        this.title = title;
        this.iconId = iconId;
        this.content = config;
    }
    public String getId() {
        if (id == null) {
            return "";
        }
        return id;
    }

    public String getTitle() {
        return title;
    }
    public int getIconId() {
        return iconId;
    }
    public FeatureConfig getConfig() {
        return content;
    }
}
