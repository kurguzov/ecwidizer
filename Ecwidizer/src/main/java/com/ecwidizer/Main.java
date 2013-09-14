package com.ecwidizer;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.app.Activity;
import android.util.Log;
import android.support.v4.app.FragmentActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;

import com.ecwidizer.S3.S3Manager;
import com.ecwidizer.S3.S3ManagerInitializeException;
import com.ecwidizer.api.CreateProductRequest;
import com.ecwidizer.api.ProductApiRequestor;
import com.ecwidizer.api.VoiceManager;

import java.io.IOException;
import java.util.Arrays;
import java.io.File;
public class Main extends FragmentActivity {

	private final PhotoManager photoManager = new PhotoManager();
    private final VoiceManager voiceManager = new VoiceManager();

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
		public void onFailure(Throwable e) {
			Logger.error("Failed to save image: " + e.getMessage(), e);
		}

		@Override
		public void imageUploaded(String imageUri) {
			Logger.log("Image uploaded to S3: " + imageUri);
			CreateProductRequest req = new CreateProductRequest();
            req.ownerid = 4;
            req.name = "Заебеквидов продукт №"+System.currentTimeMillis();
            req.description = "Описание заебеквидова продукта";
            req.price = 6.66;
            req.weight = 123.456;
            req.images = Arrays.asList(imageUri + ";http://img01.rl0.ru/pgc/c304x207/5233d273-7e9c-e8c1-7e9c-e8ce65d4737d.photo.0.jpg");
            try {
                new ProductApiRequestor().createProduct(req);
            } catch (IOException e) {
                Logger.error("aaaaa", e);
            }

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
        voiceManager.dispatchActivityResult(this, requestCode, resultCode, data);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_settings:
                openSettings();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void openSettings() {
        SettingsDialogFragment settingsDialogFragment = new SettingsDialogFragment();
        settingsDialogFragment.show(getSupportFragmentManager(), "Settings");
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

    public void captureProductName(View view) {
        voiceManager.captureName(this);
    }

    public void setProductName(String name) {
        Logger.log("PRODUCT NAME: "+name);
    }

    public void captureProductDescr(View view) {
        voiceManager.captureDescr(this);
    }

    public void setProductDescr(String descr) {
        Logger.log("PRODUCT DESCR: "+descr);
    }
}
