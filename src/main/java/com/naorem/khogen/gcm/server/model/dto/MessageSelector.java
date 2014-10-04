package com.naorem.khogen.gcm.server.model.dto;

import java.util.EnumSet;

public enum MessageSelector {
	SEND, RECEIVE, SEND_RECEIVE;

	public static MessageSelector getMessageSelector(final String value) {
		for (MessageSelector messageSelector : EnumSet.allOf(MessageSelector.class)) {
			if (messageSelector.name().equalsIgnoreCase(value)) {
				return messageSelector;
			}
		}
		return null;
	}

}
