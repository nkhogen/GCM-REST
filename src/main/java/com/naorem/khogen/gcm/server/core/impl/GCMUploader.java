package com.naorem.khogen.gcm.server.core.impl;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.net.ssl.SSLSocketFactory;

import org.jivesoftware.smack.ConnectionConfiguration;
import org.jivesoftware.smack.ConnectionConfiguration.SecurityMode;
import org.jivesoftware.smack.ConnectionListener;
import org.jivesoftware.smack.PacketInterceptor;
import org.jivesoftware.smack.PacketListener;
import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.SmackException.NotConnectedException;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.filter.PacketTypeFilter;
import org.jivesoftware.smack.packet.DefaultPacketExtension;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.Packet;
import org.jivesoftware.smack.packet.PacketExtension;
import org.jivesoftware.smack.provider.PacketExtensionProvider;
import org.jivesoftware.smack.provider.ProviderManager;
import org.jivesoftware.smack.tcp.XMPPTCPConnection;
import org.jivesoftware.smack.util.StringUtils;
import org.json.simple.JSONValue;
import org.json.simple.parser.ParseException;
import org.xmlpull.v1.XmlPullParser;

import com.naorem.khogen.gcm.server.core.GCMUpstreamMessageListener;
import com.naorem.khogen.server.common.GlobalConstants;

public class GCMUploader {

	private GCMUpstreamMessageListener gcmUpstreamMessageListener;

	private static final Logger LOGGER = Logger.getLogger(GCMUploader.class.getName());
	static {

		ProviderManager.addExtensionProvider(GlobalConstants.GCM_ELEMENT_NAME, GlobalConstants.GCM_NAMESPACE, new PacketExtensionProvider() {
			@Override
			public PacketExtension parseExtension(XmlPullParser parser) throws Exception {
				String json = parser.nextText();
				return new GcmPacketExtension(json);
			}
		});
	}

	private XMPPConnection connection;

	/**
	 * Indicates whether the connection is in draining state, which means that
	 * it will not accept any new downstream messages.
	 */
	protected volatile boolean connectionDraining = false;

	public void init() throws XMPPException, IOException, SmackException {
		connect(GlobalConstants.SENDER_ID, GlobalConstants.SENDER_API_KEY);
	}

	public GCMUpstreamMessageListener getGcmUpstreamMessageListener() {
		return gcmUpstreamMessageListener;
	}

	public void setGcmUpstreamMessageListener(final GCMUpstreamMessageListener gcmUpstreamMessageListener) {
		this.gcmUpstreamMessageListener = gcmUpstreamMessageListener;
	}

	/**
	 * Sends a downstream message to GCM.
	 * 
	 * @return true if the message has been successfully sent.
	 */
	public boolean sendDownstreamMessage(final String jsonRequest) throws NotConnectedException {
		if (!connectionDraining) {
			send(jsonRequest);
			return true;
		}
		LOGGER.info("Dropping downstream message since the connection is draining");
		return false;
	}

	/**
	 * Sends a packet with contents provided.
	 */
	protected void send(String jsonRequest) throws NotConnectedException {
		Packet request = new GcmPacketExtension(jsonRequest).toPacket();
		connection.sendPacket(request);
	}

	protected void handleControlMessage(Map<String, Object> jsonObject) {
		LOGGER.log(Level.INFO, "handleControlMessage(): " + jsonObject);
		String controlType = (String) jsonObject.get("control_type");
		if ("CONNECTION_DRAINING".equals(controlType)) {
			connectionDraining = true;
		} else {
			LOGGER.log(Level.INFO, "Unrecognized control type: %s. This could happen if new features are " + "added to the CCS protocol.", controlType);
		}
		gcmUpstreamMessageListener.onControl(jsonObject);
	}

	/**
	 * Creates a JSON encoded ACK message for an upstream message received from
	 * an application.
	 * 
	 * @param to
	 *            RegistrationId of the device who sent the upstream message.
	 * @param messageId
	 *            messageId of the upstream message to be acknowledged to CCS.
	 * @return JSON encoded ack.
	 */
	protected static String createJsonAck(String to, String messageId) {
		Map<String, Object> message = new HashMap<String, Object>();
		message.put("message_type", "ack");
		message.put("to", to);
		message.put("message_id", messageId);
		return JSONValue.toJSONString(message);
	}

