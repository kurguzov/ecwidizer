package com.ecwidizer.api;

import com.ecwidizer.Logger;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.InputStreamEntity;
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

	public static final String ECWID_API_ENDPOINT = "https://app.ecwid.com";
	public static final String ECWID_API_ENDPOINT_PRODUCTS = ECWID_API_ENDPOINT + "/api/v3/{storeId}/products";
    public static final String ECWID_API_ENDPOINT_PRODUCT_IMAGE_UPLOAD = ECWID_API_ENDPOINT_PRODUCTS + "/{productId}/image";
    private final String storeId;
	private final String token;

	public ProductApiRequestor(String storeId, String token) {
		this.storeId = storeId;
		this.token = token;
	}

	public Integer createProduct(CreateProductRequest request) throws IOException {
        List<NameValuePair> parameters = new ArrayList<NameValuePair>();

		parameters.add(new BasicNameValuePair("token", token));

        if (request.name != null) {
            parameters.add(new BasicNameValuePair("name", request.name));
        }

        if (request.description != null) {
            parameters.add(new BasicNameValuePair("description", request.description));
        }

        if (request.price != null) {
            parameters.add(new BasicNameValuePair("price", new DecimalFormat("#.###", new DecimalFormatSymbols(Locale.US)).format(request.price)));
        }

        if (request.weight != null) {
            parameters.add(new BasicNameValuePair("weight", new DecimalFormat("#.###", new DecimalFormatSymbols(Locale.US)).format(request.weight)));
        }

        HttpClient client = new DefaultHttpClient();
		String endpoint = ECWID_API_ENDPOINT_PRODUCTS.replace("{storeId}", storeId);
		HttpPost post = new HttpPost(endpoint);
        post.setEntity(new UrlEncodedFormEntity(parameters, "UTF-8"));
        Logger.log("Product API request: "+parameters);
        HttpResponse resp = client.execute(post);
        if (resp.getStatusLine().getStatusCode() != 200) {
            Logger.log("Product API request failed: "+resp.getStatusLine());
            throw new IOException(resp.getStatusLine().toString());
        }
        int productId = parseProductApiResponse(resp);
        Logger.log("Product API request succeded!");
        return productId;
    }

    private Integer parseProductApiResponse(HttpResponse resp) {
        InputStream stream = null;
        String result = null;
        try {
            HttpEntity entity = resp.getEntity();
            stream = entity.getContent();
            BufferedReader reader = new BufferedReader(new InputStreamReader(stream));
            StringBuilder sb = new StringBuilder();
            String line = null;

            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }

            result = sb.toString();
        } catch (IOException e) {
            // Все плохо
            return null;
        } finally {
            try {
                if (stream != null) {
                    stream.close();
                }
            } catch (Exception e) {
                // Все очень плохо
                return null;
            }
        }

        JSONObject jObject = null;
        try {
            jObject = new JSONObject(result);
            int productId = jObject.getInt("id");
            return productId;
        } catch (JSONException e) {
        }
        return null;
    }

    public void uploadImage(int productId, String ownerId, String fileName) throws IOException {
        List<NameValuePair> parameters = new ArrayList<NameValuePair>();

        parameters.add(new BasicNameValuePair("token", token));

        HttpClient client = new DefaultHttpClient();
        String endpoint = ECWID_API_ENDPOINT_PRODUCT_IMAGE_UPLOAD.replace("{storeId}", ""+ownerId).replace("{productId}", "" + productId);
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
