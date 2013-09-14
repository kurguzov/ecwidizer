package com.ecwidizer;

import android.content.Intent;
import android.os.Bundle;
import android.app.Activity;
import android.view.Menu;
import android.view.View;

import com.ecwidizer.api.CreateProductRequest;
import com.ecwidizer.api.ProductApiRequestor;

import java.io.IOException;
import java.util.Arrays;

public class Main extends Activity {

	private PhotoManager photoManager = new PhotoManager();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		photoManager.dispatchActivityResult(this, requestCode, resultCode, data);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	public void takePhotoClicked(View view) {
        // Тестирование API. TODO: удалить сей говнокод
        Logger.log("TEST1");
        Logger.error("TEST1");
        new Thread() {
            @Override
            public void run() {
                ProductApiRequestor.test();
            }
        }.start();

		photoManager.takePhoto(this);
	}
}
