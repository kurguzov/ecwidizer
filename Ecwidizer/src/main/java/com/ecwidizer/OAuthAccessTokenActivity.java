package com.ecwidizer;

import java.io.IOException;
import java.net.URLDecoder;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.ecwidizer.oauth2.OAuth2Helper;
import com.ecwidizer.oauth2.OAuth2Params;

/**
 * Execute the OAuthRequestTokenTask to retrieve the request, and authorize the request.
 * After the request is authorized by the user, the callback URL will be intercepted here.
 * 
 */
@SuppressLint("SetJavaScriptEnabled")
public class OAuthAccessTokenActivity extends Activity {

	private SharedPreferences prefs;
	private OAuth2Helper oAuth2Helper;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
        Log.i(MainActivity.TAG, "Starting task to retrieve request token.");
        this.prefs = PreferenceManager.getDefaultSharedPreferences(this);
        oAuth2Helper = new OAuth2Helper(this.prefs);
        webview = new WebView(this);
        webview.getSettings().setJavaScriptEnabled(true);  
        webview.setVisibility(View.VISIBLE);
        setContentView(webview);
        
        String authorizationUrl = oAuth2Helper.getAuthorizationUrl();
        Log.i(MainActivity.TAG, "Using authorizationUrl = " + authorizationUrl);
        
        handled=false;
        
        webview.setWebViewClient(new WebViewClient() {  

        	@Override  
            public void onPageStarted(WebView view, String url,Bitmap bitmap)  {  
        		Log.d(MainActivity.TAG, "onPageStarted : " + url + " handled = " + handled);
            }
        	@Override  
            public void onPageFinished(final WebView view, final String url)  {
        		Log.d(MainActivity.TAG, "onPageFinished : " + url + " handled = " + handled);
		        //if (url.contains("/installedAppRedirect?")) {
        		if (url.startsWith(OAuth2Params.ECWID.getRedirectUri())) {
	        		webview.setVisibility(View.INVISIBLE);
	        		if (!handled) {
	        			new ProcessToken(url).execute();
	        		}
        		} else {
        			webview.setVisibility(View.VISIBLE);
        		}
            }

        });  
        
        webview.loadUrl(authorizationUrl);		
	}
	
	private WebView  webview;
	
	boolean handled=false;
	private boolean hasLoggedIn;
	
	@Override
	protected void onResume() {
		super.onResume();
		Log.i(MainActivity.TAG, "onResume called with " + hasLoggedIn);
		if (hasLoggedIn) {
			finish();
		}
	}

	
	private class ProcessToken extends AsyncTask<Uri, Void, Void> {

		String url;
		boolean startActivity=false;

		public ProcessToken(String url) {
			this.url=url;
		}
		
		@Override
		protected Void doInBackground(Uri...params) {
			//if (url.contains("/installedAppRedirect?")) {
			if (url.startsWith(OAuth2Params.ECWID.getRedirectUri())) {
				Log.i(MainActivity.TAG, "Redirect URL found" + url);
				handled=true;
        		try {
        			if (url.indexOf("code=")!=-1) {
            			String authorizationCode = extractCodeFromUrl(url);
            			
            			Log.i(MainActivity.TAG, "Found code = " + authorizationCode);
						
            			oAuth2Helper.retrieveAndStoreAccessToken(authorizationCode);
            			startActivity=true;
			  		    hasLoggedIn=true;

        			} else if (url.indexOf("error=")!=-1) {
        				startActivity=true;
        			}
        			
				} catch (Exception e) {
					e.printStackTrace();
				}

        	} else {
        		Log.i(MainActivity.TAG, "Not doing anything for url " + url);
        	}
            return null;
		}

		private String extractCodeFromUrl(String url) throws Exception {
			String codeParam = "?code=";
			String encodedCode = url.substring(url.indexOf(codeParam) + codeParam.length());
			return URLDecoder.decode(encodedCode, "UTF-8");
		}  
		
		@Override
		protected void onPreExecute() {
		}

		/**
		 * When we're done and we've retrieved either a valid token or an error from the server,
		 * we'll return to our original activity 
		 */
		@Override
		protected void onPostExecute(Void result) {
			if (startActivity) {
				Log.i(MainActivity.TAG," ++++++++++++ Starting mainscreen again");
				try {
					EcwidizerSettings.get().setToken(oAuth2Helper.loadCredential().getAccessToken());
				} catch (IOException e) {
				}
				finish();
			}

		}

	}
}
