package com.recommendersystem.recommender.utils;

public class StringUtil {
	public static Boolean isBlank(String text) {
		if (text == null) {
			return true;
		}

		if (text == "") {
			return true;
		}
		
		return false;
	}

	public static Boolean isNotBlank(String text) {
		return !isBlank(text);
	}
}
