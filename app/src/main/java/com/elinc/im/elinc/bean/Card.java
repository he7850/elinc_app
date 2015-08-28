package com.elinc.im.elinc.bean;

import cn.bmob.v3.BmobObject;
import cn.bmob.v3.datatype.BmobRelation;

/**
 * Created by HUBIN on 2015/7/31.
 */
public class Card extends BmobObject {
    private Goal goal;
    private String cardClaim;
    private BmobRelation likedBy;
    private BmobRelation reply;
    private Integer likedByNum;

    public Integer getLikedByNum() {
        return likedByNum;
    }

    public void setLikedByNum(Integer likedByNum) {
        this.likedByNum = likedByNum;
    }

    public String getCardClaim() {
        return cardClaim;
    }

    public void setCardClaim(String cardClaim) {
        this.cardClaim = cardClaim;
    }

    public BmobRelation getReply() {
        return reply;
    }

    public void setReply(BmobRelation reply) {
        this.reply = reply;
    }

    public Goal getGoal() {
        return goal;
    }

    public void setGoal(Goal goal) {
        this.goal = goal;
    }

    public BmobRelation getLikedBy() {
        return likedBy;
    }

    public void setLikedBy(BmobRelation likedBy) {
        this.likedBy = likedBy;
    }
}
