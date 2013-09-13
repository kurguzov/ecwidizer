package com.ecwidizer;

import android.util.Log;

/**
 * Created by igor on 9/13/13.
 */
public class Logger {

	private static final String ECWIDIZER = "ECWIDIZER";

	public static void log(String message) {
		Log.i(ECWIDIZER, message);
	}

	public static void error(String message) {
		Log.e(ECWIDIZER, message);
	}

	public static void error(String message, Exception e) {
		Log.e(ECWIDIZER, message, e);
	}
}
