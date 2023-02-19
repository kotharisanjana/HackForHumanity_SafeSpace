package com.purrsona.safespace;

import static android.content.Context.MODE_PRIVATE;

import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.net.Uri;

import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;

import com.google.android.gms.maps.model.LatLng;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

public class Utilities {
    public static void enqueueWork(Context context) {
        WorkManager.getInstance(context).cancelAllWork();
        WorkManager.getInstance(context)
                .beginWith(new OneTimeWorkRequest.Builder(PostWorker.class)
                        .setInitialDelay(Duration.ofSeconds(Constants.INTERVAL_POST))
                        .build())
                .enqueue();
    }

    public static void launchGMaps(Context context, LatLng latLng) {
        // Create a Uri from an intent string. Use the result to create an Intent.
        Uri gmmIntentUri = Uri.parse("geo:0,0?q=" + latLng.latitude + "," + latLng.longitude);

        // Create an Intent from gmmIntentUri. Set the action to ACTION_VIEW
        Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
        mapIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        // Make the Intent explicit by setting the Google Maps package
        mapIntent.setPackage("com.google.android.apps.maps");

        // Attempt to start an activity that can handle the Intent
        context.startActivity(mapIntent);
    }
}
