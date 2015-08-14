package com.bmob.im.demo.bean;

import cn.bmob.v3.Bmob;
import cn.bmob.v3.BmobObject;

/**
 * Created by HUBIN on 2015/8/3.
 */
public class CardReply extends BmobObject {
    private String content;
    private User replyAuthor;
    private User replyTo;
    private Card card;

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public User getReplyAuthor() {
        return replyAuthor;
    }

    public void setReplyAuthor(User replyAuthor) {
        this.replyAuthor = replyAuthor;
    }

    public User getReplyTo() {
        return replyTo;
    }

    public void setReplyTo(User replyTo) {
        this.replyTo = replyTo;
    }

    public Card getCard() {
        return card;
    }

    public void setCard(Card card) {
        this.card = card;
    }
}
