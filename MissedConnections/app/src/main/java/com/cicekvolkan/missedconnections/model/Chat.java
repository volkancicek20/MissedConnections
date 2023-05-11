package com.cicekvolkan.missedconnections.model;

public class Chat {
    public static final int LAYOUT_ONE_CHAT = 1;
    public static final int LAYOUT_TWO_CHAT = 2;

    public int viewType;
    public String message;
    public String date;

    public Chat(int viewType, String message, String date) {
        this.viewType = viewType;
        this.message = message;
        this.date = date;
    }

    public int getViewType() {
        return viewType;
    }

    public void setViewType(int viewType) {
        this.viewType = viewType;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }
}
