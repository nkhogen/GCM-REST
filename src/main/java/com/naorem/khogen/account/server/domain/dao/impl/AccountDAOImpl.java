package com.naorem.khogen.account.server.domain.dao.impl;

import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

import javax.persistence.EntityManager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mysema.query.jpa.impl.JPAQuery;
import com.naorem.khogen.account.server.domain.dao.AccountDAO;
import com.naorem.khogen.account.server.domain.entities.AccountDO;
import com.naorem.khogen.account.server.domain.entities.FriendDO;
import com.naorem.khogen.account.server.domain.entities.QAccountDO;
import com.naorem.khogen.account.server.domain.entities.QFriendDO;
import com.naorem.khogen.account.server.model.dto.Account;
import com.naorem.khogen.account.server.model.dto.Friend;
import com.naorem.khogen.account.server.model.dto.FriendStatus;
import com.naorem.khogen.account.server.model.dto.UserCredential;
import com.naorem.khogen.server.common.CommonUtil;
import com.naorem.khogen.server.common.Error;
import com.naorem.khogen.server.common.Result;

public class AccountDAOImpl implements AccountDAO {
	private static final Logger LOGGER = LoggerFactory.getLogger(AccountDAOImpl.class);

	@Override
	public Result<Account> getAccount(final String userId) {
		EntityManager entityManager = CommonUtil.getEntityManager();
		try {
			JPAQuery query = new JPAQuery(entityManager).from(QAccountDO.accountDO);
			query = query.where(QAccountDO.accountDO.userId.eq(userId));
			List<AccountDO> accountDOs = query.list(QAccountDO.accountDO);
			return new Result<Account>(accountDOs.isEmpty() ? null : getAccount(accountDOs.get(0)), Error.SUCCESS);
		} catch (Exception e) {
			e.printStackTrace();
			Error error = Error.DATABASE_ERROR;
			error.setDescription(e.getMessage());
			return new Result<Account>(null, error);
		} finally {
			entityManager.close();
		}
	}

	@Override
	public Result<Account> createAccount(final Account account) {
		EntityManager entityManager = CommonUtil.getEntityManager();
		try {
			JPAQuery query = new JPAQuery(entityManager).from(QAccountDO.accountDO);
			query = query.where(QAccountDO.accountDO.userId.eq(account.getCredential().getId()));
			List<AccountDO> accountDOs = query.list(QAccountDO.accountDO);
			if (accountDOs.isEmpty()) {
				AccountDO accountDO = mergeAccountDO(null, account);
				entityManager.getTransaction().begin();
				entityManager.merge(accountDO);
				entityManager.getTransaction().commit();
				return new Result<Account>(getAccount(accountDO), Error.SUCCESS);
			} else {
				Error error = Error.ACCOUNT_ALREADY_EXIST;
				error.setDescription("Account ID {0} already exists", account.getCredential().getId());
				return new Result<Account>(null, error);
			}
		} catch (Exception e) {
			e.printStackTrace();
			if (entityManager.getTransaction().isActive()) {
				entityManager.getTransaction().rollback();
			}
			Error error = Error.DATABASE_ERROR;
			error.setDescription("Failed to update account for ID: {0}", account.getCredential().getId());
			error.setException(e);
			LOGGER.error(error.getDescription(), e);
			return new Result<Account>(null, error);
		} finally {
			entityManager.close();
		}
	}

	@Override
	public Result<Account> updateAccount(final Account account) {
		EntityManager entityManager = CommonUtil.getEntityManager();
		try {
			JPAQuery query = new JPAQuery(entityManager).from(QAccountDO.accountDO);
			query = query.where(QAccountDO.accountDO.userId.eq(account.getCredential().getId()));
			List<AccountDO> accountDOs = query.list(QAccountDO.accountDO);
			if (accountDOs.isEmpty()) {
				Error error = Error.ACCOUNT_NOT_FOUND;
				error.setDescription("Account ID {0} is not found", account.getCredential().getId());
				return new Result<Account>(null, error);
			}
			AccountDO accountDO = mergeAccountDO(accountDOs.get(0), account);
			entityManager.getTransaction().begin();
			entityManager.merge(accountDO);
			entityManager.getTransaction().commit();
			return new Result<Account>(getAccount(accountDO), Error.SUCCESS);
		} catch (Exception e) {
			e.printStackTrace();
			if (entityManager.getTransaction().isActive()) {
				entityManager.getTransaction().rollback();
			}
			Error error = Error.DATABASE_ERROR;
			error.setDescription("Failed in database call for account ID {0}", account.getCredential().getId());
			error.setException(e);
			LOGGER.error(error.getDescription(), e);
			return new Result<Account>(null, error);
		} finally {
			entityManager.close();
		}
	}

