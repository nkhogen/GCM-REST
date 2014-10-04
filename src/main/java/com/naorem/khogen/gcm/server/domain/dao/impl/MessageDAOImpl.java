package com.naorem.khogen.gcm.server.domain.dao.impl;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

import javax.persistence.EntityManager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mysema.query.jpa.impl.JPAQuery;
import com.mysema.query.types.expr.BooleanExpression;
import com.naorem.khogen.gcm.server.domain.dao.MessageDAO;
import com.naorem.khogen.gcm.server.domain.entities.MessageDO;
import com.naorem.khogen.gcm.server.domain.entities.QMessageDO;
import com.naorem.khogen.gcm.server.model.dto.Message;
import com.naorem.khogen.gcm.server.model.dto.MessageSelector;
import com.naorem.khogen.gcm.server.model.dto.MessageStatus;
import com.naorem.khogen.server.common.CommonUtil;
import com.naorem.khogen.server.common.Error;
import com.naorem.khogen.server.common.Result;

public class MessageDAOImpl implements MessageDAO {
	private static final Logger LOGGER = LoggerFactory.getLogger(MessageDAOImpl.class);

	@Override
	public Result<List<Message>> getConversation(final MessageSelector messageSelector, final String userId, final String friendId, final long startTime, final long limit) {
		List<Message> messages = new LinkedList<Message>();
		EntityManager entityManager = CommonUtil.getEntityManager();
		try {
			JPAQuery query = new JPAQuery(entityManager).from(QMessageDO.messageDO);
			BooleanExpression predicate = null;
			if (messageSelector == MessageSelector.RECEIVE) {
				predicate = QMessageDO.messageDO.receiverId.eq(userId);
				predicate = predicate.and(QMessageDO.messageDO.senderId.eq(friendId));
				predicate = predicate.and(QMessageDO.messageDO.timestamp.lt(startTime));
			} else if (messageSelector == MessageSelector.SEND) {
				predicate = QMessageDO.messageDO.receiverId.eq(friendId);
				predicate = predicate.and(QMessageDO.messageDO.senderId.eq(userId));
				predicate = predicate.and(QMessageDO.messageDO.timestamp.lt(startTime));
			} else {
				BooleanExpression firstPredicate = QMessageDO.messageDO.receiverId.eq(friendId);
				firstPredicate = firstPredicate.and(QMessageDO.messageDO.senderId.eq(userId));
				firstPredicate = firstPredicate.and(QMessageDO.messageDO.timestamp.lt(startTime));
				BooleanExpression secondPredicate = QMessageDO.messageDO.receiverId.eq(userId);
				secondPredicate = secondPredicate.and(QMessageDO.messageDO.senderId.eq(friendId));
				secondPredicate = secondPredicate.and(QMessageDO.messageDO.timestamp.lt(startTime));
				predicate = firstPredicate.or(secondPredicate);
			}
			query = query.where(predicate).orderBy(QMessageDO.messageDO.timestamp.desc());
			if (limit > 0L) {
				query = query.limit(limit);
			}
			List<MessageDO> messageDOs = query.list(QMessageDO.messageDO);
			for (MessageDO messageDO : messageDOs) {
				messages.add(getMessage(messageDO));
			}
			return new Result<List<Message>>(messages, Error.SUCCESS);
		} catch (Exception e) {
			e.printStackTrace();
			Error error = Error.DATABASE_ERROR;
			error.setDescription("Failed in database call for user ID {0} and friend ID {1}", userId, friendId);
			error.setException(e);
			LOGGER.error(error.getDescription(), e);
			return new Result<List<Message>>(null, error);
		} finally {
			entityManager.close();
		}
	}

	@Override
	public Result<List<Message>> getMessages(final MessageSelector messageSelector, final String userId, final long startTime, final long limit) {
		List<Message> messages = new LinkedList<Message>();
		EntityManager entityManager = CommonUtil.getEntityManager();
		try {
			JPAQuery query = new JPAQuery(entityManager).from(QMessageDO.messageDO);
			BooleanExpression expression = null;
			if (messageSelector == MessageSelector.RECEIVE) {
				expression = QMessageDO.messageDO.receiverId.eq(userId).and(QMessageDO.messageDO.timestamp.lt(startTime));
			} else if (messageSelector == MessageSelector.SEND) {
				expression = QMessageDO.messageDO.senderId.eq(userId).and(QMessageDO.messageDO.timestamp.lt(startTime));
			} else {
				expression = QMessageDO.messageDO.receiverId.eq(userId).and(QMessageDO.messageDO.timestamp.lt(startTime)).or(QMessageDO.messageDO.senderId.eq(userId))
						.and(QMessageDO.messageDO.timestamp.lt(startTime));
			}
			query = query.where(expression).orderBy(QMessageDO.messageDO.timestamp.desc());
			if (limit > 0L) {
				query = query.limit(limit);
			}
			List<MessageDO> messageDOs = query.list(QMessageDO.messageDO);
			for (MessageDO messageDO : messageDOs) {
				messages.add(getMessage(messageDO));
			}
			return new Result<List<Message>>(messages, Error.SUCCESS);
		} catch (Exception e) {
			e.printStackTrace();
			Error error = Error.DATABASE_ERROR;
			error.setDescription("Failed in database call for user ID {0}", userId);
			error.setException(e);
			LOGGER.error(error.getDescription(), e);
			return new Result<List<Message>>(null, error);
		} finally {
			entityManager.close();
		}
	}

