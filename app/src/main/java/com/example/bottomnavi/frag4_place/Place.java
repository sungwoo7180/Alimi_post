package com.example.bottomnavi.frag4_place;

public class Place {
    private String id;
    private String place_name;
    private String category_name;
    private String category_group_code;
    private String category_group_name;
    private String phone;
    private String address_name;
    private String road_address_name;
    private String x;
    private String y;
    private String place_url;
    private String distance;

    public Place(String id, String x, String y) {
        this.place_name = place_name;
        this.x = x;
        this.y = y;
    }
    public String getId() {
        return id;
    }

    public String getPlaceName() {
        return place_name;
    }

    public String getCategoryName() {
        return category_name;
    }

    public String getCategoryGroupCode() {
        return category_group_code;
    }

    public String getCategoryGroupName() {
        return category_group_name;
    }

    public String getPhone() {
        return phone;
    }

    public String getAddressName() {
        return address_name;
    }

    public String getRoadAddressName() {
        return road_address_name;
    }

    public String getX() {
        return x;
    }

    public String getY() {
        return y;
    }

    public String getPlaceUrl() {
        return place_url;
    }

    public String getDistance() {
        return distance;
    }
}