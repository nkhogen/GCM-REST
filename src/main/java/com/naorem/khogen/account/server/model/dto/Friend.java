package com.naorem.khogen.account.server.model.dto;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

@XmlType(name = "friend")
@XmlRootElement(name = "friend")
@XmlAccessorType(XmlAccessType.FIELD)
@JsonIgnoreProperties(ignoreUnknown = false)
@JsonInclude(value = Include.NON_NULL)
public class Friend {
	// Passwords are hidden
	private UserCredential userCredential;
	private UserCredential friendCredential;
	private FriendStatus status;

	public UserCredential getUserCredential() {
		return userCredential;
	}

	public void setUserCredential(UserCredential userCredential) {
		this.userCredential = userCredential;
	}

	public UserCredential getFriendCredential() {
		return friendCredential;
	}

	public void setFriendCredential(UserCredential friendCredential) {
		this.friendCredential = friendCredential;
	}

	public FriendStatus getStatus() {
		return status;
	}

	public void setStatus(FriendStatus status) {
		this.status = status;
	}
}
