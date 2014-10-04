package com.naorem.khogen.gcm.server.core;

import java.util.Map;

public interface GCMUpstreamMessageListener {
	void onReceive(Map<String, Object> message);
	void onAck(Map<String, Object> jsonObject);
	void onNack(Map<String, Object> jsonObject);
	void onControl(Map<String, Object> jsonObject);
}
