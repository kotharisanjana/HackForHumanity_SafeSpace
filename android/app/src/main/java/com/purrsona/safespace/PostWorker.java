package com.purrsona.safespace;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;

import java.time.Duration;

public class PostWorker extends Worker {
    private Api api;

    public PostWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
        api = Api.getInstance(getApplicationContext());
    }

    @Override
    @SuppressLint("MissingPermission")
    public Result doWork() {
        Log.e("WORKER", "Starting with work ...");

        FusedLocationProviderClient fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(getApplicationContext());
        fusedLocationProviderClient.getLastLocation().addOnSuccessListener(location -> {
            if (location == null) return;

            api.postMe(location.getLatitude(), location.getLongitude(), response -> {
                Log.e("POST_ME", "Done with this.");
            }, error -> {
                error.printStackTrace();
            });

            Utilities.enqueueWork(getApplicationContext());
        });

        return Result.success();
    }
}
