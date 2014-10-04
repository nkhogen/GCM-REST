package com.naorem.khogen.account.server.domain.entities;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
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

@Table(name = "Friend", uniqueConstraints = { @UniqueConstraint(columnNames = { "USER_ID", "FRIEND_USER_ID" }) })
@XmlAccessorType(XmlAccessType.FIELD)
@JsonIgnoreProperties(ignoreUnknown = false)
@JsonInclude(value = Include.NON_NULL)
@JsonAutoDetect(fieldVisibility = Visibility.ANY, getterVisibility = Visibility.NONE, setterVisibility = Visibility.NONE)
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, include = JsonTypeInfo.As.PROPERTY, property = "@class")
@Entity
public class FriendDO implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	/**
	 * primary key
	 */
	@Id
	@Column(name = "FRIEND_ID", nullable = false, length = 40)
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
	@Column(name = "USER_ID", nullable = false, length = 100)
	private String userId;
	@Column(name = "FRIEND_USER_ID", nullable = false, length = 100)
	private String friendId;
	@Column(name = "STATUS", nullable = false, length = 10)
	private String status;

	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "USER_ID", referencedColumnName = "USER_ID", insertable = false, updatable = false)
	private AccountDO userAccountDO;

	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "FRIEND_USER_ID", referencedColumnName = "USER_ID", insertable = false, updatable = false)
	private AccountDO friendAccountDO;

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

	public String getUserId() {
		return userId;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}

	public String getFriendId() {
		return friendId;
	}

	public void setFriendId(String friendId) {
		this.friendId = friendId;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public AccountDO getUserAccountDO() {
		return userAccountDO;
	}

	public void setUserAccountDO(AccountDO userAccountDO) {
		this.userAccountDO = userAccountDO;
	}

	public AccountDO getFriendAccountDO() {
		return friendAccountDO;
	}

	public void setFriendAccountDO(AccountDO friendAccountDO) {
		this.friendAccountDO = friendAccountDO;
	}
}
