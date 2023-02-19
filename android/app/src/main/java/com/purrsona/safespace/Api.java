package com.purrsona.safespace;

import static android.content.Context.MODE_PRIVATE;

import android.content.Context;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.UUID;

public class Api {
    private static Api api;
    private static RequestQueue queue;

//    private static final String BASE_URL = "http://172.20.214.50:3000";
    private static final String BASE_URL = "http://172.20.194.102:8080";
    private static final String BASE_URL_FLASK = "http://172.20.197.227:5000";

    public static String uid = null;

    public Api(Context context) {
        if (queue == null) queue = Volley.newRequestQueue(context);

        // initialize ui
        Api.uid = context.getSharedPreferences(context.getResources().getString(R.string.app_name), MODE_PRIVATE).getString("uid", null);
        if (Api.uid == null) {
            Api.uid = UUID.randomUUID().toString();
            context.getSharedPreferences(context.getResources().getString(R.string.app_name), MODE_PRIVATE).edit().putString("uid", Api.uid).apply();
        }
    }

    public static Api getInstance(Context context) {
        if (api == null) api = new Api(context);
        return api;
    }

    public void getMarkers(Response.Listener<String> onSuccess, Response.ErrorListener onError) {
        StringRequest stringRequest = new StringRequest(
                Request.Method.GET,
                BASE_URL + "/cluster",
                onSuccess,
                onError);

        queue.add(stringRequest);
    }


    public void postMe(Double lat, Double lng, Response.Listener<String> onSuccess, Response.ErrorListener onError) {
        StringRequest stringRequest = new StringRequest(
                Request.Method.POST,
                BASE_URL + "/location/save",
                onSuccess,
                onError) {
            @Override
            public String getBodyContentType() {
                return "application/json; charset=utf-8";
            }

            @Override
            public byte[] getBody() throws AuthFailureError {
                try {
                    JSONObject requestBody = new JSONObject();
                    requestBody.put("uid", Api.uid);
                    requestBody.put("latitude", lat);
                    requestBody.put("longitude", lng);
                    requestBody.put("radius", 0);

                    return requestBody.toString().getBytes("utf-8");
                } catch (Exception uee) {
                    uee.printStackTrace();
                    return null;
                }
            }
        };

        queue.add(stringRequest);
    }

    public void getMessage(String phone, String lat, String lng, Response.Listener<String> onSuccess, Response.ErrorListener onError) {
        StringRequest stringRequest = new StringRequest(
                Request.Method.GET,
                BASE_URL + String.format("/generic/message?to=%s&lat=%s&lng=%s", phone, lat, lng),
                onSuccess,
                onError);

        queue.add(stringRequest);
    }

    public void postOpen(Double lat, Double lng, Response.Listener<String> onSuccess, Response.ErrorListener onError) {
        StringRequest stringRequest = new StringRequest(
                Request.Method.POST,
                BASE_URL_FLASK + "/nearbyopenplaces",
                onSuccess,
                onError) {
            @Override
            public String getBodyContentType() {
                return "application/json; charset=utf-8";
            }

            @Override
            public byte[] getBody() throws AuthFailureError {
                try {
                    JSONObject requestBody = new JSONObject();
                    requestBody.put("latitude", lat);
                    requestBody.put("longitude", lng);

                    return requestBody.toString().getBytes("utf-8");
                } catch (Exception uee) {
                    uee.printStackTrace();
                    return null;
                }
            }
        };

        queue.add(stringRequest);
    }
}
