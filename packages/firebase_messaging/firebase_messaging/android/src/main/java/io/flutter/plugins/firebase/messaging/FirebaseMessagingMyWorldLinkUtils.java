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
  public static void sendNotificationReadStatus(final Context context,final String userAction,
                                                final String messageType, final String singleMessageId, final String username, final String executionId,final String token) {
    RequestQueue requestQueue = Volley.newRequestQueue(context);
    String url = "https://custmobileapp.worldlink.com.np/app/v2/notification/" + username + "/action/" + executionId;
    Log.e("URL VOLLEY PACKAGE", url);
    StringRequest request = new StringRequest(com.android.volley.Request.Method.PATCH, url, response -> {
      Log.e("PACKAGE SUCCESS ", url);

    }, error -> {
      Log.e("PACKAGE ERROR ", url);

    }){
      @Override
      protected Map<String, String> getParams() {
        HashMap<String, String> hashMap = new HashMap<String, String>();
        hashMap.put("user_action", userAction);
        hashMap.put("message_type", messageType);
        hashMap.put("message_id", singleMessageId);
//                hashMap.put("token",HelperMethods.getKeyFromPref(context));
        return hashMap;
      }

      @Override
      public Map<String, String> getHeaders() {
        Map<String, String> params = new HashMap<String, String>();
        params.put("Content-Type", "application/x-www-form-urlencoded");
        params.put("Authorization", "Bearer " + token);
        return params;
      }
    };
    requestQueue.add(request);
  }
}
