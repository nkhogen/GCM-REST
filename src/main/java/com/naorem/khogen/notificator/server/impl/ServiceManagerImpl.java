package com.naorem.khogen.notificator.server.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.naorem.khogen.account.server.core.AccountManager;
import com.naorem.khogen.account.server.model.dto.Account;
import com.naorem.khogen.account.server.model.dto.Friend;
import com.naorem.khogen.account.server.model.dto.FriendStatus;
import com.naorem.khogen.gcm.server.core.GCMServiceManager;
import com.naorem.khogen.gcm.server.model.dto.BroadcastRequest;
import com.naorem.khogen.gcm.server.model.dto.Message;
import com.naorem.khogen.gcm.server.model.dto.MessageSelector;
import com.naorem.khogen.notificator.server.ServiceManager;
import com.naorem.khogen.server.common.Error;
import com.naorem.khogen.server.common.Result;
import com.naorem.khogen.server.common.ValidationUtil;

// Authentication is done in the interceptor filter using spring security

public class ServiceManagerImpl implements ServiceManager {
	private static final Logger LOGGER = LoggerFactory.getLogger(ServiceManagerImpl.class);
	@Autowired
	private AccountManager accountManager;
	@Autowired
	private GCMServiceManager gcmServiceManager;
	@Context
	private HttpHeaders headers;
	@Context
	private HttpServletResponse response;

	@Override
	public Result<Account> getAccount(final String userId) {
		Error error = ValidationUtil.validate(userId, "User ID");
		if (error != Error.SUCCESS) {
			return new Result<Account>(null, error);
		}
		error = ValidationUtil.validateUser(userId);
		if (error != Error.SUCCESS) {
			return new Result<Account>(null, error);
		}
		return accountManager.getAccount(userId, true);
	}

	@Override
	public Result<Account> createAccount(final Account account) {
		Error error = ValidationUtil.validateAccount(account);
		if (error != Error.SUCCESS) {
			return new Result<Account>(null, error);
		}
		return accountManager.createAccount(account);
	}

	@Override
	public Result<Account> updateAccount(final Account account) {
		Error error = ValidationUtil.validateAccount(account);
		if (error != Error.SUCCESS) {
			return new Result<Account>(null, error);
		}
		error = ValidationUtil.validateUser(account.getCredential().getId());
		if (error != Error.SUCCESS) {
			return new Result<Account>(null, error);
		}
		return accountManager.updateAccount(account);
	}

	@Override
	public Result<Account> deleteAccount(final String userId) {
		Error error = ValidationUtil.validate(userId, "User ID");
		if (error != Error.SUCCESS) {
			return new Result<Account>(null, error);
		}
		error = ValidationUtil.validateUser(userId);
		if (error != Error.SUCCESS) {
			return new Result<Account>(null, error);
		}
		return accountManager.deleteAccount(userId);
	}

	@Override
	public Result<Friend> friend(final String userId, final String friendId) {
		Error error = ValidationUtil.validate(userId, "User ID");
		if (error != Error.SUCCESS) {
			return new Result<Friend>(null, error);
		}
		error = ValidationUtil.validateUser(userId);
		if (error != Error.SUCCESS) {
			return new Result<Friend>(null, error);
		}
		return accountManager.friend(userId, friendId);
	}

	@Override
	public Result<Friend> unfriend(final String userId, final String friendId) {
		Error error = ValidationUtil.validate(userId, "User ID");
		if (error != Error.SUCCESS) {
			return new Result<Friend>(null, error);
		}
		error = ValidationUtil.validateUser(userId);
		if (error != Error.SUCCESS) {
			return new Result<Friend>(null, error);
		}
		return accountManager.unfriend(userId, friendId);
	}

	@Override
	public Result<List<Friend>> getFriends(final String userId) {
		Error error = ValidationUtil.validate(userId, "User ID");
		if (error != Error.SUCCESS) {
			return new Result<List<Friend>>(null, error);
		}
		error = ValidationUtil.validateUser(userId);
		if (error != Error.SUCCESS) {
			return new Result<List<Friend>>(null, error);
		}
		return accountManager.getFriends(userId);
	}

	@Override
	public Result<Message> send(final String senderId, final String receiverId, final String text) {
		Error error = ValidationUtil.validate(senderId, "User ID");
		if (error != Error.SUCCESS) {
			return new Result<Message>(null, error);
		}
		error = ValidationUtil.validate(receiverId, "Receiver ID");
		if (error != Error.SUCCESS) {
			return new Result<Message>(null, error);
		}
		error = ValidationUtil.validate(text, "Message");
		if (error != Error.SUCCESS) {
			return new Result<Message>(null, error);
		}
		error = ValidationUtil.validateUser(senderId);
		if (error != Error.SUCCESS) {
			return new Result<Message>(null, error);
		}
		if (senderId.equals(receiverId)) {
			Result<Account> getResult = accountManager.getAccount(senderId, true);
			if (getResult.getError() != Error.SUCCESS) {
				return new Result<Message>(null, getResult.getError());
			}
			Result<Message> sendResult = gcmServiceManager.send(senderId, receiverId, getResult.getValue().getCredential().getAddress(), text);
			return sendResult;
		} else {
			List<String> receiverIds = new LinkedList<String>();
			receiverIds.add(receiverId);
			Result<List<Friend>> result = accountManager.getFriends(senderId, receiverIds);
			if (result.getError() != Error.SUCCESS) {
				return new Result<Message>(null, result.getError());
			}
			List<Friend> friends = result.getValue();
			if (friends.size() > 0) {
				Friend friend = friends.get(0);
				if (friend.getStatus() == FriendStatus.ACTIVE) {
					Result<Message> sendResult = gcmServiceManager.send(senderId, receiverId, friend.getFriendCredential().getAddress(), text);
					return sendResult;
				}
			}
		}
		error = Error.SEND_MESSAGE_ERROR;
		error.setDescription("Error sending message from sender ID {0} to receiver ID {1}. Inactive friendship", senderId, receiverId);
		LOGGER.error(error.getDescription());
		return new Result<Message>(null, error);
	}

