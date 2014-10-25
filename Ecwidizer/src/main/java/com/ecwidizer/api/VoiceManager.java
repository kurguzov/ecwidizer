package com.ecwidizer.api;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.speech.RecognizerIntent;
import android.widget.Toast;

import com.ecwidizer.Logger;
import com.ecwidizer.Main;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by basil on 14.09.13.
 */
public class VoiceManager {
    private static final int CAPTURE_NAME = 10;

    public static void captureName(Activity activity) {
        captureVoice(activity, CAPTURE_NAME);
    }

    private static void captureVoice(Activity activity, int requestCode) {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Voice recognition Demo...");

        if (isSpeechRecognitionActivityPresented(activity)) {
            activity.startActivityForResult(intent, requestCode);
        } else {
            Toast.makeText(activity.getApplicationContext(),
                    "Ops! Your device doesn't support Speech to Text",
                    Toast.LENGTH_SHORT).show();
            installGoogleVoiceSearch(activity);
        }
    }

    private static boolean isSpeechRecognitionActivityPresented(Activity activity) {
        try {
            PackageManager pm = activity.getPackageManager();
            List activities = pm.queryIntentActivities(new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH), 0);
            if (activities.size() != 0) return true;
        } catch (Exception ignored) {
        }
        return false;
    }

	private static void installGoogleVoiceSearch(final Activity activity) {
		Dialog dialog = new AlertDialog.Builder(activity)
				.setMessage("Do you really want to install \"Google Voice Search\" from Google Play Market?")
				.setTitle("Attention")
				.setPositiveButton("Install", new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						try {
							Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=com.google.android.voicesearch"));
							intent.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY | Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
							activity.startActivity(intent);
						} catch (Exception ex) {
							Toast.makeText(activity.getApplicationContext(),
									"Ops! Your device doesn't have installed Google Play Application",
									Toast.LENGTH_SHORT).show();
						}
					}})
				.setNegativeButton("Cancel", null)
				.create();
		dialog.show();
	}

    public void dispatchActivityResult(Main activity, int requestCode, int resultCode, Intent data) {
        if (requestCode == CAPTURE_NAME) {
            if (resultCode == Activity.RESULT_OK) {
                // Populate the wordsList with the String values the recognition engine thought it heard
                ArrayList<String> matches = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                Logger.log("RECOGNIZED WORDS: " + matches);
                activity.setProductName(matches.get(0));
            } else {
                Logger.error("Unexpected result code: "+resultCode);
            }
        } else {
			Logger.error("Unexpected request code: "+requestCode);
		}
    }

}
