package com.naorem.khogen.account.server.core;

import java.util.List;

import com.naorem.khogen.account.server.model.dto.Account;
import com.naorem.khogen.account.server.model.dto.Friend;
import com.naorem.khogen.server.common.Result;


public interface AccountManager {
	/* Account registration */
	Result<Account> createAccount(final Account account);
	Result<Account> updateAccount(final Account account);
	Result<Account> getAccount(final String userId, final boolean hidePassword);
	Result<Account> deleteAccount(final String userId);
	Result<Friend> friend(final String userId, final String friendId);
	Result<Friend> unfriend(final String userId, final String friendId);
	Result<List<Friend>> getFriends(final String userId);
	Result<List<Friend>> getFriends(final String userId, final List<String> receiverIds);
	Result<List<Account>> getAccounts(final List<String> userIds);
}
