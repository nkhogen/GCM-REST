package com.naorem.khogen.server.common;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Element;

import org.springframework.util.ResourceUtils;

public class Cache {
	private CacheManager cacheManager;
	private volatile static Cache cache;
	private final String cacheName = GlobalConstants.CACHE_NAME;

	private Cache() throws IOException {
		URL url = ResourceUtils.getURL(GlobalConstants.CACHE_CONFIG_PATH);
		InputStream inputStream = url.openStream();
		if (inputStream != null) {
			cacheManager = CacheManager.create(inputStream);
			try {
				inputStream.close();
			} catch (IOException e) {
			}
		}
	}

	public static Cache getInstance() throws IOException {
		if (cache == null) {
			synchronized (Cache.class) {
				if (cache == null) {
					cache = new Cache();
				}
			}
		}
		return cache;
	}

	public Object get(final Object key) {
		Element element = cacheManager.getEhcache(cacheName).get(key);
		if (element != null) {
			return element.getObjectValue();
		}
		return null;
	}

	public boolean remove(final Object key) {
		return cacheManager.getEhcache(cacheName).remove(key);
	}

	public void put(final Object key, final Object value) {
		cacheManager.getEhcache(cacheName).put(new Element(key, value));
	}
}
