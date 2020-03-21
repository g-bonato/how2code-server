package com.recommendersystem.recommender.utils;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

public class LogUtil {
	private static Logger logger = Logger.getLogger(LogUtil.class);
	
	public static void logDebug(String message) {
		logger.setLevel(Level.ALL);
		
		logger.debug(message);
	}
	
	public static void logError(String message) {
		logger.setLevel(Level.ALL);
		
		logger.error(message);
	}
}
