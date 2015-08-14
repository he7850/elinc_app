package com.bmob.im.demo.bean;

import cn.bmob.v3.BmobObject;

/**
 * Created by czr on 2015/7/30.
 */
public class Goal extends BmobObject {
    private String goalContent;
    private String claim;
    private User author;
    private Integer priority; //位置
    private Integer day;
    private Boolean out;

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    private String type;

    public Boolean getOut() {
        return out;
    }

    public void setOut(Boolean out) {
        this.out = out;
    }

    public Integer getPriority() {
        return priority;
    }
    public void setPriority(Integer priority) {
        this.priority = priority;
    }

    public void setGoalContent(String goalContent){
        this.goalContent=goalContent;
    }
    public void setClaim(String claim) { this.claim = claim; }
    public void setAuthor(User author) {
        this.author = author;
    }
    public void setDay(Integer day) {
        this.day = day;
    }
    public String getGoalContent() {
        return goalContent;
    }
    public String getClaim() {
        return claim;
    }
    public User getAuthor() { return author;  }
    public Integer getDay() {
        return day;
    }
}
