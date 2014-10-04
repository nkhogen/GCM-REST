package com.naorem.khogen.gcm.server.model.dto;

import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

@XmlType(name = "broadcastRequest")
@XmlRootElement(name = "broadcastRequest")
@XmlAccessorType(XmlAccessType.FIELD)
@JsonIgnoreProperties(ignoreUnknown = false)
@JsonInclude(value = Include.NON_NULL)
public class BroadcastRequest {
	private String senderId;
	@XmlElementWrapper(name = "receiverIds")
	private List<String> receiverIds;
	private String message;

	public String getSenderId() {
		return senderId;
	}

	public void setSenderId(String senderId) {
		this.senderId = senderId;
	}

	public List<String> getReceiverIds() {
		return receiverIds;
	}

	public void setReceiverIds(List<String> receiverIds) {
		this.receiverIds = receiverIds;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}
}
