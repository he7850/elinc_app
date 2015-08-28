package com.elinc.im.elinc.bean;

import cn.bmob.v3.BmobObject;

/**
 * Created by HUBIN on 2015/8/1.
 */
public class Bug extends BmobObject {
    private String title;
    private String content;

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public User getSender() {
        return sender;
    }

    public void setSender(User sender) {
        this.sender = sender;
    }

    private User sender;
}
