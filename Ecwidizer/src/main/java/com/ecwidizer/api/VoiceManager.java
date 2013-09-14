package com.ecwidizer.api;

import android.app.Activity;
import android.content.Intent;
import android.speech.RecognizerIntent;

import com.ecwidizer.Logger;
import com.ecwidizer.Main;

import java.util.ArrayList;

/**
 * Created by basil on 14.09.13.
 */
public class VoiceManager {
    private static final int CAPTURE_NAME = 10;
    private static final int CAPTURE_DESCR = 11;

    public void captureName(Activity activity) {
        captureVoice(activity, CAPTURE_NAME);
    }

    public void captureDescr(Activity activity) {
        captureVoice(activity, CAPTURE_DESCR);
    }

    private void captureVoice(Activity activity, int requestCode) {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Voice recognition Demo...");
        activity.startActivityForResult(intent, requestCode);
    }

    public void dispatchActivityResult(Main activity, int requestCode, int resultCode, Intent data) {
        if (requestCode == CAPTURE_NAME || requestCode == CAPTURE_DESCR) {
            if (resultCode == Activity.RESULT_OK) {
                // Populate the wordsList with the String values the recognition engine thought it heard
                ArrayList<String> matches = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                Logger.log("RECOGNIZED WORDS: " + matches);
                if (requestCode == CAPTURE_NAME) {
                    activity.setProductName(matches.get(0));
                } else if (requestCode == CAPTURE_DESCR) {
                    activity.setProductDescr(matches.get(0));
                } else {
                    throw new AssertionError(requestCode);
                }
            } else {
                Logger.error("Unexpected result code: "+resultCode);
            }
        }
    }



}
