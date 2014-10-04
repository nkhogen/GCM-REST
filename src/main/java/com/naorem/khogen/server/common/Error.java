package com.naorem.khogen.server.common;

import java.text.MessageFormat;
import java.util.EnumSet;

/**
 * Policy error codes, messages and helper methods
 * 
 */
public enum Error {
	/* Start of error codes */

	SUCCESS(0, "Success"), INVALID_PARAM(99, "Invalid parameter"), VALIDATION_ERROR(100, "Invalid data"), ACCOUNT_ALREADY_EXIST(101, "Account already exists"), ACCOUNT_NOT_FOUND(
			102, "Account not found"), DATABASE_ERROR(103, "Database error"), MESSAGE_NOT_FOUND(104, "Message not found "), USER_LOGGED_OUT(105, "User not logged in"), SEND_MESSAGE_ERROR(
			106, "Unable to send message"), UNKNOWN_ERROR(107, "Unknown error");

	/* End of error codes */

	private final int code;
	private final String message;
	private String description;
	private Exception exception;

	private Error(final int code, final String message) {
		this.code = code;
		this.message = message;
	}

	public int getCode() {
		return code;
	}

	public String getMessage() {
		return message;
	}

	public String getDescription() {
		if (description == null) {
			return message;
		}
		return description;
	}

	public Exception getException() {
		return exception;
	}

	public void setDescription(final String description) {
		if (description != null) {
			this.description = message + ". " + description;
		}
	}

	public void setDescription(final String description, final Object... arguments) {
		if (description != null) {
			this.description = message + ". " + (arguments == null ? description : MessageFormat.format(message, description));
		}
	}

	public void setException(final Exception e) {
		this.exception = e;
	}

	public static Error getError(final int code) {
		for (Error error : EnumSet.allOf(Error.class)) {
			if (error.getCode() == code) {
				return error;
			}
		}
		throw new IllegalArgumentException("Unknown error code " + code);
	}
}