	@Override
	public Result<Message> putMessage(final Message message) {
		MessageDO messageDO = mergeMessageDO(null, message);
		EntityManager entityManager = CommonUtil.getEntityManager();
		try {
			entityManager.getTransaction().begin();
			entityManager.persist(messageDO);
			entityManager.getTransaction().commit();
			return new Result<Message>(message, Error.SUCCESS);
		} catch (Exception e) {
			LOGGER.error("Failed to insert message: " + message, e);
			e.printStackTrace();
			if (entityManager.getTransaction().isActive()) {
				entityManager.getTransaction().rollback();
			}
			Error error = Error.DATABASE_ERROR;
			error.setDescription("Failed in database call for message ID {0}, sender ID {1}, receiver ID {2}", message.getId(), message.getSenderId(), message.getReceiverId());
			error.setException(e);
			LOGGER.error(error.getDescription(), e);
			return new Result<Message>(null, error);
		} finally {
			entityManager.close();
		}
	}

	@Override
	public Result<Collection<Message>> putMessages(final Collection<Message> messages) {
		EntityManager entityManager = CommonUtil.getEntityManager();
		try {
			entityManager.getTransaction().begin();
			for (Message message : messages) {
				MessageDO messageDO = mergeMessageDO(null, message);
				entityManager.persist(messageDO);
				entityManager.flush();
			}
			entityManager.getTransaction().commit();
			return new Result<Collection<Message>>(messages, Error.SUCCESS);
		} catch (Exception e) {
			e.printStackTrace();
			if (entityManager.getTransaction().isActive()) {
				entityManager.getTransaction().rollback();
			}
			Error error = Error.DATABASE_ERROR;
			error.setDescription("Failed in database call for messages");
			error.setException(e);
			LOGGER.error(error.getDescription(), e);
			return new Result<Collection<Message>>(null, error);
		} finally {
			entityManager.close();
		}
	}

	@Override
	public Result<Message> deleteMessage(final String userId, final String messageId) {
		EntityManager entityManager = CommonUtil.getEntityManager();
		try {
			JPAQuery query = new JPAQuery(entityManager).from(QMessageDO.messageDO);
			query = query.where(QMessageDO.messageDO.receiverId.eq(userId).and(QMessageDO.messageDO.id.eq(messageId))
					.or(QMessageDO.messageDO.senderId.eq(userId).and(QMessageDO.messageDO.id.eq(messageId))));
			List<MessageDO> messageDOs = query.list(QMessageDO.messageDO);
			if (messageDOs.isEmpty()) {
				return new Result<Message>(null, Error.MESSAGE_NOT_FOUND);
			}
			entityManager.getTransaction().begin();
			entityManager.remove(messageDOs.get(0));
			entityManager.getTransaction().commit();
			return new Result<Message>(getMessage(messageDOs.get(0)), Error.SUCCESS);
		} catch (Exception e) {
			e.printStackTrace();
			if (entityManager.getTransaction().isActive()) {
				entityManager.getTransaction().rollback();
			}
			Error error = Error.DATABASE_ERROR;
			error.setDescription("Failed in database call for message ID {0} and user ID {1}", messageId, userId);
			error.setException(e);
			LOGGER.error(error.getDescription(), e);
			return new Result<Message>(null, error);
		} finally {
			entityManager.close();
		}
	}

	private MessageDO mergeMessageDO(final MessageDO currentMessageDO, final Message message) {
		MessageDO messageDO = null;
		if (currentMessageDO == null) {
			messageDO = new MessageDO();
			messageDO.setReceiverId(message.getReceiverId().trim().toLowerCase());
			messageDO.setSenderId(message.getSenderId().trim().toLowerCase());
			messageDO.setTimestamp(message.getTimestamp());
			messageDO.setContent(message.getContent().trim());
			messageDO.setStatus(message.getStatus().name());
			messageDO.setId(UUID.randomUUID().toString());
			messageDO.setLockVersion(0L);
		} else {
			messageDO = currentMessageDO;
			messageDO.setReceiverId(message.getReceiverId().trim().toLowerCase());
			messageDO.setSenderId(message.getSenderId().trim().toLowerCase());
			messageDO.setTimestamp(message.getTimestamp());
			messageDO.setContent(message.getContent().trim());
			messageDO.setStatus(message.getStatus().name());
		}
		return messageDO;
	}

	private Message getMessage(final MessageDO messageDO) {
		Message message = new Message();
		message.setReceiverId(messageDO.getReceiverId());
		message.setSenderId(messageDO.getSenderId());
		message.setId(messageDO.getId());
		message.setContent(messageDO.getContent());
		message.setTimestamp(messageDO.getTimestamp());
		message.setStatus(MessageStatus.getMessageStatus(messageDO.getStatus()));
		return message;
	}
}
