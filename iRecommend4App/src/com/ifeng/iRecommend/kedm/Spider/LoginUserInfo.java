package com.ifeng.iRecommend.kedm.Spider;

import java.util.ArrayList;
import java.util.List;

public class LoginUserInfo {
	private String uid;
	private String loginFrom;
	private String name;
	private String school;
	private String birthday;
	private String tags;
	private String gender;
	private String verified;
	private String description;
	private String phone;
	private List<String> V = new ArrayList<String>();
	public List<String> getV() {
		return V;
	}
	public void setV(List<String> v) {
		V = v;
	}
	public String getUid() {
		return uid;
	}
	public void setUid(String uid) {
		this.uid = uid;
	}
	public String getLoginFrom() {
		return loginFrom;
	}
	public void setLoginFrom(String loginFrom) {
		this.loginFrom = loginFrom;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getSchool() {
		return school;
	}
	public void setSchool(String school) {
		this.school = school;
	}
	public String getBirthday() {
		return birthday;
	}
	public void setBirthday(String birthday) {
		this.birthday = birthday;
	}
	public String getTags() {
		return tags;
	}
	public void setTags(String tags) {
		this.tags = tags;
	}
	public String getGender() {
		return gender;
	}
	public void setGender(String gender) {
		this.gender = gender;
	}
	public String getVerified() {
		return verified;
	}
	public void setVerified(String verified) {
		this.verified = verified;
	}
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	public String getPhone() {
		return phone;
	}
	public void setPhone(String phone) {
		this.phone = phone;
	}

}
