package com.ecwidizer;

import android.util.Log;

/**
 * Created by igor on 9/13/13.
 */
public class Logger {

	private static final String ECWIDIZER = "ECWIDIZER";

	private void log(String message) {
		Log.i(ECWIDIZER, message);
	}

	private void error(String message) {
		Log.e(ECWIDIZER, message);
	}
}
