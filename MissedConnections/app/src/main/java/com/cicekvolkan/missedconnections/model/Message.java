package com.cicekvolkan.missedconnections.model;

public class Message {
    public static final int LAYOUT_ONE_MESSAGE = 1;

    public int viewType;
    public String mail;
    public String name;

    public Message(int viewType, String mail, String name) {
        this.viewType = viewType;
        this.mail = mail;
        this.name = name;
    }

    public int getViewType() {
        return viewType;
    }

    public void setViewType(int viewType) {
        this.viewType = viewType;
    }

    public String getMail() {
        return mail;
    }

    public void setMail(String mail) {
        this.mail = mail;
    }


    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
