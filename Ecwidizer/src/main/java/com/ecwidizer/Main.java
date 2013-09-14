package com.ecwidizer;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.app.Activity;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.ImageView;

import com.ecwidizer.S3.S3Manager;
import com.ecwidizer.S3.S3ManagerInitializeException;
import com.ecwidizer.api.CreateProductRequest;
import com.ecwidizer.api.ProductApiRequestor;

import java.io.IOException;
import java.util.Arrays;
import java.io.File;

public class Main extends Activity {

	private PhotoManager photoManager = new PhotoManager();

	/**
	 * Обработчик сохранения картинки
	 */
	class ImageSaver implements PhotoManager.SaveImageCallback, S3Manager.ImageUploadedConsumer {
		@Override
		public void onSuccess(String filename) {
			try {
				S3Manager.getInstance(getApplicationContext()).uploadToS3(new File(filename), this);
			} catch (S3ManagerInitializeException e) {
				onFailure(e);
			}
		}

		@Override
		public void onFailure(Exception e) {
			Logger.error("Failed to save image", e);
		}

		@Override
		public void imageUploaded(String imageUri) {
			Logger.log("Image uploaded to S3: " + imageUri);
			// Аркадий, жги
		}
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		photoManager.dispatchActivityResult(this, requestCode, resultCode, data, new ImageSaver());
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	public void takePhotoClicked(View view) {

//        // Тестирование API. TODO: удалить сей говнокод
//        Logger.log("TEST1");
//        Logger.error("TEST1");
//        new Thread() {
//            @Override
//            public void run() {
//                ProductApiRequestor.test();
//            }
//        }.start();
//
		photoManager.takePhoto(this);

    }

	public void setProductThumbnail(Bitmap bitmap) {
		ImageView mImageView = (ImageView) findViewById(R.id.imageView);
		mImageView.setImageBitmap(bitmap);
	}

}
