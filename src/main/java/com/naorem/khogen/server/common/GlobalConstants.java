package com.naorem.khogen.server.common;

public final class GlobalConstants {
	public static final int GCM_PORT = 5235;
	public static final long SENDER_ID = 540781517159L; // your GCM sender id
	public static final String SENDER_API_KEY = "AIzaSyDG42Zug0-QndgI1X3jkV-Z3SNwEI-oUNw";
	public static final String GCM_SERVER = "gcm.googleapis.com";
	public static final String GCM_ELEMENT_NAME = "gcm";
	public static final String GCM_NAMESPACE = "google:mobile:data";
	public static final String MESSAGE_FIELD = "message";
	public static final String REGISTRATION_ID_FIELD = "registration_id";
	public static final String PERSISTENCE_UNIT = "com.naorem.khogen.notificator";
	public static final String ROLE_ALL_USER = "ALL";
	public static final int PUT_MESSAGE_RETRY_LIMIT = 3;
	public static final String LIMIT_REQUEST_HEADER = "limit";
	public static final long SQL_ROW_LIMIT_PADDING = 100L;
	public static final String CACHE_NAME = "NotificatorCache";
	public static final String CACHE_CONFIG_PATH = "classpath:config/cache/ehcache-config.xml";
	private GlobalConstants(){}
}
