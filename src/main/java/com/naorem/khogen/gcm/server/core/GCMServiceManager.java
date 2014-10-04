package com.naorem.khogen.gcm.server.core;

import java.util.List;
import java.util.Map;

import com.naorem.khogen.gcm.server.model.dto.Message;
import com.naorem.khogen.gcm.server.model.dto.MessageSelector;
import com.naorem.khogen.server.common.Result;

public interface GCMServiceManager {
	/* GCM push */
	Result<Message> send(final String senderId, final String receiverId, final String receiverAddress, final String text);
	Result<List<Message>> send(final String senderId, final Map<String, String> receiverAddresses, final String text);
	/* Offline access */
	Result<List<Message>> getConversation(final MessageSelector messageSelector, final String userId, final String friendId, final long startTime, final long limit);
	Result<List<Message>> getMessages(final MessageSelector messageSelector, final String userId, final long startTime, final long limit);
	Result<Message> putMessage(final Message message);
	Result<Message> deleteMessage(final String userId, final String messageId);
}
