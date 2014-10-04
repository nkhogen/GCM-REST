package com.naorem.khogen.gcm.server.core.impl;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.json.simple.JSONValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.naorem.khogen.gcm.server.core.GCMServiceManager;
import com.naorem.khogen.gcm.server.core.GCMUpstreamMessageListener;
import com.naorem.khogen.gcm.server.domain.dao.MessageDAO;
import com.naorem.khogen.gcm.server.model.dto.Message;
import com.naorem.khogen.gcm.server.model.dto.MessageSelector;
import com.naorem.khogen.gcm.server.model.dto.MessageStatus;
import com.naorem.khogen.server.common.CommonUtil;
import com.naorem.khogen.server.common.Error;
import com.naorem.khogen.server.common.GlobalConstants;
import com.naorem.khogen.server.common.Result;

public class GCMServiceManagerImpl implements GCMUpstreamMessageListener, GCMServiceManager {
	private static final Logger LOGGER = LoggerFactory.getLogger(GCMServiceManagerImpl.class);
	@Autowired
	private GCMUploader gcmUploader;
	@Autowired
	private MessageDAO messageDAO;
	private ExecutorService executorService;

	public void init() {
		this.executorService = Executors.newCachedThreadPool();
	}

	public void destroy() {
		if (executorService != null) {
			try {
				executorService.shutdown();
			} catch (Exception e) {
				LOGGER.error("Failed to shutdown GCM service manager", e);
				e.printStackTrace();
			}
		}
	}

	public Result<Message> send(final String senderId, final String receiverId, final String receiverAddress, final String text) {
		final Message message = new Message();
		message.setReceiverId(receiverId);
		message.setSenderId(senderId);
		message.setId(UUID.randomUUID().toString());
		message.setContent(text);
		message.setTimestamp(System.currentTimeMillis());
		message.setStatus(MessageStatus.DELIVERED);
		String jsonMessage = createDownstreamJsonMessage(receiverAddress, message, null);
		try {
			if (gcmUploader.sendDownstreamMessage(jsonMessage)) {
				executorService.submit(new Runnable() {
					// do async update..
					@Override
					public void run() {
						// retry
						try {
							messageDAO.putMessage(message);
						} catch (Exception e) {
							e.printStackTrace();
						}
					}

				});
				return new Result<Message>(message, Error.SUCCESS);
			}
			Error error = Error.SEND_MESSAGE_ERROR;
			error.setDescription("Error sending message from sender ID {0} to receiver ID {1}", senderId, receiverId);
			LOGGER.error(error.getDescription());
			return new Result<Message>(null, error);
		} catch (Exception e) {
			e.printStackTrace();
			Error error = Error.SEND_MESSAGE_ERROR;
			error.setDescription("Error sending message from sender ID {0} to receiver ID {1}", senderId, receiverId);
			error.setException(e);
			LOGGER.error(error.getDescription(), e);
			return new Result<Message>(null, error);
		}
	}

	// to device
	public static String createDownstreamJsonMessage(final String deviceRegId, final Message message, final Map<String, String> metaData) {
		Map<String, Object> messageMap = new HashMap<String, Object>();
		messageMap.put("to", deviceRegId);
		String collapseKey = String.valueOf(System.currentTimeMillis() / 10);
		long timeToLive = 120L;
		boolean delayWhileIdle = true;
		if (collapseKey != null) {
			messageMap.put("collapse_key", collapseKey);
		}
		messageMap.put("time_to_live", timeToLive);
		if (delayWhileIdle) {
			messageMap.put("delay_while_idle", true);
		}
		messageMap.put("message_id", message.getId().toString());
		Map<String, String> payload = new HashMap<String, String>();
		if (message != null) {
			payload.put(GlobalConstants.MESSAGE_FIELD, CommonUtil.toJson(message));
		}
		if (metaData != null && !metaData.isEmpty()) {
			payload.putAll(metaData);
		}
		messageMap.put("data", payload);
		return JSONValue.toJSONString(messageMap);
	}

	// from device for registration

	@Override
	public void onReceive(Map<String, Object> message) {
		// TODO Auto-generated method stub
	}

