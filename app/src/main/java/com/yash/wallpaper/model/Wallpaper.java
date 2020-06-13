package com.yash.wallpaper.model;

public class Wallpaper {
    private String uid;
    private String uname;
    private String imageUri;
    private String wallpaperId;
    private String category;

    public Wallpaper(){}

    public Wallpaper(String uid, String uname, String imageUri, String category) {
        this.uid = uid;
        this.uname = uname;
        this.imageUri = imageUri;
        this.category = category;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getUname() {
        return uname;
    }

    public void setUname(String uname) {
        this.uname = uname;
    }

    public String getImageUri() {
        return imageUri;
    }

    public void setImageUri(String imageUri) {
        this.imageUri = imageUri;
    }

    public String getWallpaperId() {
        return wallpaperId;
    }

    public void setWallpaperId(String wallpaperId) {
        this.wallpaperId = wallpaperId;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }
}