	@Override
	public Result<Account> deleteAccount(final String userId) {
		EntityManager entityManager = CommonUtil.getEntityManager();
		try {
			JPAQuery query = new JPAQuery(entityManager).from(QAccountDO.accountDO);
			query = query.where(QAccountDO.accountDO.userId.eq(userId));
			List<AccountDO> accountDOs = query.list(QAccountDO.accountDO);
			if (accountDOs.isEmpty()) {
				Error error = Error.ACCOUNT_NOT_FOUND;
				error.setDescription("Account ID {0} is not found", userId);
				return new Result<Account>(null, error);
			}
			AccountDO accountDO = accountDOs.get(0);
			query = new JPAQuery(entityManager).from(QFriendDO.friendDO);
			query = query.where(QFriendDO.friendDO.userId.eq(accountDO.getUserId()).or(QFriendDO.friendDO.friendId.eq(accountDO.getUserId())));
			List<FriendDO> friendDOs = query.list(QFriendDO.friendDO);
			entityManager.getTransaction().begin();
			for(FriendDO friendDO : friendDOs) {
				entityManager.remove(friendDO);
			}
			entityManager.remove(accountDO);
			entityManager.getTransaction().commit();
			return new Result<Account>(getAccount(accountDOs.get(0)), Error.SUCCESS);
		} catch (Exception e) {
			LOGGER.error("Failed to delete account for ID: " + userId, e);
			e.printStackTrace();
			if (entityManager.getTransaction().isActive()) {
				entityManager.getTransaction().rollback();
			}
			Error error = Error.DATABASE_ERROR;
			error.setDescription("Failed in database call for account ID {0}", userId);
			error.setException(e);
			LOGGER.error(error.getDescription(), e);
			return new Result<Account>(null, error);
		} finally {
			entityManager.close();
		}
	}

	@Override
	public Result<Friend> friend(final String userId, final String friendId) {
		String firstUserId = null;
		String secondUserId = null;
		FriendStatus friendStatus = FriendStatus.SENT;
		if (userId.compareToIgnoreCase(friendId) < 0) {
			firstUserId = userId;
			secondUserId = friendId;
			friendStatus = FriendStatus.SENT;
		} else {
			firstUserId = friendId;
			secondUserId = userId;
			friendStatus = FriendStatus.RECEIVED;
		}
		EntityManager entityManager = CommonUtil.getEntityManager();
		try {
			JPAQuery query = new JPAQuery(entityManager).from(QFriendDO.friendDO);
			query = query.where(QFriendDO.friendDO.userAccountDO.userId.eq(firstUserId).and(QFriendDO.friendDO.friendAccountDO.userId.eq(secondUserId)));
			List<FriendDO> friendDOs = query.list(QFriendDO.friendDO);
			FriendDO friendDO = mergeFriendDO(friendDOs.isEmpty() ? null : friendDOs.get(0), firstUserId, secondUserId, friendStatus.toString());
			entityManager.getTransaction().begin();
			entityManager.merge(friendDO);
			entityManager.getTransaction().commit();
			return new Result<Friend>(getFriend(friendDO, userId), Error.SUCCESS);
		} catch (Exception e) {
			e.printStackTrace();
			if (entityManager.getTransaction().isActive()) {
				entityManager.getTransaction().rollback();
			}
			Error error = Error.DATABASE_ERROR;
			error.setDescription("Failed in database call for account ID {0} and friend ID {1}", userId, friendId);
			error.setException(e);
			LOGGER.error(error.getDescription(), e);
			return new Result<Friend>(null, error);
		} finally {
			entityManager.close();
		}
	}

