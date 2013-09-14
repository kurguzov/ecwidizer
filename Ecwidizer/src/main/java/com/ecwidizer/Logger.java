package com.ecwidizer;

import android.util.Log;

/**
 * Created by igor on 9/13/13.
 */
public class Logger {

	private static final String ECWIDIZER = "ZAEBECWID";

	public static void log(String message) {
		Log.i(ECWIDIZER, message);
	}

	public static void error(String message) {
		Log.e(ECWIDIZER, message);
	}

	public static void error(String message, Throwable e) {
		Log.e(ECWIDIZER, message, e);
	}

	public static void kernelPanic(String message) {
		Log.e(ECWIDIZER, "Kernel Panic!!! " + message);
	}
}
