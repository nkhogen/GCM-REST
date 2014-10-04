package com.naorem.khogen.gcm.server.domain.dao;

import java.util.Collection;
import java.util.List;

import com.naorem.khogen.gcm.server.model.dto.Message;
import com.naorem.khogen.gcm.server.model.dto.MessageSelector;
import com.naorem.khogen.server.common.Result;

public interface MessageDAO {
	Result<List<Message>> getConversation(final MessageSelector messageSelector, final String userId, final String friendId, final long startTime, final long endTime);
	Result<List<Message>> getMessages(final MessageSelector messageSelector, final String userId, final long startTime, final long endTime);
	Result<Message> putMessage(final Message message);
	Result<Collection<Message>> putMessages(final Collection<Message> messages);
	Result<Message> deleteMessage(final String userId, final String messageId);
}
