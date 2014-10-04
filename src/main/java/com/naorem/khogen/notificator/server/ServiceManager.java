package com.naorem.khogen.notificator.server;

import java.util.List;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;

import com.naorem.khogen.account.server.model.dto.Account;
import com.naorem.khogen.account.server.model.dto.Friend;
import com.naorem.khogen.gcm.server.model.dto.BroadcastRequest;
import com.naorem.khogen.gcm.server.model.dto.Message;
import com.naorem.khogen.server.common.Result;

@Consumes("application/json")
@Produces("application/json")
public interface ServiceManager {
	/* Authentication & authorization */
	//@Secured("ROLE_ADMIN")
	@Path("/fetch-account")
	@GET
	Result<Account> getAccount(@QueryParam("user") final String userId);

	/* Account registration */
	
	@Path("/create-account")
	@POST
	Result<Account> createAccount(final Account account);
	
	@Path("/update-account")
	@PUT
	Result<Account> updateAccount(final Account account);

	@Path("/delete-account")
	@DELETE
	Result<Account> deleteAccount(@QueryParam("user") final String userId);

	@Path("/add-friend")
	@PUT
	Result<Friend> friend(@QueryParam("user") final String userId, @QueryParam("friend") final String friendId);

	@Path("/delete-friend")
	@DELETE
	Result<Friend> unfriend(@QueryParam("user") final String userId, @QueryParam("friend") final String friendId);
	
	@Path("/fetch-friends")
	@GET
	Result<List<Friend>> getFriends(@QueryParam("user") final String userId);

	/* GCM server */
	@Path("/send")
	@POST
	Result<Message> send(@QueryParam("sender") final String senderId, @QueryParam("receiver") final String receiverId, final String text);

	@Path("/broadcast")
	@POST
	Result<List<Message>> send(final BroadcastRequest broadcastRequest);

	@Path("/fetch-conversation")
	@GET
	Result<List<Message>> getConversation(@QueryParam("user") final String senderId, @QueryParam("friend") final String receiverId, @QueryParam("start") final long startTime,
			@QueryParam("limit") final long limit);

	@Path("/fetch-messages")
	@GET
	Result<List<Message>> getMessages(@QueryParam("user") final String id, @QueryParam("start") final long startTime, @QueryParam("limit") final long limit);

	@Path("/delete-message")
	@DELETE
	Result<Message> deleteMessage(@QueryParam("user") final String userId, @QueryParam("message") final String messageId);

}
