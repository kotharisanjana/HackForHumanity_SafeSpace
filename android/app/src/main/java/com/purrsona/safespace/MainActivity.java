package com.purrsona.safespace;

import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.text.Layout;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.TileOverlay;
import com.google.android.gms.maps.model.TileOverlayOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.maps.android.heatmaps.HeatmapTileProvider;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback {
    private Api api;

    private Button buttonDistress;
    private View imageBorder, buttonLocateMe;
    private CheckBox checkBoxOpenStores;
    private View layoutSos;

    private GoogleMap googleMap;
    private Marker marker;
    private Location location;

    private Vibrator vibrator;

    private JSONArray openStoresArray;
    private Marker[] openStoresMarkers;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);

        buttonDistress = findViewById(R.id.button_distress);
        buttonLocateMe = findViewById(R.id.button_locate_me);
        imageBorder = findViewById(R.id.image_border);
        checkBoxOpenStores = (CheckBox) findViewById(R.id.checkbox_open_stores);
        layoutSos = findViewById(R.id.layout_sos);

        layoutSos.setOnClickListener(view -> {
            takeMeToSosDialer();
        });

//        imageBorder.setOnClickListener(view -> {
//            takeMeToSosDialer();
//        });

        buttonDistress.setOnClickListener(view -> {
            if (((Button) view).getText().toString().toLowerCase().startsWith("enable")) {
                buttonDistress.setText("Disable Distress");
                imageBorder.setVisibility(View.VISIBLE);
                layoutSos.setVisibility(View.VISIBLE);
                buttonDistress.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.h4h_red)));
                vibrator.vibrate(500);

                // send whatsapp messages
                for (String number : getContactsList(getApplicationContext())) {
                    number = number.replaceAll("[^0-9]", "");
                    api.getMessage(number, String.valueOf(location.getLatitude()), String.valueOf(location.getLongitude()), response -> {
                        Log.e("DISTRESS_MESSAGE", "Sent message.");
                    }, error -> {
                        error.printStackTrace();
                    });
                }
            } else {
                buttonDistress.setText("Enable Distress");
                imageBorder.setVisibility(View.GONE);
                layoutSos.setVisibility(View.GONE);
                buttonDistress.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.h4h2)));
                vibrator.vibrate(VibrationEffect.EFFECT_TICK);

            }
        });

        buttonLocateMe.setOnClickListener(view -> {
            locateMe(googleMap);
        });

        checkBoxOpenStores.setOnCheckedChangeListener((compoundButton, b) -> {
            // removing old open stores markers
            removeOpenStoresMarkers();

            // get open places
            api.postOpen(googleMap.getCameraPosition().target.latitude, googleMap.getCameraPosition().target.longitude, response -> {
                try {
                    openStoresArray = new JSONArray(response);

                    if (b) {
                        openStoresMarkers = new Marker[openStoresArray.length()];

                        for (int i = 0; i < openStoresArray.length(); i++) {
                            try {
                                JSONObject openStore = openStoresArray.getJSONObject(i);

                                openStoresMarkers[i] = googleMap.addMarker(new MarkerOptions()
                                        .position(new LatLng(openStore.getDouble("latitude"), openStore.getDouble("longitude")))
                                        .icon(BitmapDescriptorFactory.fromBitmap(createBitmapFromDrawable(getResources().getDrawable(R.drawable.marker_open)))));
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }, error -> {
                error.printStackTrace();
            });
        });

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        api = Api.getInstance(getApplicationContext());

        Utilities.enqueueWork(getApplicationContext());
    }

    private void takeMeToSosDialer() {
        Intent callIntent = new Intent(Intent.ACTION_DIAL);
        callIntent.setData(Uri.parse("tel:911"));
        startActivity(callIntent);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_settings:
                startActivity(new Intent(MainActivity.this, SettingsActivity.class));
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        this.googleMap = googleMap;

        googleMap.setOnMarkerClickListener(marker -> {
            Utilities.launchGMaps(getApplicationContext(), marker.getPosition());
            return false;
        });

        api.getMarkers(
                response -> {
                    try {
                        JSONArray array = new JSONArray(response);

                        for (int i = 0; i < array.length(); i++) {
                            JSONObject obj = array.getJSONObject(i);
                            double lat = obj.getDouble("centroidLat");
                            double lng = obj.getDouble("centroidLong");
//                            JSONArray dpArr = obj.getJSONArray("dp");

//                            List<LatLng> latLngs = new ArrayList<>();
//                            for (int j = 0; j < dpArr.length(); j++) {
//                                JSONArray latLngArr = dpArr.getJSONArray(j);
//                                latLngs.add(new LatLng(latLngArr.getDouble(0), latLngArr.getDouble(1)));
//                            }

                            View view = createMarker(getResources().getColor(R.color.h4h1), obj.getInt("personCount"));

                            LatLng cluster = new LatLng(lat, lng);
                            googleMap.addMarker(new MarkerOptions()
                                    .position(cluster)
                                    .icon(BitmapDescriptorFactory.fromBitmap(createBitmapFromView(view))));

                            // heat-map
//                            if (latLngs.size() > 0) {
//                                HeatmapTileProvider provider = new HeatmapTileProvider.Builder()
//                                        .data(latLngs)
//                                        .radius(50)
//                                        .build();
//
//                                // Add a tile overlay to the map, using the heat map tile provider.
//                                TileOverlay overlay = googleMap.addTileOverlay(new TileOverlayOptions().tileProvider(provider));
//                            }

                            googleMap.addCircle(getCircle(cluster, obj.getInt("radius")));
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

//                    googleMap.setOnCameraIdleListener(() -> {
//                        Toast.makeText(this, "Ok, bud.", Toast.LENGTH_SHORT).show();
//                    });
                },
                error -> {
                    Toast.makeText(this, "" + error.getMessage(), Toast.LENGTH_SHORT).show();
                }
        );

        locateMe(googleMap);
    }

    @SuppressLint("MissingPermission")
    private void locateMe(@NonNull GoogleMap googleMap) {
        FusedLocationProviderClient fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        fusedLocationProviderClient.getLastLocation().addOnSuccessListener(location -> {
            if (location == null) {
                Toast.makeText(MainActivity.this, "Location is null.", Toast.LENGTH_SHORT).show();
                return;
            }

            this.location = location;

            LatLng me = new LatLng(location.getLatitude(), location.getLongitude());
            if (marker != null) marker.remove();
            marker = googleMap.addMarker(new MarkerOptions()
                    .position(me)
                    .title("You")
                    .icon(BitmapDescriptorFactory.fromBitmap(createBitmapFromDrawable(getResources().getDrawable(R.drawable.marker_me)))));

            googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(me, 20));
        });
    }

    private void removeOpenStoresMarkers() {
        if (openStoresMarkers != null)
            for (int i = 0; i < openStoresMarkers.length; i++)
                openStoresMarkers[i].remove();
    }

    @Override
    public void onPointerCaptureChanged(boolean hasCapture) {
        super.onPointerCaptureChanged(hasCapture);
    }

    private Bitmap createBitmapFromView(View view) {
        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        view.setLayoutParams(new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT));

        view.measure(displayMetrics.widthPixels, displayMetrics.heightPixels);
        view.layout(0, 0, displayMetrics.widthPixels, displayMetrics.heightPixels);
        view.buildDrawingCache();
        Bitmap bitmap = Bitmap.createBitmap(view.getMeasuredWidth(), view.getMeasuredHeight(), Bitmap.Config.ARGB_8888);

        Canvas canvas = new Canvas(bitmap);
        view.draw(canvas);

        return bitmap;
    }

    private Bitmap createBitmapFromDrawable(Drawable vectorDrawable) {
        Bitmap bitmap = Bitmap.createBitmap(vectorDrawable.getIntrinsicWidth(), vectorDrawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);

        vectorDrawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        vectorDrawable.draw(canvas);

        return bitmap;
    }


    public View createMarker(int markerColor, int count) {
        RelativeLayout view = (RelativeLayout) getLayoutInflater().inflate(R.layout.marker, null);

        ((ImageView) view.findViewById(R.id.marker_view)).setImageTintList(ColorStateList.valueOf(markerColor));

        ((TextView) view.findViewById(R.id.marker_count)).setText(String.valueOf(count));
        return view;
    }

    public CircleOptions getCircle(LatLng latLng, int r) {
        CircleOptions circleOptions = new CircleOptions()
                .center(latLng)
                .radius(r)
                .strokeWidth(0);
        circleOptions.fillColor(Color.parseColor("#5001676B"));
        return circleOptions;
    }

    public List<String> getContactsList(Context context) {
        try {
            JSONArray contactsArray = new JSONArray(context.getSharedPreferences(context.getResources().getString(R.string.app_name), MODE_PRIVATE).getString("contacts", "[]"));

            List<String> numbers = new ArrayList<>();
            for (int i = 0; i < contactsArray.length(); i++) {
                JSONObject contact = contactsArray.getJSONObject(i);
                numbers.add(contact.getString("number"));
            }

            return numbers;
        } catch (JSONException e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }
}