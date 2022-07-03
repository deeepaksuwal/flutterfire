package io.flutter.plugins.firebase.messaging;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

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
  public static void sendNotificationReadStatus(final Context mContext,
                                                final String messageType, final String singleMessageId, final String username, final String executionId) {
    RequestQueue requestQueue = Volley.newRequestQueue(context);
    String url = "https://custmobileapp.worldlink.com.np/app/v2/notification/" + username + "/action/" + executionId;
    Log.e("URL VOLLEY", url);
    StringRequest request = new StringRequest(com.android.volley.Request.Method.PATCH, url,
      new com.android.volley.Response.Listener<String>() {

        @Override
        public void onResponse(String response) {
          Log.d("asdff", "onResponse: " + response);
          Toast.makeText(mContext, "Success : " + response, Toast.LENGTH_LONG).show();
        }
      }, new com.android.volley.Response.ErrorListener() {
      @Override
      public void onErrorResponse(VolleyError error) {
//                Log.d("asdf", "onErrorResponse: "+error);
        Log.d("asdf url ", "onErrorResponse: " + url);
      }
    }) {
      @Override
      protected Map<String, String> getParams() {
        HashMap<String, String> hashMap = new HashMap<String, String>();
        hashMap.put("user_action", "seen");
        hashMap.put("message_type", messageType);
        hashMap.put("message_id", singleMessageId);
//                hashMap.put("token",HelperMethods.getKeyFromPref(mContext));
        return hashMap;
      }

      @Override
      public Map<String, String> getHeaders() {
        Map<String, String> params = new HashMap<String, String>();
        params.put("Content-Type", "application/x-www-form-urlencoded");
        params.put("Authorization", "Bearer " + HelperMethods.getKeyFromPref(mContext));
        return params;
      }
    };
    requestQueue.add(request);
  }
}
