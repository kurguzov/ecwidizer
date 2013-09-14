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
import android.widget.TextView;

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

    private String imageUrl;

	/**
	 * Обработчик сохранения картинки
	 */
	class ImageSaver implements PhotoManager.SaveImageCallback, S3Manager.ImageUploadedConsumer {
		@Override
		public void onSuccess(String filename) {
            setBusy(true);
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
            setBusy(false);
            Main.this.imageUrl = imageUri;
        }
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
        setBusy(false);
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
        ((TextView) findViewById(R.id.productNameText)).setText(name);
    }

    public void captureProductDescr(View view) {
        voiceManager.captureDescr(this);
    }

    public void setProductDescr(String descr) {
        Logger.log("PRODUCT DESCR: "+descr);
    }

	public void addProductClicked(View view) {
        Logger.log("ADD PRODUCT BUTTON");
        setBusy(true);

        final CreateProductRequest req = new CreateProductRequest();
        req.ownerid = 3111011;
        req.name = ((TextView) findViewById(R.id.productNameText)).getText().toString();
        req.description = null;

        String priceStr = ((TextView) findViewById(R.id.productPriceText)).getText().toString();
        try {
            req.price = Double.parseDouble(priceStr);
        } catch (NumberFormatException e) {
            Logger.error("Жжош!", e);
        }
        req.weight = 123.456;
        req.images = Arrays.asList(imageUrl);

        Thread thread = new Thread() {
            @Override
            public void run() {
                long start = System.currentTimeMillis();
                try {
                    new ProductApiRequestor().createProduct(req);
                    clearFields();
                    try {
                        Thread.sleep(Math.max(1, 2000-(System.currentTimeMillis()-start)));
                    } catch (InterruptedException e) {
                    }
                } catch (IOException e) {
                    Logger.error("Платформа - ебаное говно, живи с этим.", e);
                } finally {
                    setBusy(false);
                }
            }
        };
        thread.start();
	}

	private void clearFields() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                ImageView mImageView = (ImageView) findViewById(R.id.imageView);
                mImageView.setImageBitmap(null);
                imageUrl = null;
                ((TextView) findViewById(R.id.productNameText)).setText("");
                ((TextView) findViewById(R.id.productPriceText)).setText("");
            }
        });
	}

	private void setBusy(final boolean busy) {
        runOnUiThread(new Runnable() {
            public void run() {
                findViewById(R.id.addProductButton).setEnabled(!busy);
                findViewById(R.id.loadingIndicator).setVisibility(busy ? View.VISIBLE : View.INVISIBLE);
            }
        });
    }
}
