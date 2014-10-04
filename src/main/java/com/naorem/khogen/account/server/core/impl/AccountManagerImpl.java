package com.naorem.khogen.account.server.core.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;

import com.naorem.khogen.account.server.core.AccountManager;
import com.naorem.khogen.account.server.domain.dao.AccountDAO;
import com.naorem.khogen.account.server.model.dto.Account;
import com.naorem.khogen.account.server.model.dto.Friend;
import com.naorem.khogen.server.common.Error;
import com.naorem.khogen.server.common.Result;
import com.naorem.khogen.server.common.ValidationUtil;

public class AccountManagerImpl implements AccountManager {
	@Autowired
	private AccountDAO accountDAO;

	@Override
	public Result<Account> getAccount(final String userId, final boolean hidePassword) {
		Result<Account> result = accountDAO.getAccount(userId);
		if (result.getError() == Error.SUCCESS && hidePassword) {
			result.getValue().getCredential().setPassword(null);
		}
		return result;
	}

	@Override
	public Result<List<Account>> getAccounts(final List<String> userIds) {
		Result<List<Account>> result = accountDAO.getAccounts(userIds);
		if (result.getError() == Error.SUCCESS) {
			for (Account account : result.getValue()) {
				account.getCredential().setPassword(null);
			}
		}
		return result;
	}

	@Override
	public Result<Account> createAccount(final Account account) {
		ValidationUtil.validateAccount(account);
		return accountDAO.createAccount(account);
	}

	@Override
	public Result<Account> updateAccount(final Account account) {
		ValidationUtil.validateAccount(account);
		return accountDAO.updateAccount(account);
	}

	@Override
	public Result<Account> deleteAccount(final String userId) {
		ValidationUtil.validate(userId, "User ID");
		return accountDAO.deleteAccount(userId);
	}

	@Override
	public Result<Friend> friend(final String userId, final String friendId) {
		ValidationUtil.validate(userId, "User ID");
		ValidationUtil.validate(friendId, "Friend ID");
		return accountDAO.friend(userId, friendId);
	}

	@Override
	public Result<Friend> unfriend(final String userId, final String friendId) {
		return accountDAO.unfriend(userId, friendId);
	}

	@Override
	public Result<List<Friend>> getFriends(final String userId) {
		Result<List<Friend>> result = accountDAO.getFriends(userId);
		if (result.getError() == Error.SUCCESS) {
			for (Friend friend : result.getValue()) {
				friend.getFriendCredential().setPassword(null);
			}
		}
		return result;
	}

	@Override
	public Result<List<Friend>> getFriends(final String userId, final List<String> receiverIds) {
		Result<List<Friend>> result = accountDAO.getFriends(userId, receiverIds);
		if (result.getError() == Error.SUCCESS) {
			for (Friend friend : result.getValue()) {
				friend.getFriendCredential().setPassword(null);
			}
		}
		return result;
	}
}
