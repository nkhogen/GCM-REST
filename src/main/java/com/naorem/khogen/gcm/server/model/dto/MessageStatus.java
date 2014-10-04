package com.naorem.khogen.gcm.server.model.dto;

import java.util.EnumSet;

public enum MessageStatus {
	DELIVERED, PENDING;

	public static MessageStatus getMessageStatus(final String value) {
		for (MessageStatus messageStatus : EnumSet.allOf(MessageStatus.class)) {
			if (messageStatus.name().equalsIgnoreCase(value)) {
				return messageStatus;
			}
		}
		throw new IllegalArgumentException("Inavlid enum value "+value);
	}
}