	@Override
	public Result<Friend> unfriend(final String userId, final String friendId) {
		String firstUserId = null;
		String secondUserId = null;
		if (userId.compareToIgnoreCase(friendId) < 0) {
			firstUserId = userId;
			secondUserId = friendId;
		} else {
			firstUserId = friendId;
			secondUserId = userId;
		}
		EntityManager entityManager = CommonUtil.getEntityManager();
		try {
			JPAQuery query = new JPAQuery(entityManager).from(QFriendDO.friendDO);
			query = query.where(QFriendDO.friendDO.userId.eq(firstUserId).and(QFriendDO.friendDO.friendId.eq(secondUserId)));
			List<FriendDO> friendDOs = query.list(QFriendDO.friendDO);
			if (friendDOs.isEmpty()) {
				Error error = Error.ACCOUNT_NOT_FOUND;
				error.setDescription("Friend account for user ID {0} and friend ID {1} is not found", userId, friendId);
				return new Result<Friend>(null, error);
			}
			entityManager.getTransaction().begin();
			entityManager.remove(friendDOs.get(0));
			entityManager.getTransaction().commit();
			return new Result<Friend>(getFriend(friendDOs.get(0), userId), Error.SUCCESS);
		} catch (Exception e) {
			e.printStackTrace();
			if (entityManager.getTransaction().isActive()) {
				entityManager.getTransaction().rollback();
			}
			Error error = Error.DATABASE_ERROR;
			error.setDescription("Failed in database call for account ID {0} and friend ID {1}", userId, friendId);
			error.setException(e);
			LOGGER.error(error.getDescription(), e);
			return new Result<Friend>(null, error);
		} finally {
			entityManager.close();
		}
	}

	@Override
	public Result<List<Friend>> getFriends(final String userId) {
		List<Friend> friends = new LinkedList<Friend>();
		EntityManager entityManager = CommonUtil.getEntityManager();
		try {
			JPAQuery query = new JPAQuery(entityManager).from(QFriendDO.friendDO);
			query = query.where(QFriendDO.friendDO.userId.eq(userId).or(QFriendDO.friendDO.friendId.eq(userId)));
			List<FriendDO> friendDOs = query.list(QFriendDO.friendDO);
			for (FriendDO friendDO : friendDOs) {
				friends.add(getFriend(friendDO, userId));
			}
			return new Result<List<Friend>>(friends, Error.SUCCESS);
		} catch (Exception e) {
			e.printStackTrace();
			Error error = Error.DATABASE_ERROR;
			error.setDescription("Failed in database call for account ID {0}", userId);
			error.setException(e);
			LOGGER.error(error.getDescription(), e);
			return new Result<List<Friend>>(null, error);
		} finally {
			entityManager.close();
		}
	}

	@Override
	public Result<List<Friend>> getFriends(final String userId, final List<String> receiverIds) {
		List<Friend> filteredFriends = new LinkedList<Friend>();
		Result<List<Friend>> result = getFriends(userId);
		if (result.getError() == Error.SUCCESS) {
			List<Friend> friends = result.getValue();
			for (String receiverId : receiverIds) {
				for (Friend friend : friends) {
					if (friend.getFriendCredential().getId().equals(receiverId)) {
						filteredFriends.add(friend);
					}
				}
			}
			return new Result<List<Friend>>(filteredFriends, Error.SUCCESS);
		}
		return result;
	}

	@Override
	public Result<List<Account>> getAccounts(final List<String> userIds) {
		List<Account> accounts = new LinkedList<Account>();
		EntityManager entityManager = CommonUtil.getEntityManager();
		try {
			JPAQuery query = new JPAQuery(entityManager).from(QAccountDO.accountDO);
			query = query.where(QAccountDO.accountDO.userId.in(userIds));
			List<AccountDO> accountDOs = query.list(QAccountDO.accountDO);
			for (AccountDO accountDO : accountDOs) {
				Account account = new Account();
				account.setCredential(getUserCredential(accountDO));
				accounts.add(account);
			}
			return new Result<List<Account>>(accounts, Error.SUCCESS);
		} catch (Exception e) {
			e.printStackTrace();
			Error error = Error.DATABASE_ERROR;
			error.setDescription("Failed in database call for account IDs {0}", userIds);
			error.setException(e);
			LOGGER.error(error.getDescription(), e);
			return new Result<List<Account>>(null, error);
		} finally {
			entityManager.close();
		}
	}

