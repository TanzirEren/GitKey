package com.gitkey.build.model;

public class ToolItem {
    private final String id;
    private final int titleRes;
    private final int descriptionRes;
    private final int iconRes;
    private final int colorRes;

    public ToolItem(String id, int titleRes, int descriptionRes, int iconRes, int colorRes) {
        this.id = id;
        this.titleRes = titleRes;
        this.descriptionRes = descriptionRes;
        this.iconRes = iconRes;
        this.colorRes = colorRes;
    }

    public String getId() { return id; }
    public int getTitleRes() { return titleRes; }
    public int getDescriptionRes() { return descriptionRes; }
    public int getIconRes() { return iconRes; }
    public int getColorRes() { return colorRes; }
}
