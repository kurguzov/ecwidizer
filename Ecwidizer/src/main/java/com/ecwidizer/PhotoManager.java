package com.ecwidizer;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.widget.ImageView;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/**
 * Created by igor on 9/13/13.
 */
public class PhotoManager {

	private static final int TAKE_PICTURE = 1;
	public static final String FILE_NAME_KEY = "FILE_NAME_KEY";

	public void takePhoto(Activity activity) {
		try {
			Intent captureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
			if (!isIntentAvailable(activity.getApplicationContext(), captureIntent)) {
				throw new Exception("Image Capture is not available");
			}
			File f = createImageFile();
			Logger.log("Saving to file: " + f.getAbsolutePath());
			getStorage(activity).edit().putString(FILE_NAME_KEY, f.getAbsolutePath()).commit();
			captureIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(f));
			activity.startActivityForResult(captureIntent, TAKE_PICTURE);
		} catch (Exception e) {
			Logger.error("Failed to save picture", e);
		}
	}

	private SharedPreferences getStorage(Activity activity) {
		return activity.getPreferences(Context.MODE_PRIVATE);
	}

	public void dispatchActivityResult(Activity activity, int requestCode, int resultCode, Intent data) {
		try {
			if (requestCode != TAKE_PICTURE) {
				throw new Exception("Invalid action: " + requestCode);
			}
			if (resultCode == Activity.RESULT_OK) {
				if (data != null) {
					// Image captured and saved to fileUri specified in the Intent
					Logger.log("Image saved to " + data);
					handleSmallCameraPhoto(activity, data);
				} else {
					String fileName = getStorage(activity).getString(FILE_NAME_KEY, null);
					if (fileName == null || fileName.isEmpty()) {
						throw new Exception("File name undefined: '" + fileName + "'");
					}
					Bitmap bitmap = BitmapFactory.decodeFile(fileName);
					if (bitmap == null) {
						throw new Exception("Unable to decode image bitmap");
					}
					showImagePreview(activity, bitmap);
				}
			} else if (resultCode == Activity.RESULT_CANCELED) {
				// User cancelled the image capture
				Logger.log("User cancelled image input");
			} else {
				// Image capture failed, advise user
				throw new Exception("Invalid result code: " + requestCode);
			}
		} catch (Exception e) {
			Logger.error("Unable to dispatch activity result #" + requestCode, e);
		}
	}

	private File createImageFile() throws IOException {
		// Create an image file name
		String timeStamp =
				new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
		String imageFileName = "product-" + timeStamp;
		File storageDir = new File(
				Environment.getExternalStoragePublicDirectory(
						Environment.DIRECTORY_PICTURES
				),
				"ecwid-photos"
		);
		if (!storageDir.exists()) {
			Logger.log("storageDir " + storageDir.getAbsolutePath() + " doesn't exist, creating...");
			storageDir.mkdir();
		}
		Logger.log("storageDir: " + storageDir.getAbsolutePath() + "=>" + storageDir.isDirectory());
		if (!storageDir.isDirectory()) return null;

		File image = File.createTempFile(imageFileName, ".jpg", storageDir);
		return image;
	}

	private void handleSmallCameraPhoto(Activity activity, Intent intent) {
		Bundle extras = intent.getExtras();
		if (extras == null) return;
		Bitmap bitmap = (Bitmap) extras.get("data");
		showImagePreview(activity, bitmap);
	}

	private void showImagePreview(Activity activity, Bitmap bitmap) {
		ImageView mImageView = (ImageView) activity.findViewById(R.id.imageView);
		mImageView.setImageBitmap(bitmap);
	}

	private static boolean isIntentAvailable(Context context, Intent intent) {
		final PackageManager packageManager = context.getPackageManager();
		List<ResolveInfo> list =
				packageManager.queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY);
		return list.size() > 0;
	}

}
