package com.binfast.boottest.service;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;

public class UserInfo implements java.io.Serializable {

	@NotNull
	private String name;

	@NotNull
	@Positive
	private Long userId;

	private String sex;

	private String idcard;
	
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Long getUserId() {
		return userId;
	}

	public void setUserId(Long userId) {
		this.userId = userId;
	}

	public String getSex() {
		return sex;
	}

	public void setSex(String sex) {
		this.sex = sex;
	}

	public String getIdcard() {
		return idcard;
	}

	public void setIdcard(String idcard) {
		this.idcard = idcard;
	}
	
	
	
}
