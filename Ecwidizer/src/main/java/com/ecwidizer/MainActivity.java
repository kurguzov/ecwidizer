package com.ecwidizer;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.FragmentActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.ecwidizer.api.CreateProductRequest;
import com.ecwidizer.api.ProductApiRequestor;
import com.ecwidizer.api.VoiceManager;

import java.io.IOException;

public class MainActivity extends FragmentActivity {

	public static final String TAG = "ecwidizer";

	private final PhotoManager photoManager = new PhotoManager();
    private final VoiceManager voiceManager = new VoiceManager();

    private String imageFile;

	/**
	 * Обработчик сохранения картинки
	 */
	class ImageSaver implements PhotoManager.SaveImageCallback {
		@Override
		public void onSuccess(String filename) {
			MainActivity.this.imageFile = filename;
		}

		@Override
		public void onFailure(Throwable e) {
			Logger.error("Failed to save image: " + e.getMessage(), e);
			showErrorMessage("Failed to save image: " + e.getMessage());
		}

	}

	private void showErrorMessage(final String message) {
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				new AlertDialog.Builder(MainActivity.this)
						.setTitle("Error occured")
						.setMessage(message)
						.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int which) {
								// VOID
							}
						})
						.show();
			}
		});
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main_activity);
        setBusy(false);

		initApplication();
    }

	private void initApplication() {
		EcwidizerSettings.create(this);

		if (!EcwidizerSettings.get().isConnectedWithEcwid()) {
			Intent intent = new Intent(this, WelcomeActivity.class);
			startActivity(intent);
		}

		if (!isExternalStorageWritable()) {
			showNoSDCardError();
		}
	}

	private void showNoSDCardError() {
        showErrorMessage(getResources().getString(R.string.external_storage_not_available));
    }

    private boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            return true;
        }
        return false;
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
		try {
			photoManager.takePhoto(this);
		} catch (Exception e) {
			Logger.error("Failed to save picture", e);
			showErrorMessage("Failed to save picture: " + e.getMessage());
		}
	}

	public void setProductThumbnail(Bitmap bitmap) {
		ImageView mImageView = (ImageView) findViewById(R.id.imageView);
		mImageView.setImageBitmap(bitmap);
	}

    public void captureProductName(View view) {
        VoiceManager.captureName(this);
    }

    public void setProductName(String name) {
        name = capitalize(name);
        Logger.log("PRODUCT NAME: "+name);
        ((TextView) findViewById(R.id.productNameText)).setText(name);
    }

	public void addProductClicked(View view) {
        Logger.log("ADD PRODUCT BUTTON");
		TextView nameField = (TextView) findViewById(R.id.productNameText);
		String nameValue = nameField.getText().toString();
		if (nameValue.trim().isEmpty()) {
			nameField.setError("Product name cannot be empty");
			return;
		}

		setBusy(true);

		final CreateProductRequest req = new CreateProductRequest();
		req.name = nameValue;
		req.sku = generateSKU();

		req.description = null;

        String priceStr = ((TextView) findViewById(R.id.productPriceText)).getText().toString();
        try {
            req.price = Double.parseDouble(priceStr);
        } catch (NumberFormatException e) {
			req.price = 0.0;
        }
        req.weight = 0.0;

        Thread thread = new Thread() {
            @Override
            public void run() {
                long start = System.currentTimeMillis();
				try {
					EcwidizerSettings settings = EcwidizerSettings.get();
                    ProductApiRequestor productApiRequestor = new ProductApiRequestor(settings);
                    Integer product = productApiRequestor.createProduct(req);
					if (imageFile != null) {
						productApiRequestor.uploadImage(product, settings.getStoreId(), imageFile);
					}
                    clearFields();
					runOnUiThread(new Runnable() {
						@Override
						public void run() {
							Toast.makeText(MainActivity.this, "Product successfully uploaded.", Toast.LENGTH_SHORT).show();
						}
					});
				} catch (IOException e) {
                    Logger.error("Unable to reach Ecwid API", e);
                    showErrorMessage("Failed to create product. Ecwid returned error: " + e.getMessage());
                } catch (Exception e) {
                    Logger.error("Unable to create product", e);
					showErrorMessage("Failed to create product: " + e.getMessage());
                } finally {
                    try {
                        Thread.sleep(Math.max(1, 2000-(System.currentTimeMillis()-start)));
                    } catch (InterruptedException e) { }
                    setBusy(false);
                }
            }
        };
        thread.start();
	}

	private String generateSKU() {
		return "ECW-" + System.currentTimeMillis();
	}

	private void clearFields() {
        runOnUiThread(new Runnable() {
			@Override
			public void run() {
				try {
					ImageView mImageView = (ImageView) findViewById(R.id.imageView);
					mImageView.setImageBitmap(null);
					mImageView.setImageDrawable(getResources().getDrawable(R.drawable.camera_small));

					imageFile = null;
					((TextView) findViewById(R.id.productNameText)).setText("");
					((TextView) findViewById(R.id.productPriceText)).setText("");
				} catch (Resources.NotFoundException e) {
					Logger.kernelPanic("unable to clear fields: " + e.getMessage());
				}
			}
		});
	}

    private String capitalize(String s) {
        if (s.isEmpty()) {
            return s;
        } else {
            return Character.toUpperCase(s.charAt(0)) + s.substring(1);
        }
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
