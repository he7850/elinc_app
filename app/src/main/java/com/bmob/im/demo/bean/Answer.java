package com.bmob.im.demo.bean;

import cn.bmob.v3.BmobObject;
import cn.bmob.v3.datatype.BmobRelation;

/**
 * Created by czr on 2015/7/26.
 */
public class Answer extends BmobObject {
    private User responder;
    private Question questionId;
    private String answerContent;
    private BmobRelation likedBy;

    public BmobRelation getLikedBy() {
        return likedBy;
    }

    public void setLikedBy(BmobRelation likedBy) {
        this.likedBy = likedBy;
    }

    public String getAnswerAvatar() {
        return answerAvatar;
    }

    public void setAnswerAvatar(String answerAvatar) {
        this.answerAvatar = answerAvatar;
    }

    private Answer quote;
    private String answerAvatar;
    public User getResponder() {
        return responder;
    }

    public Question getQuestionId() {
        return questionId;
    }
    public String getAnswerContent(){
        return answerContent;
    }

    public Answer getQuote() {
        return quote;
    }

    public void setResponder(User responder) {
        this.responder = responder;
    }

    public void setQuestionId(Question questionId) {
        this.questionId = questionId;
    }

    public void setAnswerContent(String answerContent) {
        this.answerContent = answerContent;
    }

    public void setQuote(Answer quote) {
        this.quote = quote;
    }
}
