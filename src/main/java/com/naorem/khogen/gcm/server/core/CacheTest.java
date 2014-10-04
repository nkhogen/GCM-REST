package com.naorem.khogen.gcm.server.core;

import java.io.IOException;

import com.naorem.khogen.server.common.Cache;

public class CacheTest {
	public static void main(String[] args) throws IOException {
		Cache cache = Cache.getInstance();
		cache.put("key", "Khogendro");
		System.out.println(cache.get("key"));
	}
}
