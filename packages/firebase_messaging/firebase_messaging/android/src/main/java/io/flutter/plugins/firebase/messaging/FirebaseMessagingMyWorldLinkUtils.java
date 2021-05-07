package io.flutter.plugins.firebase.messaging;

import android.content.Context;

import androidx.annotation.Nullable;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class FirebaseMessagingMyWorldLinkUtils {
  public static void sendNotificationReadStatus(final Context context, final String notificationId, final String status, final String username, String token) {
    RequestQueue requestQueue = Volley.newRequestQueue(context);
    StringRequest request = new StringRequest(Request.Method.POST,
      "https://custmobileapp.worldlink.com.np/app/v1/appEservice/update_notification", response -> {

    }, error -> {

    }) {
      @Override
      protected Map<String, String> getParams() {
        JSONArray notificationContents = new JSONArray();

        JSONObject jsonObject = new JSONObject();
        try {
          jsonObject.put("id", "cust_fcm." + notificationId);
          jsonObject.put("status", status);
          notificationContents.put(jsonObject);
        } catch (JSONException e) {
          e.printStackTrace();
        }

        HashMap<String, String> hashMap = new HashMap<>();
        hashMap.put("machine_name", username);
        hashMap.put("request_from", "auth-mobileapp");
        hashMap.put("notifications", String.valueOf(notificationContents));
        return hashMap;
      }

      @Override
      public Map<String, String> getHeaders() {
        Map<String, String> params = new HashMap<>();
        params.put("content-type", "application/x-www-form-urlencoded");
        params.put("authorization", "Bearer " + token);
        return params;

      }
    };
    requestQueue.add(request);
  }
}
