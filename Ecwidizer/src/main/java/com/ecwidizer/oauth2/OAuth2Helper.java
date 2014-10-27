package com.ecwidizer.oauth2;

import android.content.SharedPreferences;
import android.util.Log;

import com.ecwidizer.MainActivity;
import com.ecwidizer.EcwidizerSettings;
import com.ecwidizer.oauth2.store.SharedPreferencesCredentialStore;
import com.google.api.client.auth.oauth2.AuthorizationCodeFlow;
import com.google.api.client.auth.oauth2.ClientParametersAuthentication;
import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.auth.oauth2.CredentialStore;
import com.google.api.client.auth.oauth2.TokenResponse;
import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

/**
* Created by alexis on 25.10.14.
*/
public class OAuth2Helper {

	/** Global instance of the HTTP transport. */
	private static final HttpTransport HTTP_TRANSPORT = new NetHttpTransport();

	/** Global instance of the JSON factory. */
	private static final JsonFactory JSON_FACTORY = new JacksonFactory();

	private final CredentialStore credentialStore;

	private AuthorizationCodeFlow flow;

	private OAuth2Params oauth2Params;

	public OAuth2Helper(SharedPreferences sharedPreferences, OAuth2Params oauth2Params) {
		this.credentialStore = new SharedPreferencesCredentialStore(sharedPreferences);
		this.oauth2Params = oauth2Params;
		this.flow = new AuthorizationCodeFlow.Builder(
				oauth2Params.getAccessMethod(),
				HTTP_TRANSPORT,
				JSON_FACTORY,
				new GenericUrl(oauth2Params.getTokenServerUrl()),
				new ClientParametersAuthentication(oauth2Params.getClientId(), oauth2Params.getClientSecret()),
				oauth2Params.getClientId(),
				oauth2Params.getAuthorizationServerEncodedUrl()
		).setCredentialStore(this.credentialStore).build();
	}

	public OAuth2Helper(SharedPreferences sharedPreferences) {
		this(sharedPreferences, OAuth2Params.ECWID);
	}

	public String getAuthorizationUrl() {
		return flow.newAuthorizationUrl().setRedirectUri(oauth2Params.getRedirectUri()).setScopes(convertScopesToString(oauth2Params.getScope())).build();
	}

	public void retrieveAndStoreAccessToken(String authorizationCode) throws IOException {
		Log.i(MainActivity.TAG, "retrieveAndStoreAccessToken for code " + authorizationCode);
		TokenResponse tokenResponse = flow.newTokenRequest(authorizationCode)
				.setScopes(convertScopesToString(oauth2Params.getScope()))
				.setRedirectUri(oauth2Params.getRedirectUri()).execute();

		String storeId = tokenResponse.getUnknownKeys().get("store_id").toString();

		Log.i(MainActivity.TAG, "Found tokenResponse:");
		Log.i(MainActivity.TAG, "Access Token: " + tokenResponse.getAccessToken());
		Log.i(MainActivity.TAG, "Refresh Token: " + tokenResponse.getRefreshToken());
		Log.i(MainActivity.TAG, "Store ID: " + storeId);

		EcwidizerSettings.get().setStoreId(storeId);

		flow.createAndStoreCredential(tokenResponse, oauth2Params.getUserId());
	}

	public Credential loadCredential() throws IOException {
		return flow.loadCredential(oauth2Params.getUserId());
	}

	public void clearCredentials() throws IOException {
		 flow.getCredentialStore().delete(oauth2Params.getUserId(), null);
	}

	private Collection<String> convertScopesToString(String scopesConcat) {
		String[] scopes = scopesConcat.split(",");
		Collection<String> collection = new ArrayList<String>();
		Collections.addAll(collection, scopes);
		return collection;
	}

}
