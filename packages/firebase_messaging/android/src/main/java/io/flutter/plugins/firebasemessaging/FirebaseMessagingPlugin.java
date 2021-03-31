// Copyright 2017 The Chromium Authors. All rights reserved.
// Use of this source code is governed by a BSD-style license that can be
// found in the LICENSE file.

package io.flutter.plugins.firebasemessaging;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.RemoteMessage;

import io.flutter.embedding.engine.plugins.FlutterPlugin;
import io.flutter.embedding.engine.plugins.activity.ActivityAware;
import io.flutter.embedding.engine.plugins.activity.ActivityPluginBinding;
import io.flutter.plugin.common.BinaryMessenger;
import io.flutter.plugin.common.MethodCall;
import io.flutter.plugin.common.MethodChannel;
import io.flutter.plugin.common.MethodChannel.MethodCallHandler;
import io.flutter.plugin.common.MethodChannel.Result;
import io.flutter.plugin.common.PluginRegistry.NewIntentListener;
import io.flutter.plugin.common.PluginRegistry.Registrar;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * FirebaseMessagingPlugin
 */
public class FirebaseMessagingPlugin extends BroadcastReceiver
  implements MethodCallHandler, NewIntentListener, FlutterPlugin, ActivityAware {

  private static final String CLICK_ACTION_VALUE = "FLUTTER_NOTIFICATION_CLICK";
  private static final String TAG = "FirebaseMessagingPlugin";
  public static final String NOTIFICATION_DELETE = "notification_delete";
  public static final String NOTIFICATION_OPEN = "notification_open";

  public static final String NOTIFICATION_TYPE_1 = "1";
  public static final String NOTIFICATION_TYPE_1_DATA = "packagename";
  public static final String NOTIFICATION_TYPE_2 = "2";
  public static final String NOTIFICATION_TYPE2_DATA = "link";

  public static final String NOTIFICATION_OPEN_APP = "open_app";
  public static final String NOTIFICATION_TYPE4 = "4";
  public static final String NOTIFICATION_TYPE5 = "5";
  public static final String NOTIFICATION_TYPE7 = "7";
  public static final String NOTIFICATION_TYPE9 = "9";

  public static final String NOTIFICATION_TYPE_10 = "10";


  public static final String NOTIFICATION_SUBJECT = "subject";
  public static final String NOTIFICATION_NOTICE = "notice";
  public static final String NOTIFICATION_LINK = "link";
  public static final String NOTIFICATION_SURVEYDATA = "SurveyData";
  public static final String NOTIFICATION_OPEN_PAGE = "OpenPage";
  public static final String NOTIFICATION_DISMISS = "DismissNotification";
  public static final String NOTIFICATION_LOGINPIN = "loginPin";
  public static final String NOTIFICATION_TYPE = "Type";
  public static final String NOTIFICATION_ID = "notification_id";
  public static final String NOTIFICATION_DATE = "notification_date";
  public static final String NOTIFICATION_EXTRA = "notification_extra";
  public static final int DELETE_NOTIFICATION_ID = 340;
  public static final String NOTIFICATION_OPEN_NOTIFICATIONPAGE = "NotificationPage";
  public static final String NOTIFICATION_IMAGE = "image";
  public static final String ACTION_DISMISS_NOTIFICATION = "NOTIFICATION_DISMISSED";
  public static final String ACTION_OPEN_NOTIFICATION = "NOTIFICATION_OPENED";
  public static final String MESSAGE_FCM = "FCM";
  private static final String NOTIFICATION_PASSWORD_DATA = "PasswordData";

  private MethodChannel channel;
  private Context applicationContext;
  private Activity mainActivity;
  private String notice = "";
  private String subject = "";
  private String description = "";
  private String image = "";
  private String link = "";
  private int type = 0;
  private Intent finalIntent;
  private String status = "";
  private String notificationId = "";
  private long date;


  public static void registerWith(Registrar registrar) {
    FirebaseMessagingPlugin instance = new FirebaseMessagingPlugin();
    instance.setActivity(registrar.activity());
    registrar.addNewIntentListener(instance);
    instance.onAttachedToEngine(registrar.context(), registrar.messenger());
  }

  private void onAttachedToEngine(Context context, BinaryMessenger binaryMessenger) {
    this.applicationContext = context;
    FirebaseApp.initializeApp(applicationContext);
    channel = new MethodChannel(binaryMessenger, "plugins.flutter.io/firebase_messaging");
    final MethodChannel backgroundCallbackChannel =
      new MethodChannel(binaryMessenger, "plugins.flutter.io/firebase_messaging_background");

    channel.setMethodCallHandler(this);
    backgroundCallbackChannel.setMethodCallHandler(this);
    FlutterFirebaseMessagingService.setBackgroundChannel(backgroundCallbackChannel);

    // Register broadcast receiver
    IntentFilter intentFilter = new IntentFilter();
    intentFilter.addAction(FlutterFirebaseMessagingService.ACTION_TOKEN);
    intentFilter.addAction(FlutterFirebaseMessagingService.ACTION_REMOTE_MESSAGE);
    LocalBroadcastManager manager = LocalBroadcastManager.getInstance(applicationContext);
    manager.registerReceiver(this, intentFilter);
  }

  private void setActivity(Activity flutterActivity) {
    this.mainActivity = flutterActivity;
  }

  @Override
  public void onAttachedToEngine(FlutterPluginBinding binding) {
    onAttachedToEngine(binding.getApplicationContext(), binding.getBinaryMessenger());
  }

  @Override
  public void onDetachedFromEngine(FlutterPluginBinding binding) {
    LocalBroadcastManager.getInstance(binding.getApplicationContext()).unregisterReceiver(this);
  }

  @Override
  public void onAttachedToActivity(ActivityPluginBinding binding) {
    binding.addOnNewIntentListener(this);
    this.mainActivity = binding.getActivity();
  }

  @Override
  public void onDetachedFromActivityForConfigChanges() {
    this.mainActivity = null;
  }

  @Override
  public void onReattachedToActivityForConfigChanges(ActivityPluginBinding binding) {
    binding.addOnNewIntentListener(this);
    this.mainActivity = binding.getActivity();
  }

  @Override
  public void onDetachedFromActivity() {
    this.mainActivity = null;
  }

  // BroadcastReceiver implementation.
  @Override
  public void onReceive(Context context, Intent intent) {
    String action = intent.getAction();

    if (action == null) {
      return;
    }

    if (action.equals(FlutterFirebaseMessagingService.ACTION_TOKEN)) {
      String token = intent.getStringExtra(FlutterFirebaseMessagingService.EXTRA_TOKEN);
      channel.invokeMethod("onToken", token);
    } else if (action.equals(FlutterFirebaseMessagingService.ACTION_REMOTE_MESSAGE)) {
//      RemoteMessage message =
//          intent.getParcelableExtra(FlutterFirebaseMessagingService.EXTRA_REMOTE_MESSAGE);
      Map<String, Object> content = parseRemoteMessage(intent);
//      channel.invokeMethod("onMessage", content);
      channel.invokeMethod("onMessage", content);
    }
    //Just for first commit
  }

  @NonNull
  private Map<String, Object> parseRemoteMessage(Intent intent) {
//    Map<String, Object> content = new HashMap<>();
//    content.put("data", message.getData());
//
//    RemoteMessage.Notification notification = message.getNotification();
//
//    Map<String, Object> notificationMap = new HashMap<>();
//
//    String title = notification != null ? notification.getTitle() : null;
//    notificationMap.put("title", title);
//
//    String body = notification != null ? notification.getBody() : null;
//    notificationMap.put("body", body);
//
//    content.put("notification", notificationMap);
//    return content;
    Map<String, Object> notification = new HashMap<>();
    Map<String, Object> analytics = new HashMap<>();
    Map<String, Object> data = new HashMap<>();
    subject = intent.getStringExtra(NOTIFICATION_SUBJECT) != null ? intent.getStringExtra(NOTIFICATION_SUBJECT) : "";
    link = intent.getStringExtra(NOTIFICATION_LINK) != null ? intent.getStringExtra(NOTIFICATION_LINK) : "";
    notice = intent.getStringExtra(NOTIFICATION_NOTICE) != null ? intent.getStringExtra(NOTIFICATION_NOTICE) : "";
    notificationId = intent.getStringExtra(NOTIFICATION_ID) != null ? intent.getStringExtra(NOTIFICATION_ID) : "";
    type = intent.getIntExtra(NOTIFICATION_TYPE, 0);
    date = intent.getLongExtra(NOTIFICATION_DATE, 0);

    notification.put("notificationSubject", subject);
    notification.put("notificationNotice", notice);
    notification.put("notificationId", notificationId);
    notification.put("notificationDate", date);
    notification.put("notificationType", type);
    notification.put("notificationLink", link);
    notification.put("notificationAction", NOTIFICATION_OPEN_APP);
    notification.put("notificationMessageType", MESSAGE_FCM);
    notification.put("notificationStatus", "3");
    notification.put("notificationImage", intent.getStringExtra(NOTIFICATION_IMAGE) != null ? intent.getStringExtra(NOTIFICATION_IMAGE) : "");
    analytics.put("analytic_label", "");
    analytics.put("analytic_action", ACTION_OPEN_NOTIFICATION);
    analytics.put("analytic_value", 1);

    data.put("notification", notification);
    data.put("analytics", analytics);
    data.put("surveyData", intent.getStringExtra(NOTIFICATION_SURVEYDATA));
    data.put("passwordData", intent.getStringExtra(NOTIFICATION_PASSWORD_DATA));
    return data;
  }

  @Override
  public void onMethodCall(final MethodCall call, final Result result) {
    /*  Even when the app is not active the `FirebaseMessagingService` extended by
     *  `FlutterFirebaseMessagingService` allows incoming FCM messages to be handled.
     *
     *  `FcmDartService#start` and `FcmDartService#initialized` are the two methods used
     *  to optionally setup handling messages received while the app is not active.
     *
     *  `FcmDartService#start` sets up the plumbing that allows messages received while
     *  the app is not active to be handled by a background isolate.
     *
     *  `FcmDartService#initialized` is called by the Dart side when the plumbing for
     *  background message handling is complete.
     */
    if ("FcmDartService#start".equals(call.method)) {
      long setupCallbackHandle = 0;
      long backgroundMessageHandle = 0;
      try {
        @SuppressWarnings("unchecked")
        Map<String, Long> callbacks = ((Map<String, Long>) call.arguments);
        setupCallbackHandle = callbacks.get("setupHandle");
        backgroundMessageHandle = callbacks.get("backgroundHandle");
      } catch (Exception e) {
        Log.e(TAG, "There was an exception when getting callback handle from Dart side");
        e.printStackTrace();
      }
      FlutterFirebaseMessagingService.setBackgroundSetupHandle(mainActivity, setupCallbackHandle);
      FlutterFirebaseMessagingService.startBackgroundIsolate(mainActivity, setupCallbackHandle);
      FlutterFirebaseMessagingService.setBackgroundMessageHandle(
        mainActivity, backgroundMessageHandle);
      result.success(true);
    } else if ("FcmDartService#initialized".equals(call.method)) {
      FlutterFirebaseMessagingService.onInitialized();
      result.success(true);
    } else if ("configure".equals(call.method)) {
      FirebaseInstanceId.getInstance()
        .getInstanceId()
        .addOnCompleteListener(
          new OnCompleteListener<InstanceIdResult>() {
            @Override
            public void onComplete(@NonNull Task<InstanceIdResult> task) {
              if (!task.isSuccessful()) {
                Log.w(TAG, "getToken, error fetching instanceID: ", task.getException());
                return;
              }
              channel.invokeMethod("onToken", task.getResult().getToken());
            }
          });
      if (mainActivity != null) {
        sendMessageFromIntent("onLaunch", mainActivity.getIntent());
      }
      result.success(null);
    } else if ("subscribeToTopic".equals(call.method)) {
      String topic = call.arguments();
      FirebaseMessaging.getInstance()
        .subscribeToTopic(topic)
        .addOnCompleteListener(
          new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
              if (!task.isSuccessful()) {
                Exception e = task.getException();
                Log.w(TAG, "subscribeToTopic error", e);
                result.error("subscribeToTopic", e.getMessage(), null);
                return;
              }
              result.success(null);
            }
          });
    } else if ("unsubscribeFromTopic".equals(call.method)) {
      String topic = call.arguments();
      FirebaseMessaging.getInstance()
        .unsubscribeFromTopic(topic)
        .addOnCompleteListener(
          new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
              if (!task.isSuccessful()) {
                Exception e = task.getException();
                Log.w(TAG, "unsubscribeFromTopic error", e);
                result.error("unsubscribeFromTopic", e.getMessage(), null);
                return;
              }
              result.success(null);
            }
          });
    } else if ("getToken".equals(call.method)) {
      FirebaseInstanceId.getInstance()
        .getInstanceId()
        .addOnCompleteListener(
          new OnCompleteListener<InstanceIdResult>() {
            @Override
            public void onComplete(@NonNull Task<InstanceIdResult> task) {
              if (!task.isSuccessful()) {
                Log.w(TAG, "getToken, error fetching instanceID: ", task.getException());
                result.success(null);
                return;
              }

              result.success(task.getResult().getToken());
            }
          });
    } else if ("deleteInstanceID".equals(call.method)) {
      new Thread(
        new Runnable() {
          @Override
          public void run() {
            try {
              FirebaseInstanceId.getInstance().deleteInstanceId();
              if (mainActivity != null) {
                mainActivity.runOnUiThread(
                  new Runnable() {
                    @Override
                    public void run() {
                      result.success(true);
                    }
                  });
              }
            } catch (IOException ex) {
              Log.e(TAG, "deleteInstanceID, error:", ex);
              if (mainActivity != null) {
                mainActivity.runOnUiThread(
                  new Runnable() {
                    @Override
                    public void run() {
                      result.success(false);
                    }
                  });
              }
            }
          }
        })
        .start();
    } else if ("autoInitEnabled".equals(call.method)) {
      result.success(FirebaseMessaging.getInstance().isAutoInitEnabled());
    } else if ("setAutoInitEnabled".equals(call.method)) {
      Boolean isEnabled = (Boolean) call.arguments();
      FirebaseMessaging.getInstance().setAutoInitEnabled(isEnabled);
      result.success(null);
    } else {
      result.notImplemented();
    }
  }

  @Override
  public boolean onNewIntent(Intent intent) {
    boolean res = sendMessageFromIntent("onResume", intent);
    if (res && mainActivity != null) {
      mainActivity.setIntent(intent);
    }
    return res;
  }

  /**
   * @return true if intent contained a message to send.
   */
  private boolean sendMessageFromIntent(String method, Intent intent) {
//    if (CLICK_ACTION_VALUE.equals(intent.getAction())
//        || CLICK_ACTION_VALUE.equals(intent.getStringExtra("click_action"))) {
//      Map<String, Object> message = new HashMap<>();
//      Bundle extras = intent.getExtras();intent.getStringExtra(NOTIFICATION_ID) != null ? intent.getStringExtra(NOTIFICATION_ID) : ""
//
//      channel.invokeMethod(method, message);
//      return true;
//    }
    Map<String, Object> notification = new HashMap<>();
    Map<String, Object> analytics = new HashMap<>();
    Map<String, Object> data = new HashMap<>();
    subject = intent.getStringExtra(NOTIFICATION_SUBJECT) != null ? intent.getStringExtra(NOTIFICATION_SUBJECT) : "";
    link = intent.getStringExtra(NOTIFICATION_LINK) != null ? intent.getStringExtra(NOTIFICATION_LINK) : "";
    notice = intent.getStringExtra(NOTIFICATION_NOTICE) != null ? intent.getStringExtra(NOTIFICATION_NOTICE) : "";
    notificationId = intent.getStringExtra(NOTIFICATION_ID) != null ? intent.getStringExtra(NOTIFICATION_ID) : "";
    type = intent.getIntExtra(NOTIFICATION_TYPE, 0);
    date = intent.getLongExtra(NOTIFICATION_DATE, 0);
    if (intent.getAction() != null) {
      switch (intent.getAction()) {
        case NOTIFICATION_TYPE_1:
          String appPackageName = intent.getExtras().getString(NOTIFICATION_TYPE_1_DATA) != null ? intent.getExtras().getString(NOTIFICATION_TYPE_1_DATA) : "";
          try {
            finalIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + appPackageName));
          } catch (android.content.ActivityNotFoundException e) {
            finalIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + appPackageName));
          }
          finalIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
          applicationContext.startActivity(finalIntent);
          break;
        case NOTIFICATION_TYPE_2:
          link = intent.getStringExtra(NOTIFICATION_LINK);
          if (!link.startsWith("http://") && !link.startsWith("https://"))
            link = "http://" + link;
          intent = new Intent(Intent.ACTION_VIEW, Uri.parse(link));
          intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
          applicationContext.startActivity(intent);
          break;
        case NOTIFICATION_OPEN_APP:
          String label2 = "Subject:" + subject + ", Notice:" + notice + ", Link: " + link;
          notification.put("notificationSubject", subject);
          notification.put("notificationNotice", notice);
          notification.put("notificationId", notificationId);
          notification.put("notificationDate", date);
          notification.put("notificationType", type);
          notification.put("notificationLink", link);
          notification.put("notificationAction", NOTIFICATION_OPEN_APP);
          notification.put("notificationMessageType", MESSAGE_FCM);
          notification.put("notificationImage", intent.getStringExtra(NOTIFICATION_IMAGE) != null ? intent.getStringExtra(NOTIFICATION_IMAGE) : "");
          analytics.put("analytic_label", label2);
          analytics.put("analytic_action", ACTION_OPEN_NOTIFICATION);
          analytics.put("analytic_value", 1);

          data.put("notification", notification);
          data.put("analytics", analytics);
          channel.invokeMethod(method, data);
          return true;
        case NOTIFICATION_TYPE4:
        case NOTIFICATION_TYPE5:
          notification.put("notificationAction", NOTIFICATION_TYPE4);
          notification.put("notificationId", notificationId);
          notification.put("notificationDate", date);
          notification.put("notificationMessageType", MESSAGE_FCM);
          analytics.put("analytic_label", intent.getStringExtra(NOTIFICATION_SURVEYDATA));
          analytics.put("analytic_action", ACTION_OPEN_NOTIFICATION);
          analytics.put("analytic_value", 1);

          data.put("notification", notification);
          data.put("analytics", analytics);
          data.put("surveyData", intent.getStringExtra(NOTIFICATION_SURVEYDATA));
          channel.invokeMethod(method, data);
          return true;

        case NOTIFICATION_OPEN_NOTIFICATIONPAGE:
          notification.put("notificationAction", NOTIFICATION_OPEN_NOTIFICATIONPAGE);
          data.put("notification", notification);
          channel.invokeMethod(method, data);
        case NOTIFICATION_TYPE9:
          notification.put("notificationSubject", subject);
          notification.put("notificationNotice", notice);
          notification.put("notificationId", notificationId);
          notification.put("notificationDate", date);
          notification.put("notificationType", type);
          notification.put("notificationLink", link);
          notification.put("notificationAction", NOTIFICATION_TYPE9);
          notification.put("notificationMessageType", MESSAGE_FCM);
          notification.put("notificationImage", intent.getStringExtra(NOTIFICATION_IMAGE) != null ? intent.getStringExtra(NOTIFICATION_IMAGE) : "");
          analytics.put("analytic_label", "password_reset_notification");
          analytics.put("analytic_action", ACTION_OPEN_NOTIFICATION);
          analytics.put("analytic_value", 1);
          data.put("notification", notification);
          data.put("analytics", analytics);
          data.put("passwordData", intent.getStringExtra(NOTIFICATION_PASSWORD_DATA));
          channel.invokeMethod(method, data);
        default:
          return false;
      }
    }
    return false;
  }

}

