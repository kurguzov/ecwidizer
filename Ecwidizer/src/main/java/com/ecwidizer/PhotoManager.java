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
import android.util.Log;
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

	private void log(String message) {
		Log.i(PhotoManager.class.getName(), message);
	}

	private void savePicture(Intent data) {
		try {
			File f = createImageFile();
			System.out.println("Saving to file: " + f.getAbsolutePath());
			data.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(f));
//			startActivityForResult(data, 2);
		} catch (IOException e) {
			e.printStackTrace();
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
			log("storageDir " + storageDir.getAbsolutePath() + " doesn't exist, creating...");
			storageDir.mkdir();
		}
		log("storageDir: " + storageDir.getAbsolutePath() + "=>" + storageDir.isDirectory());
		if (!storageDir.isDirectory()) return null;

		File image = File.createTempFile(
				storageDir.getAbsolutePath() + "/" + imageFileName,
				".jpg"
		);
		return image;
	}

	public void takePhoto(Activity activity) {
		Intent captureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
		if (isIntentAvailable(activity.getApplicationContext(), captureIntent)) {
			activity.startActivityForResult(captureIntent, 1);
		}
	}

	private void handleSmallCameraPhoto(Intent intent) {
//		Bundle extras = intent.getExtras();
//		if (extras == null) return;
//		Bitmap bitmap = (Bitmap) extras.get("data");
//		ImageView mImageView = (ImageView) findViewById(R.id.imageView);
//		mImageView.setImageBitmap(bitmap);
	}

	private static boolean isIntentAvailable(Context context, Intent intent) {
		final PackageManager packageManager = context.getPackageManager();
		List<ResolveInfo> list =
				packageManager.queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY);
		return list.size() > 0;
	}

	public void dispatchActivityResult(int requestCode, int resultCode, Intent data) {

	}
}
