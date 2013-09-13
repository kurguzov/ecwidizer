package com.ecwidizer;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
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
	private static final int SAVE_PICTURE = 2;

	public void takePhoto(Activity activity) {
		Intent captureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
		if (isIntentAvailable(activity.getApplicationContext(), captureIntent)) {
			activity.startActivityForResult(captureIntent, TAKE_PICTURE);
		} else {
			Logger.error("Image Capture is not available");
		}
	}

	public void dispatchActivityResult(Activity activity, int requestCode, int resultCode, Intent data) {
		switch (requestCode) {
			case TAKE_PICTURE:
				handleSmallCameraPhoto(activity, data);
				savePicture(activity, data);
				break;
			case SAVE_PICTURE:
				// push to kaktus
				break;
			default:
				Logger.error("Invalid action: " + requestCode);
		}
	}

	private void savePicture(Activity activity, Intent data) {
		try {
			File f = createImageFile();
			Logger.log("Saving to file: " + f.getAbsolutePath());
			data.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(f));
			activity.startActivityForResult(data, SAVE_PICTURE);
		} catch (IOException e) {
			e.printStackTrace();
			Logger.error("Failed to save picture", e);
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