	@Override
	public void onAck(Map<String, Object> jsonObject) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onNack(Map<String, Object> jsonObject) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onControl(Map<String, Object> jsonObject) {
		// TODO Auto-generated method stub

	}

	@Override
	public Result<List<Message>> getConversation(final MessageSelector messageSelector, final String userId, final String friendId, final long startTime, final long limit) {
		long actualLimit = limit == 0L? 0L : limit + GlobalConstants.SQL_ROW_LIMIT_PADDING;
		Result<List<Message>> result = messageDAO.getConversation(messageSelector, userId, friendId, startTime, actualLimit);
		if(result.getError() != Error.SUCCESS) {
			return result;
		}
		if(limit == 0L) {
			return result;
		}
		List<Message> responseMessages = new LinkedList<Message>();
		List<Message> messages = result.getValue();
		long count = 0L;
		long lastTimestamp = 0L;
		for(Message message : messages) {
			count++;
			if(count > limit) {
				if(message.getTimestamp() != lastTimestamp) {
					break;
				}
			}
			lastTimestamp = message.getTimestamp();
			responseMessages.add(message);
		}
		return new Result<List<Message>>(responseMessages, Error.SUCCESS);
	}

	@Override
	public Result<List<Message>> getMessages(final MessageSelector messageSelector,final String userId, final long startTime, final long limit) {
		long actualLimit = limit == 0L? 0L : limit + GlobalConstants.SQL_ROW_LIMIT_PADDING;
		Result<List<Message>> result = messageDAO.getMessages(messageSelector, userId, startTime, actualLimit);
		if(result.getError() != Error.SUCCESS) {
			return result;
		}
		if(limit == 0L) {
			return result;
		}
		List<Message> messages = result.getValue();
		List<Message> responseMessages = new LinkedList<Message>();
		long count = 0L;
		long lastTimestamp = 0L;
		for(Message message : messages) {
			count++;
			if(count > limit) {
				if(message.getTimestamp() != lastTimestamp) {
					break;
				}
			}
			lastTimestamp = message.getTimestamp();
			responseMessages.add(message);
		}
		return new Result<List<Message>>(responseMessages, Error.SUCCESS);
	}

	@Override
	public Result<Message> putMessage(final Message message) {
		return messageDAO.putMessage(message);
	}

	@Override
	public Result<Message> deleteMessage(final String userId, final String messageId) {
		return messageDAO.deleteMessage(userId, messageId);
	}

	@Override
	public Result<List<Message>> send(final String senderId, final Map<String, String> receiverAddresses, final String text) {
		final String messageId = UUID.randomUUID().toString();
		final List<Message> messages = new LinkedList<Message>();
		for (Map.Entry<String, String> receiverEntry : receiverAddresses.entrySet()) {
			final Message message = new Message();
			message.setReceiverId(receiverEntry.getKey());
			message.setSenderId(senderId);
			message.setId(messageId);
			message.setContent(text);
			message.setTimestamp(System.currentTimeMillis());
			message.setStatus(MessageStatus.DELIVERED);
			String jsonMessage = createDownstreamJsonMessage(receiverEntry.getValue(), message, null);
			try {
				if (gcmUploader.sendDownstreamMessage(jsonMessage)) {
					messages.add(message);
				} else {
					Error error = Error.SEND_MESSAGE_ERROR;
					error.setDescription("Error sending message from sender ID {0} to receiver IDs {1}", senderId, receiverAddresses.keySet());
					LOGGER.error(error.getDescription());
					return new Result<List<Message>>(messages, error);
				}
			} catch (Exception e) {
				e.printStackTrace();
				Error error = Error.SEND_MESSAGE_ERROR;
				error.setDescription("Error sending message from sender ID {0} to receiver IDs {1}", senderId, receiverAddresses.keySet());
				error.setException(e);
				LOGGER.error(error.getDescription(), e);
				return new Result<List<Message>>(messages, error);
			}
		}
		if (messages.size() > 0) {
			executorService.submit(new Runnable() {
				@Override
				public void run() {
					// retry
					try {
						messageDAO.putMessages(messages);
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			});
		}
		return new Result<List<Message>>(messages, Error.SUCCESS); 
	}
}
