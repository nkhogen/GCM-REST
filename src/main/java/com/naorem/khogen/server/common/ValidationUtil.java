package com.naorem.khogen.server.common;

import java.util.Collection;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import com.naorem.khogen.account.server.model.dto.Account;
import com.naorem.khogen.account.server.model.dto.UserCredential;
import com.naorem.khogen.gcm.server.model.dto.BroadcastRequest;

public class ValidationUtil {
	private static final Logger LOGGER = LoggerFactory.getLogger(ValidationUtil.class);

	public static Error validateAccount(final Account account) {
		if (account == null) {
			Error error = Error.INVALID_PARAM;
			error.setDescription("Account is null");
			LOGGER.error(error.getDescription());
			return error;
		}
		UserCredential userCredential = account.getCredential();
		if (userCredential == null) {
			Error error = Error.INVALID_PARAM;
			error.setDescription("User credential is null");
			LOGGER.error(error.getDescription());
			return error;
		}
		if (userCredential.getAddress() == null) {
			Error error = Error.INVALID_PARAM;
			error.setDescription("Address is null");
			LOGGER.error(error.getDescription());
			return error;
		}
		if (!CommonUtil.isEmailValid(userCredential.getId())) {
			Error error = Error.INVALID_PARAM;
			error.setDescription("User ID is null");
			LOGGER.error(error.getDescription());
			return error;
		}
		
		if (userCredential.getPassword() == null) {
			Error error = Error.INVALID_PARAM;
			error.setDescription("Password is null");
			LOGGER.error(error.getDescription());
			return error;
		}
		if (userCredential.getApiKey() == null) {
			// throw new IllegalArgumentException("API key is null");
		}
		if (userCredential.getDisplayId() == null) {
			Error error = Error.INVALID_PARAM;
			error.setDescription("Display name is null");
			LOGGER.error(error.getDescription());
			return error;
		}
		return Error.SUCCESS;
	}

	public static Error validateBroadcastRequest(final BroadcastRequest broadcastRequest) {
		if (broadcastRequest == null) {
			Error error = Error.INVALID_PARAM;
			error.setDescription("BroadcastRequest is null");
			LOGGER.error(error.getDescription());
			return error;
		}
		Error error = validate(broadcastRequest.getReceiverIds(), "Receiver IDs");
		if(error != Error.SUCCESS) {
			return error;
		}
		error = validate(broadcastRequest.getMessage(), "Message");
		if(error != Error.SUCCESS) {
			return error;
		}
		return validate(broadcastRequest.getSenderId(), "Sender ID");
		
	}

	@SuppressWarnings("rawtypes")
	public static Error validate(final Object object, final String name) {
		if (object == null) {
			Error error = Error.INVALID_PARAM;
			error.setDescription("{0} is null", name);
			LOGGER.error(error.getDescription());
			return error;
		}
		if (object instanceof String) {
			if (((String) object).trim().isEmpty()) {
				Error error = Error.INVALID_PARAM;
				error.setDescription("{0} is empty", name);
				LOGGER.error(error.getDescription());
				return error;
			}
		}
		if (object instanceof Collection) {
			Collection collection = (Collection) object;
			if (collection.isEmpty()) {
				Error error = Error.INVALID_PARAM;
				error.setDescription("{0} collection is empty", name);
				LOGGER.error(error.getDescription());
				return error;
			}
			for (Object element : collection) {
				validate(element, name + " validation: element ");
			}
		}
		if (object instanceof Map) {
			Map map = (Map) object;
			if (map.isEmpty()) {
				Error error = Error.INVALID_PARAM;
				error.setDescription("{0} map is empty", name);
				LOGGER.error(error.getDescription());
				return error;
			}
			for (Object element : map.entrySet()) {
				validate(element, name + " validation: value ");
			}
		}
		return Error.SUCCESS;
	}

	public static Error validateNumber(final long value, final long minValue, final long maxValue, final String name) {
		if (value < minValue) {
			Error error = Error.INVALID_PARAM;
			error.setDescription("{0} cannot be less than {1}", name, minValue);
			LOGGER.error(error.getDescription());
			return error;
		}
		if (maxValue != Long.MAX_VALUE) {
			if (value > maxValue) {
				Error error = Error.INVALID_PARAM;
				error.setDescription("{0} cannot be greater than {1}", name, maxValue);
				LOGGER.error(error.getDescription());
				return error;
			}
		}
		return Error.SUCCESS;
	}

	/**
	 * This validates whether the userId is actually the one who is logged in
	 * 
	 * @param userId
	 */
	public static Error validateUser(final String userId) {
		try {
			Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
			if (authentication != null && authentication.isAuthenticated() && authentication.getPrincipal().equals(userId)) {
				return Error.SUCCESS;
			}
		} catch (Exception e) {
		}
		Error error = Error.USER_LOGGED_OUT;
		error.setDescription("User {0} is not logged in", userId);
		LOGGER.error(error.getDescription());
		return error;
	}
}