	@Override
	public Result<List<Message>> send(final BroadcastRequest broadcastRequest) {
		Error error = ValidationUtil.validateBroadcastRequest(broadcastRequest);
		if (error != Error.SUCCESS) {
			return new Result<List<Message>>(null, error);
		}
		error = ValidationUtil.validateUser(broadcastRequest.getSenderId());
		if (error != Error.SUCCESS) {
			return new Result<List<Message>>(null, error);
		}
		List<String> receiverIds = new ArrayList<String>(broadcastRequest.getReceiverIds().size());
		Map<String, String> receiverAddresses = new HashMap<String, String>();
		for (String receiverId : broadcastRequest.getReceiverIds()) {
			if (broadcastRequest.getSenderId().equals(receiverId)) {
				Result<Account> getResult = accountManager.getAccount(broadcastRequest.getSenderId(), true);
				if (getResult.getError() != Error.SUCCESS) {
					return new Result<List<Message>>(null, getResult.getError());
				}
				receiverAddresses.put(receiverId, getResult.getValue().getCredential().getAddress());
			} else {
				receiverIds.add(receiverId);
			}
		}
		if (receiverIds.size() > 0) {
			Result<List<Friend>> result = accountManager.getFriends(broadcastRequest.getSenderId(), receiverIds);
			if (result.getError() != Error.SUCCESS) {
				return new Result<List<Message>>(null, result.getError());
			}
			List<Friend> friends = result.getValue();
			if (friends.size() > 0) {
				for (Friend friend : friends) {
					if (friend.getStatus() == FriendStatus.ACTIVE) {
						receiverAddresses.put(friend.getFriendCredential().getId(), friend.getFriendCredential().getAddress());
					}
				}
			}
		}
		return gcmServiceManager.send(broadcastRequest.getSenderId(), receiverAddresses, broadcastRequest.getMessage());
	}

	@Override
	public Result<List<Message>> getConversation(final String userId, final String friendId, final long startTime, final long limit) {
		Error error = ValidationUtil.validate(userId, "User ID");
		if (error != Error.SUCCESS) {
			return new Result<List<Message>>(null, error);
		}
		error = ValidationUtil.validate(friendId, "Friend ID");
		if (error != Error.SUCCESS) {
			return new Result<List<Message>>(null, error);
		}
		error = ValidationUtil.validateNumber(startTime, 0L, Long.MAX_VALUE, "Start time");
		if (error != Error.SUCCESS) {
			return new Result<List<Message>>(null, error);
		}
		error = ValidationUtil.validateNumber(limit, 0L, Long.MAX_VALUE, "Row limit");
		if (error != Error.SUCCESS) {
			return new Result<List<Message>>(null, error);
		}
		error = ValidationUtil.validateUser(userId);
		if (error != Error.SUCCESS) {
			return new Result<List<Message>>(null, error);
		}
		return gcmServiceManager.getConversation(MessageSelector.SEND_RECEIVE, userId, friendId, startTime, limit);
	}

	@Override
	public Result<List<Message>> getMessages(final String userId, final long startTime, final long limit) {
		Error error = ValidationUtil.validate(userId, "User ID");
		if (error != Error.SUCCESS) {
			return new Result<List<Message>>(null, error);
		}
		error = ValidationUtil.validateNumber(startTime, 0L, Long.MAX_VALUE, "Start time");
		if (error != Error.SUCCESS) {
			return new Result<List<Message>>(null, error);
		}
		error = ValidationUtil.validateNumber(limit, 0L, Long.MAX_VALUE, "Row limit");
		if (error != Error.SUCCESS) {
			return new Result<List<Message>>(null, error);
		}
		error = ValidationUtil.validateUser(userId);
		if (error != Error.SUCCESS) {
			return new Result<List<Message>>(null, error);
		}
		return gcmServiceManager.getMessages(MessageSelector.SEND_RECEIVE, userId, startTime, limit);
	}

	@Override
	public Result<Message> deleteMessage(final String userId, final String messageId) {
		Error error = ValidationUtil.validate(userId, "User ID");
		if (error != Error.SUCCESS) {
			return new Result<Message>(null, error);
		}
		error = ValidationUtil.validate(messageId, "Message ID");
		if (error != Error.SUCCESS) {
			return new Result<Message>(null, error);
		}
		error = ValidationUtil.validateUser(userId);
		if (error != Error.SUCCESS) {
			return new Result<Message>(null, error);
		}
		return gcmServiceManager.deleteMessage(userId, messageId);
	}
}
