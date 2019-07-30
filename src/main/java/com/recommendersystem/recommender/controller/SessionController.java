package com.recommendersystem.recommender.controller;

import java.nio.charset.StandardCharsets;
import java.util.Calendar;

import com.google.common.hash.Hashing;
import com.recommendersystem.recommender.models.Session;
import com.recommendersystem.recommender.models.User;

public class SessionController {
	public static Session createSession(User user) {
		Session session = new Session();

		Calendar expirationDate = Calendar.getInstance();
		expirationDate.add(1, Calendar.MONTH);

		session.setExpirationDate(expirationDate);
		session.setLastAccess(Calendar.getInstance());
		session.setSessionId(Hashing.sha512()
				.hashString(
						user.getEmail() + user.getId() + user.getPassword() + session.getLastAccess().getTimeInMillis(),
						StandardCharsets.UTF_8)
				.toString());

		return session;
	}

	public static boolean isValidSession(String sessionId, User user) {
		if (user == null || user.getSession() == null || user.getSession().getSessionId() == null) {
			return false;
		}

		if (!sessionId.equals(user.getSession().getSessionId())) {
			return false;
		}

		if (Calendar.getInstance().getTimeInMillis() > user.getSession().getExpirationDate().getTimeInMillis()) {
			return false;
		}

		return true;
	}

	public static Session getInvalidSession() {
		Session session = null;

		return session;
	}
}
