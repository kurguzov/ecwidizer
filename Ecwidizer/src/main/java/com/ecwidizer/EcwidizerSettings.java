package com.ecwidizer;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;

import java.util.Properties;

/**
 * Created by igor on 25/10/14.
 */
public class EcwidizerSettings {

	public static final String APPLICATION_PROPERTIES_FILE = "application.properties";
	private static EcwidizerSettings INSTANCE;

	private final String SETTINGS_STORAGE = "ecwidizer-preferences";
	private static final String SETTINGS_STORE_ID = "store_id";
	private static final String SETTINGS_TOKEN = "token";
	private final SharedPreferences preferences;
	private final Properties properties;

	private EcwidizerSettings(Activity activity) {
		preferences = activity.getSharedPreferences(SETTINGS_STORAGE, Context.MODE_PRIVATE);
		properties = readApplicationProperties(activity);
	}

	private static Properties readApplicationProperties(Activity activity) {
		Properties properties = new Properties();
		try {
			properties.load(activity.getAssets().open(APPLICATION_PROPERTIES_FILE));
		} catch (Exception e) {
			Logger.error("Unable to read properties", e);
		}
		return properties;
	}

	public static void create(Activity activity) {
		INSTANCE = new EcwidizerSettings(activity);
	}

	public static EcwidizerSettings get() {
		return INSTANCE;
	}

	public String getStoreId() {
		return preferences.getString(SETTINGS_STORE_ID, "");
	}

	public void setStoreId(String storeId) {
		preferences.edit().putString(SETTINGS_STORE_ID, storeId).apply();
	}

	public String getToken() {
		return preferences.getString(SETTINGS_TOKEN, "");
	}

	public void setToken(String token) {
		preferences.edit().putString(SETTINGS_TOKEN, token).apply();
	}

	public String getApiEndpoint() {
		return properties.getProperty("ecwid.api_endpoint");
	}
}
