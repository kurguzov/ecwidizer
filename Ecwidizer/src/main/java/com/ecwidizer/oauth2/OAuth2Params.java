package com.ecwidizer.oauth2;

import com.google.api.client.auth.oauth2.BearerToken;
import com.google.api.client.auth.oauth2.Credential;

/**
 * Enum that encapsulates the various OAuth2 connection parameters for the different providers
 *
 * We capture the following properties for the demo application
 *
 * clientId
 * clientSecret
 * scope
 * rederectUri
 * apiUrl
 * tokenServerUrl
 * authorizationServerEncodedUrl
 * accessMethod
 *
 * @author davydewaele
 *
 */
public enum OAuth2Params {

	ECWID("myapp", "mysecret",
			"https://myigor.ecwid.com/api/oauth/token",
			"https://myigor.ecwid.com/api/oauth/authorize",
			BearerToken.authorizationHeaderAccessMethod(),
			"read_store_profile read_catalog update_catalog create_catalog",
			"http://localhost",
			"myapp");

    private String clientId;
	private String clientSecret;
	private String scope;
	private String rederectUri;
	private String userId;

	private String tokenServerUrl;
	private String authorizationServerEncodedUrl;

	private Credential.AccessMethod accessMethod;

	OAuth2Params(String clientId, String clientSecret, String tokenServerUrl, String authorizationServerEncodedUrl, Credential.AccessMethod accessMethod, String scope, String rederectUri, String userId) {
		this.clientId=clientId;
		this.clientSecret=clientSecret;
		this.tokenServerUrl=tokenServerUrl;
		this.authorizationServerEncodedUrl=authorizationServerEncodedUrl;
		this.accessMethod=accessMethod;
		this.scope=scope;
		this.rederectUri = rederectUri;
		this.userId=userId;
	}

	public String getClientId() {
		if (this.clientId==null || this.clientId.length()==0) {
			throw new IllegalArgumentException("Please provide a valid clientId in the Oauth2Params class");
		}
		return clientId;
	}
	public String getClientSecret() {
		if (this.clientSecret==null || this.clientSecret.length()==0) {
			throw new IllegalArgumentException("Please provide a valid clientSecret in the Oauth2Params class");
		}
		return clientSecret;
	}

	public String getScope() {
		return scope;
	}

	public String getRedirectUri() {
		return rederectUri;
	}

	public String getTokenServerUrl() {
		return tokenServerUrl;
	}

	public String getAuthorizationServerEncodedUrl() {
		return authorizationServerEncodedUrl;
	}

	public Credential.AccessMethod getAccessMethod() {
		return accessMethod;
	}

	public String getUserId() {
		return userId;
	}

}
