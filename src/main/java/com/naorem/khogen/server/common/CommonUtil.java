package com.naorem.khogen.server.common;

import java.util.regex.Pattern;

import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityManager;
import javax.persistence.Persistence;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class CommonUtil {
	private static final Pattern EMAIL_PATTERN = Pattern.compile("^[a-zA-Z]+[0-9a-zA-Z_\\-]*(\\.[0-9a-zA-Z_\\-]+)*@[0-9a-zA-B]+[0-9a-zA-Z_\\-]*\\.[a-zA-Z]+(\\.[a-zA-Z]+)*$");
	private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
	private static final EntityManagerFactory entityManagerFactory = Persistence.createEntityManagerFactory(GlobalConstants.PERSISTENCE_UNIT);

	public static EntityManager getEntityManager() {
		return entityManagerFactory.createEntityManager();
	}

	public static String toJson(final Object object) {
		try {
			return OBJECT_MAPPER.writeValueAsString(object);
		} catch (JsonProcessingException e) {
			e.printStackTrace();
		}
		return null;
	}

	public static boolean isEmailValid(final String email) {
		if (email == null || email.trim().isEmpty()) {
			return false;
		}
		return EMAIL_PATTERN.matcher(email).find();
	}
}
