package com.bmob.im.demo.bean;

import android.graphics.Point;

import java.util.ArrayList;
import java.util.List;

import cn.bmob.v3.BmobObject;
import cn.bmob.v3.datatype.BmobRelation;

/**
 * Created by HUBIN on 2015/7/24.
 */
public class Question extends BmobObject {
    private String questionContent;
    private User author;
    private String title;
    private List<String> tags;
    private String questionAvatar;
    private BmobRelation answerList;
    private BmobRelation followerList;

    public BmobRelation getFollowerList() {
        return followerList;
    }

    public void setFollowerList(BmobRelation followerList) {
        this.followerList = followerList;
    }

    public BmobRelation getAnswerList() {
        return answerList;
    }

    public void setAnswerList(BmobRelation answerList) {
        this.answerList = answerList;
    }

    public String getQuestionAvatar() {
        return questionAvatar;
    }

    public void setQuestionAvatar(String questionAvatar) {

            this.questionAvatar = questionAvatar;

    }

    public List<String> getTags() {
        return tags;
    }

    public void setTags(List<String> tags) {
        this.tags = tags;
    }

    public String getQuestionContent() {
        return questionContent;
    }

    public void setQuestionContent(String questionContent) {
        this.questionContent = questionContent;
    }

    public User getAuthor() {
        return author;
    }

    public void setAuthor(User author) {
        this.author = author;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }
}
