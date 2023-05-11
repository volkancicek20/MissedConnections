package com.cicekvolkan.missedconnections.model;

public class Post {

    public static final int LAYOUT_ONE = 1;

    public int viewType;
    public String imageUrl;
    public String name;
    public String mail;
    public String comment;
    public String locationCity;
    public String locationDistrict;
    public String see;
    public String date;
    public boolean check;

    public Post(int viewType, String imageUrl, String name,String mail, String comment,String locationCity,String locationDistrict, String date,String see,boolean check) {
        this.viewType = viewType;
        this.imageUrl = imageUrl;
        this.name = name;
        this.mail = mail;
        this.comment = comment;
        this.locationCity = locationCity;
        this.locationDistrict = locationDistrict;
        this.date = date;
        this.see = see;
        this.check = check;
    }
    public Post(int viewType, String name,String mail, String comment,String locationCity,String locationDistrict, String date,String see,boolean check) {
        this.viewType = viewType;
        this.name = name;
        this.mail = mail;
        this.comment = comment;
        this.locationCity = locationCity;
        this.locationDistrict = locationDistrict;
        this.date = date;
        this.see = see;
        this.check = check;
    }

    public boolean isCheck() {
        return check;
    }

    public void setCheck(boolean check) {
        this.check = check;
    }

    public String getSee() {
        return see;
    }

    public void setSee(String see) {
        this.see = see;
    }

    public String getMail() {
        return mail;
    }

    public void setMail(String mail) {
        this.mail = mail;
    }

    public String getLocationCity() {
        return locationCity;
    }

    public void setLocationCity(String locationCity) {
        this.locationCity = locationCity;
    }

    public String getLocationDistrict() {
        return locationDistrict;
    }

    public void setLocationDistrict(String locationDistrict) {
        this.locationDistrict = locationDistrict;
    }

    public int getViewType() {
        return viewType;
    }

    public void setViewType(int viewType) {
        this.viewType = viewType;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }
}