	private UserCredential getUserCredential(final AccountDO accountDO) {
		UserCredential userCredential = new UserCredential();
		userCredential.setId(accountDO.getUserId());
		userCredential.setApiKey(accountDO.getApiKey());
		userCredential.setAddress(accountDO.getAddress());
		userCredential.setDisplayId(accountDO.getUsername());
		userCredential.setPassword(accountDO.getPassword());
		return userCredential;
	}

	private AccountDO mergeAccountDO(final AccountDO currentAccountDO, final Account account) {
		AccountDO accountDO = null;
		if (currentAccountDO == null) {
			accountDO = new AccountDO();
			accountDO.setAddress(account.getCredential().getAddress().trim());
			accountDO.setApiKey(account.getCredential().getApiKey());
			accountDO.setUserId(account.getCredential().getId().trim().toLowerCase());
			// TODO check for update of fields in Biz layer?
			accountDO.setPassword(account.getCredential().getPassword().trim());
			accountDO.setUsername(account.getCredential().getDisplayId().trim());
			accountDO.setId(UUID.randomUUID().toString());
			long time = System.currentTimeMillis();
			accountDO.setCreatedTimestamp(time);
			accountDO.setModifiedTimestamp(time);
			accountDO.setLockVersion(0L);
		} else {
			accountDO = currentAccountDO;
			accountDO.setAddress(account.getCredential().getAddress().trim());
			accountDO.setApiKey(account.getCredential().getApiKey());
			accountDO.setUserId(account.getCredential().getId().trim().toLowerCase());
			// TODO check for update of fields in Biz layer?
			accountDO.setPassword(account.getCredential().getPassword().trim());
			accountDO.setUsername(account.getCredential().getDisplayId().trim());
			// let it generate by DB
			accountDO.setModifiedTimestamp(System.currentTimeMillis());
		}
		return accountDO;
	}

	private FriendDO mergeFriendDO(final FriendDO existingFriendDO, final String firstUser, final String secondUser, final String status) {
		FriendDO friendDO = null;
		if (existingFriendDO == null) {
			friendDO = new FriendDO();
			friendDO.setId(UUID.randomUUID().toString());
			friendDO.setLockVersion(0L);
			friendDO.setUserId(firstUser.trim().toLowerCase());
			friendDO.setFriendId(secondUser.trim().toLowerCase());
			long time = System.currentTimeMillis();
			friendDO.setCreatedTimestamp(time);
			friendDO.setModifiedTimestamp(time);
			friendDO.setStatus(status);
		} else {
			friendDO = existingFriendDO;
			String existingStatus = friendDO.getStatus();
			if (!existingStatus.equals(status)) {
				friendDO.setStatus(FriendStatus.ACTIVE.toString());
			}
		}
		return friendDO;
	}

	private Account getAccount(final AccountDO accountDO) {
		Account account = new Account();
		account.setCredential(getUserCredential(accountDO));
		return account;
	}

	private Friend getFriend(final FriendDO friendDO, final String userId) {
		Friend friend = new Friend();
		FriendStatus friendStatus = FriendStatus.getFriendStatus(friendDO.getStatus());
		if (friendDO.getUserId().equals(userId)) {
			friend.setUserCredential(getUserCredential(friendDO.getUserAccountDO()));
			friend.setFriendCredential(getUserCredential(friendDO.getFriendAccountDO()));
			friend.setStatus(friendStatus);

		} else {
			friend.setUserCredential(getUserCredential(friendDO.getFriendAccountDO()));
			friend.setFriendCredential(getUserCredential(friendDO.getUserAccountDO()));
			if (friendStatus == FriendStatus.RECEIVED) {
				friend.setStatus(FriendStatus.SENT);
			} else if (friendStatus == FriendStatus.SENT) {
				friend.setStatus(FriendStatus.RECEIVED);
			} else {
				friend.setStatus(FriendStatus.ACTIVE);
			}
		}
		return friend;
	}
}
