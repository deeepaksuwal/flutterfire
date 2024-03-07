// Copyright 2020 The Chromium Authors. All rights reserved.
// Use of this source code is governed by a BSD-style license that can be
// found in the LICENSE file.

package io.flutter.plugins.firebase.messaging;

import com.freshchat.consumer.sdk.Freshchat;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.google.firebase.messaging.RemoteMessage;

import java.util.HashMap;

import android.content.SharedPreferences;
import com.freshchat.consumer.sdk.Freshchat;

public class FlutterFirebaseMessagingReceiver extends BroadcastReceiver {
  private static final String TAG = "FLTFireMsgReceiver";
  static HashMap<String, RemoteMessage> notifications = new HashMap<>();

  @Override
  public void onReceive(Context context, Intent intent) {
    Log.d(TAG, "broadcast received for message");
    if (ContextHolder.getApplicationContext() == null) {
      ContextHolder.setApplicationContext(context.getApplicationContext());
    }

    RemoteMessage remoteMessage = new RemoteMessage(intent.getExtras());
    if (Freshchat.isFreshchatNotification(remoteMessage)) {
      Log.d(TAG, "broadcast received for message is freshchat notif background");
      Freshchat.handleFcmMessage(context, remoteMessage);

    }else {
      Log.d(TAG, "broadcast received for message is normal notif background");

      // Store the RemoteMessage if the message contains a notification payload.
      if (remoteMessage.getNotification() != null) {
        notifications.put(remoteMessage.getMessageId(), remoteMessage);
        FlutterFirebaseMessagingStore.getInstance().storeFirebaseMessage(remoteMessage);
      }

      SharedPreferences prefs = context.getSharedPreferences("FlutterSharedPreferences", Context.MODE_PRIVATE);
      String username = prefs.getString("flutter." + "PREFS_USER_DEFAULT", "");
      prefs.edit().putBoolean("flutter.PREFS_USER_READ_STATUS_NOTIFICATION", true).apply();
      InsertNotificationDBHelper dbHelper = new InsertNotificationDBHelper(context);
      dbHelper.addNotification(remoteMessage, username);

      //  |-> ---------------------
      //      App in Foreground
      //   ------------------------
      if (FlutterFirebaseMessagingUtils.isApplicationForeground(context)) {
        Intent onMessageIntent = new Intent(FlutterFirebaseMessagingUtils.ACTION_REMOTE_MESSAGE);
        onMessageIntent.putExtra(FlutterFirebaseMessagingUtils.EXTRA_REMOTE_MESSAGE, remoteMessage);
        LocalBroadcastManager.getInstance(context).sendBroadcast(onMessageIntent);
        return;
      }

      //  |-> ---------------------
      //    App in Background/Quit
      //   ------------------------
      Intent onBackgroundMessageIntent =
        new Intent(context, FlutterFirebaseMessagingBackgroundService.class);
      onBackgroundMessageIntent.putExtra(
        FlutterFirebaseMessagingUtils.EXTRA_REMOTE_MESSAGE, remoteMessage);
      FlutterFirebaseMessagingBackgroundService.enqueueMessageProcessing(
        context, onBackgroundMessageIntent);
    }
  }
}