	/**
	 * Connects to GCM Cloud Connection Server using the supplied credentials.
	 * 
	 * @param senderId
	 *            Your GCM project number
	 * @param apiKey
	 *            API Key of your project
	 */
	public void connect(long senderId, String apiKey) throws XMPPException, IOException, SmackException {
		ConnectionConfiguration config = new ConnectionConfiguration(GlobalConstants.GCM_SERVER, GlobalConstants.GCM_PORT);
		config.setSecurityMode(SecurityMode.enabled);
		config.setReconnectionAllowed(true);
		config.setRosterLoadedAtLogin(false);
		config.setSendPresence(false);
		config.setSocketFactory(SSLSocketFactory.getDefault());

		connection = new XMPPTCPConnection(config);
		connection.connect();

		connection.addConnectionListener(new LoggingConnectionListener());

		// Handle incoming packets
		connection.addPacketListener(new PacketListener() {
			@Override
			public void processPacket(Packet packet) {
				// logger.log(Level.INFO, "Received: " + packet.toXML());
				Message incomingMessage = (Message) packet;
				GcmPacketExtension gcmPacket = (GcmPacketExtension) incomingMessage.getExtension(GlobalConstants.GCM_NAMESPACE);
				String json = gcmPacket.getJson();
				try {
					@SuppressWarnings("unchecked")
					Map<String, Object> jsonObject = (Map<String, Object>) JSONValue.parseWithException(json);
					// present for "ack"/"nack", null otherwise
					Object messageType = jsonObject.get("message_type");

					if (messageType == null) {
						// Normal upstream data message
						gcmUpstreamMessageListener.onReceive(jsonObject);
						// Send ACK to CCS
						String messageId = (String) jsonObject.get("message_id");
						String from = (String) jsonObject.get("from");
						String ack = createJsonAck(from, messageId);
						send(ack);
					} else if ("ack".equals(messageType.toString())) {
						// Process Ack
						gcmUpstreamMessageListener.onAck(jsonObject);
					} else if ("nack".equals(messageType.toString())) {
						// Process Nack
						gcmUpstreamMessageListener.onNack(jsonObject);
					} else if ("control".equals(messageType.toString())) {
						// Process control message
						handleControlMessage(jsonObject);
					} else {
						LOGGER.log(Level.WARNING, "Unrecognized message type (%s)", messageType.toString());
					}
				} catch (ParseException e) {
					LOGGER.log(Level.SEVERE, "Error parsing JSON " + json, e);
				} catch (Exception e) {
					LOGGER.log(Level.SEVERE, "Failed to process packet", e);
				}
			}
		}, new PacketTypeFilter(Message.class));

		// Log all outgoing packets
		connection.addPacketInterceptor(new PacketInterceptor() {
			@Override
			public void interceptPacket(Packet packet) {
				LOGGER.log(Level.INFO, "Sent successfully");// {0}",
															// packet.toXML());
			}
		}, new PacketTypeFilter(Message.class));
		connection.login(senderId + "@" + GlobalConstants.GCM_SERVER, apiKey);
	}

	/**
	 * XMPP Packet Extension for GCM Cloud Connection Server.
	 */
	private static final class GcmPacketExtension extends DefaultPacketExtension {

		private final String json;

		public GcmPacketExtension(String json) {
			super(GlobalConstants.GCM_ELEMENT_NAME, GlobalConstants.GCM_NAMESPACE);
			this.json = json;
		}

		public String getJson() {
			return json;
		}

		@Override
		public String toXML() {
			return String.format("<%s xmlns=\"%s\">%s</%s>", GlobalConstants.GCM_ELEMENT_NAME, GlobalConstants.GCM_NAMESPACE, StringUtils.escapeForXML(json),
					GlobalConstants.GCM_ELEMENT_NAME);
		}

		public Packet toPacket() {
			Message message = new Message();
			message.addExtension(this);
			return message;
		}
	}

	private static final class LoggingConnectionListener implements ConnectionListener {

		@Override
		public void connected(XMPPConnection xmppConnection) {
			LOGGER.info("Connected.");
		}

		@Override
		public void authenticated(XMPPConnection xmppConnection) {
			LOGGER.info("Authenticated.");
		}

		@Override
		public void reconnectionSuccessful() {
			LOGGER.info("Reconnecting..");
		}

		@Override
		public void reconnectionFailed(Exception e) {
			LOGGER.log(Level.INFO, "Reconnection failed.. ", e);
		}

		@Override
		public void reconnectingIn(int seconds) {
			LOGGER.log(Level.INFO, "Reconnecting in %d secs", seconds);
		}

		@Override
		public void connectionClosedOnError(Exception e) {
			LOGGER.info("Connection closed on error.");
		}

		@Override
		public void connectionClosed() {
			LOGGER.info("Connection closed.");
		}
	}

}
