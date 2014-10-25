package com.ecwidizer.api;

import com.ecwidizer.EcwidizerSettings;
import com.ecwidizer.Logger;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.InputStreamEntity;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Created by basil on 13.09.13.
 */
public class ProductApiRequestor {

	private final String productsApiEndpoint;
    private final String imageApiEndlpoint;
    private final String storeId;
	private final String token;

	public ProductApiRequestor(EcwidizerSettings settings) {
		this.productsApiEndpoint = settings.getApiEndpoint() + "/api/v3/{storeId}/products";
		this.imageApiEndlpoint = productsApiEndpoint + "/{productId}/image";
		this.storeId = settings.getStoreId();
		this.token = settings.getToken();
	}

	public Integer createProduct(CreateProductRequest request) throws IOException, JSONException {
		String endpoint = productsApiEndpoint.replace("{storeId}", storeId) + "?token=" + token;

		String json = createNewProductJson(request);

		HttpClient client = new DefaultHttpClient();
		HttpPost post = new HttpPost(endpoint);
		post.setHeader("Content-Type", "application/json");
		post.setEntity(new StringEntity(json, "UTF-8"));
        Logger.log("Product API request: " + endpoint + " " + json);
        HttpResponse resp = client.execute(post);
        if (resp.getStatusLine().getStatusCode() != 200) {
			JSONObject jObject = parseProductApiResponse(resp);
			String message = jObject == null ? null : jObject.getString("errorMessage");
			Logger.log("Product API request failed: "+resp.getStatusLine() + " / " + message);
            throw new IOException(message == null ? resp.getStatusLine().toString() : message);
        }
		JSONObject jObject = parseProductApiResponse(resp);
		if (jObject == null) {
			Logger.log("Invalid response from Ecwid.");
			throw new IOException("Invalid response from Ecwid.");
		}
		Logger.log("Product API request succeded!");
		int productId = jObject.getInt("id");
        return productId;
    }

	private String createNewProductJson(CreateProductRequest request) throws JSONException {
		JSONObject jsonObject = new JSONObject();
		if (request.sku != null) {
			jsonObject.put("sku", request.sku);
		}

		if (request.name != null) {
			jsonObject.put("name", request.name);
		}

		if (request.description != null) {
			jsonObject.put("description", request.description);
		}

		if (request.price != null) {
			jsonObject.put("price", request.price);
		}

		if (request.weight != null) {
			jsonObject.put("weight", request.weight);
		}

		return jsonObject.toString();
	}

	private JSONObject parseProductApiResponse(HttpResponse resp) throws IOException {
        HttpEntity entity = resp.getEntity();
        InputStream stream = entity.getContent();
        BufferedReader reader = new BufferedReader(new InputStreamReader(stream));
        StringBuilder sb = new StringBuilder();
        String line;

        while ((line = reader.readLine()) != null) {
            sb.append(line);
        }

        String result = sb.toString();

		try {
			return new JSONObject(result);
		} catch (JSONException e) {
			return null;
		}
	}

    public void uploadImage(int productId, String ownerId, String fileName) throws IOException {
        List<NameValuePair> parameters = new ArrayList<NameValuePair>();

        parameters.add(new BasicNameValuePair("token", token));

        HttpClient client = new DefaultHttpClient();
        String endpoint = imageApiEndlpoint.replace("{storeId}", ""+ownerId).replace("{productId}", "" + productId);
        HttpPost post = new HttpPost(endpoint);

        File imageFile = new File(fileName);
        InputStreamEntity reqEntity = new InputStreamEntity(new FileInputStream(imageFile), -1);
        reqEntity.setContentType("binary/octet-stream");
        reqEntity.setChunked(true);
        post.setEntity(reqEntity);
        Logger.log("Image Upload API request: " + parameters);
        HttpResponse resp = client.execute(post);
        if (resp.getStatusLine().getStatusCode() != 200) {
            Logger.log("Image Upload API request failed: "+resp.getStatusLine());
            throw new IOException(resp.getStatusLine().toString());
        }
        Logger.log("Image Upload API request succeded!");
    }
}
