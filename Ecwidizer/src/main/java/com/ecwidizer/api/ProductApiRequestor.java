package com.ecwidizer.api;

import com.ecwidizer.Logger;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;

import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by basil on 13.09.13.
 */
public class ProductApiRequestor {

    public void createProduct(CreateProductRequest request) throws IOException {
        List<NameValuePair> parameters = new ArrayList<NameValuePair>();
        parameters.add(new BasicNameValuePair("ownerid", Integer.toString(request.ownerid)));

        if (request.name != null) {
            parameters.add(new BasicNameValuePair("name", request.name));
        }

        if (request.description != null) {
            parameters.add(new BasicNameValuePair("description", request.description));
        }

        if (request.price != null) {
            parameters.add(new BasicNameValuePair("price", new DecimalFormat("#.###").format(request.price)));
        }

        if (request.weight != null) {
            parameters.add(new BasicNameValuePair("weight", new DecimalFormat("#.###").format(request.weight)));
        }

        if (request.images != null && !request.images.isEmpty()) {
            StringBuilder sb = new StringBuilder();
            for (String image: request.images) {
                if (sb.length() > 0) sb.append(";");
                sb.append(image);
            }
            parameters.add(new BasicNameValuePair("images", sb.toString()));
        }

        HttpClient client = new DefaultHttpClient();
        HttpPost post = new HttpPost("http://appdev.ecwid.com/productcreate");
        Logger.log("Product API request: "+parameters);
        HttpResponse resp = client.execute(post);
        if (resp.getStatusLine().getStatusCode() != 200) {
            Logger.log("Product API request failed: "+resp.getStatusLine());
            throw new IOException(resp.getStatusLine().toString());
        }
        Logger.log("Product API request succeded!");
    }


    public static void test() {
        try {
            Logger.log("Request product started");
            CreateProductRequest req = new CreateProductRequest();
            req.ownerid = 4;
            req.name = "Заебеквидов продукт №"+System.currentTimeMillis();
            req.description = "Описание заебеквидова продукта";
            req.price = 6.66;
            req.weight = 123.456;
            req.images = Arrays.asList("http://upload.wikimedia.org/wikipedia/commons/thumb/9/93/Dildos_o_Consoladores_Nena.jpg/220px-Dildos_o_Consoladores_Nena.jpg;http://upload.wikimedia.org/wikipedia/commons/thumb/9/93/Dildos_o_Consoladores_Nena.jpg/220px-Dildos_o_Consoladores_Nena.jpg");
            new ProductApiRequestor().createProduct(req);
        } catch (IOException e) {
            Logger.error("!!!!!!! "+e.toString());
        }

    }

}
