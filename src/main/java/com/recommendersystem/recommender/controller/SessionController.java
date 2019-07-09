package com.recommendersystem.recommender.controller;

import java.util.Calendar;

import com.recommendersystem.recommender.models.Session;

public class SessionController {
	public static Session createSession() {
		Session session = new Session();

		Calendar expirationDate = Calendar.getInstance();
		expirationDate.add(2, Calendar.MONTH);

		session.setExpirationDate(expirationDate);
		session.setLastAccess(Calendar.getInstance());
		session.setSessionId("hugdsfhiugdsf");

		return session;
	}

	public static boolean isValidSession(String sessionId, String userId) {
		return true;
	}
}
