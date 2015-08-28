package com.elinc.im.elinc.bean;

import java.util.List;

import cn.bmob.im.bean.BmobChatUser;
import cn.bmob.v3.datatype.BmobGeoPoint;
import cn.bmob.v3.datatype.BmobRelation;

/** 重载BmobChatUser对象：若还有其他需要增加的属性可在此添加
  * @ClassName: TextUser
  * @Description: TODO
  * @author smile
  * @date 2014-5-29 下午6:15:45
  */
public class User extends BmobChatUser {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	/**
	 * 发布的博客列表
	 */
	private BmobRelation blogs;
	
	/**
	 * //显示数据拼音的首字母
	 */
	private String sortLetters;
	
	/**
	 * //性别-true-男
	 */
	private Boolean sex;
	
	private Blog blog;
	
	/**
	 * 地理坐标
	 */
	private BmobGeoPoint location;//
	
	private Integer hight;
	/*下面是自己添加的成员变量*/
	private String university;
	private String campus;
	private String signature;
	private List<String> tags;

	public List<String> getTags() {
		return tags;
	}

	public void setTags(List<String> tags) {
		this.tags = tags;
	}

	public String getInviter() {
		return inviter;
	}

	public void setInviter(String inviter) {
		this.inviter = inviter;
	}

	private String grade;
	private String inviter;
	private BmobRelation follow;
	private Integer zan;
	public static long getSerialVersionUID() {
		return serialVersionUID;
	}

	public BmobRelation getFollow() {
		return follow;
	}

	public Integer getZan() {
		return zan;
	}

	public void setZan(Integer zan) {
		this.zan = zan;
	}


	 /*这个是储存用户关注的问题*/
	public Blog getBlog() {
		return blog;
	}
	public void setBlog(Blog blog) {
		this.blog = blog;
	}
	public Integer getHight() {
		return hight;
	}
	public void setHight(Integer hight) {
		this.hight = hight;
	}
	public BmobRelation getBlogs() {
		return blogs;
	}
	public void setBlogs(BmobRelation blogs) {
		this.blogs = blogs;
	}
	public BmobGeoPoint getLocation() {
		return location;
	}
	public void setLocation(BmobGeoPoint location) {
		this.location = location;
	}
	public Boolean getSex() {
		return sex;
	}
	public void setSex(Boolean sex) {
		this.sex = sex;
	}
	public String getSortLetters() {
		return sortLetters;
	}
	public void setSortLetters(String sortLetters) {
		this.sortLetters = sortLetters;
	}
	/*下面是自己添加的成员方法*/

	public String getCampus() {
		return campus;
	}



	public String getGrade() {
		return grade;
	}

	public String getSignature() {
		return signature;
	}

	public String getUniversity() {
		return university;
	}

	public void setCampus(String campus) {
		this.campus = campus;
	}

	public void setGrade(String grade) {
		this.grade = grade;
	}

	public void setSignature(String signature) {
		this.signature = signature;
	}

	public void setUniversity(String university) {
		this.university = university;
	}

	public void setFollow(BmobRelation follow) {
		this.follow = follow;
	}
}
