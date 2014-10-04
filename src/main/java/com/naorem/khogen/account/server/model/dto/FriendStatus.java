package com.naorem.khogen.account.server.model.dto;

import java.util.EnumSet;

public enum FriendStatus {
	SENT("SENT"), RECEIVED("RECEIVED"), ACTIVE("ACTIVE");

	private final String value;

	FriendStatus(final String value) {
		this.value = value;
	}

	@Override
	public String toString() {
		return value;
	}

	public static FriendStatus getFriendStatus(final String value) {
		for (FriendStatus friendStatus : EnumSet.allOf(FriendStatus.class)) {
			if (friendStatus.toString().equalsIgnoreCase(value)) {
				return friendStatus;
			}
		}
		return null;
	}
}
