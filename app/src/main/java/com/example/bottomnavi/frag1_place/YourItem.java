package com.example.bottomnavi.frag1_place;

public class YourItem {

    private String imageUrl;
    private String text;
    private String location;
    private String nickname;
    public YourItem(String imageUrl, String text, String location, String nickname) {
        this.imageUrl = imageUrl;
        this.text = text;
        this.location = location;
        this.nickname = nickname;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public String getText() {
        return text;
    }

    public String getLocation() {
        return location;
    }

    public String getNickname() {
        return nickname;
    }

}