package com.naorem.khogen.account.server.domain.entities;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import javax.persistence.Version;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

@Table(name = "Account", uniqueConstraints = { @UniqueConstraint(columnNames = { "USER_ID" }), @UniqueConstraint(columnNames = { "ADDRESS" }) })
@XmlAccessorType(XmlAccessType.FIELD)
@JsonIgnoreProperties(ignoreUnknown = false)
@JsonInclude(value = Include.NON_NULL)
@JsonAutoDetect(fieldVisibility = Visibility.ANY, getterVisibility = Visibility.NONE, setterVisibility = Visibility.NONE)
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, include = JsonTypeInfo.As.PROPERTY, property = "@class")
@Entity
public class AccountDO implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * primary key
	 */
	@Id
	@Column(name = "ACCOUNT_ID", nullable = false, length = 40)
	private String id;

	/**
	 * database version
	 */
	@Version
	@Column(name = "LOCK_VERSION")
	private Long lockVersion;
	@Column(name = "CREATED_TIME")
	private Long createdTimestamp;
	@Column(name = "MODIFIED_TIME")
	private Long modifiedTimestamp;
	@Column(name = "USER_NAME", nullable = false, length = 300)
	private String username;
	@Column(name = "USER_ID", nullable = false, length = 100)
	private String userId;
	@Column(name = "ADDRESS", nullable = false, length = 300)
	// 190 is the length
	private String address;
	@Column(name = "PASSWORD", nullable = false, length = 100)
	private String password;
	@Column(name = "API_KEY", nullable = true, length = 200)
	// unused for now
	private String apiKey;
	@Column(name = "STATUS", nullable = true, length = 10)
	// unused for now
	private String status;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public Long getLockVersion() {
		return lockVersion;
	}

	public void setLockVersion(Long lockVersion) {
		this.lockVersion = lockVersion;
	}

	public Long getCreatedTimestamp() {
		return createdTimestamp;
	}

	public void setCreatedTimestamp(Long createdTimestamp) {
		this.createdTimestamp = createdTimestamp;
	}

	public Long getModifiedTimestamp() {
		return modifiedTimestamp;
	}

	public void setModifiedTimestamp(Long modifiedTimestamp) {
		this.modifiedTimestamp = modifiedTimestamp;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getUserId() {
		return userId;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}

	public String getAddress() {
		return address;
	}

	public void setAddress(String address) {
		this.address = address;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getApiKey() {
		return apiKey;
	}

	public void setApiKey(String apiKey) {
		this.apiKey = apiKey;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

}
