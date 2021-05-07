// Copyright 2020 The Chromium Authors. All rights reserved.
// Use of this source code is governed by a BSD-style license that can be
// found in the LICENSE file.

package io.flutter.plugins.firebase.messaging;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.media.RingtoneManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.transition.Transition;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.JobIntentService;
import androidx.core.app.NotificationCompat;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.CustomTarget;
import com.google.firebase.messaging.RemoteMessage;

import io.flutter.embedding.engine.FlutterShellArgs;

import java.util.Calendar;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;

public class FlutterFirebaseMessagingBackgroundService extends JobIntentService {
  private static final String TAG = "FLTFireMsgService";

  private static final List<Intent> messagingQueue =
    Collections.synchronizedList(new LinkedList<>());

  /**
   * Background Dart execution context.
   */
  private static FlutterFirebaseMessagingBackgroundExecutor flutterBackgroundExecutor;

  /**
   * Schedule the message to be handled by the {@link FlutterFirebaseMessagingBackgroundService}.
   */
  public static void enqueueMessageProcessing(Context context, Intent messageIntent) {
    Log.e(TAG, messageIntent.toString());
    if (messageIntent.hasExtra(FlutterFirebaseMessagingUtils.EXTRA_REMOTE_MESSAGE)) {
      handleNotificationOnBackgroundOnly(messageIntent, context);
    } else {
      enqueueWork(
        context,
        FlutterFirebaseMessagingBackgroundService.class,
        FlutterFirebaseMessagingUtils.JOB_ID,
        messageIntent);
    }

  }

  private static void handleNotificationOnBackgroundOnly(Intent messageIntent, Context context) {
    int type;
    String link = "";
    Long date;
    String notice = "";
    Bundle bundle = new Bundle();
    String messageId = "";
    String subject = "";
    RemoteMessage message = messageIntent.getParcelableExtra(FlutterFirebaseMessagingUtils.EXTRA_REMOTE_MESSAGE);
    Log.e(TAG, "MESSAGEID" + message.getData());
    for (Map.Entry<String, String> entry : message.getData().entrySet()) {
      bundle.putString(entry.getKey(), entry.getValue());
    }
    type = Integer.parseInt(bundle.getString("type"));

    if (type == 1 || type == 2 || type == 7) {
      Intent intent;
      link = bundle.getString("link");
      date = Calendar.getInstance().getTimeInMillis();
      notice = bundle.getString("Notice");
      String image = bundle.getString("image");
      messageId = bundle.getString("Message_Id");
      subject = bundle.getString("subject");
      if (image == null) {
        image = "";
      }

      NotificationManager mNotificationManager = null;
      mNotificationManager = (NotificationManager)
        context.getSystemService(Context.NOTIFICATION_SERVICE);
      intent = new Intent(context, FirebaseCustomNotificationHandler.class);
      intent.setAction(getAction(type));
      geExtra(context, intent, type, link);
      intent.putExtra(FlutterFirebaseMessagingMyWorldLinkConstants.NOTIFICATION_LINK, link);
      intent.putExtra(FlutterFirebaseMessagingMyWorldLinkConstants.NOTIFICATION_DATE, date);
      intent.putExtra(FlutterFirebaseMessagingMyWorldLinkConstants.NOTIFICATION_NOTICE, notice);
      intent.putExtra(FlutterFirebaseMessagingMyWorldLinkConstants.NOTIFICATION_SUBJECT, subject);
      intent.putExtra(FlutterFirebaseMessagingMyWorldLinkConstants.NOTIFICATION_TYPE, type);
      intent.putExtra(FlutterFirebaseMessagingMyWorldLinkConstants.NOTIFICATION_ID, messageId);
      intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
      PendingIntent contentIntent = PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_CANCEL_CURRENT);

      int importance = 0;
      if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
        importance = NotificationManager.IMPORTANCE_HIGH;
      }
      if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
        NotificationChannel notificationChannel =
          new NotificationChannel("1",
            "1", importance);
        mNotificationManager.createNotificationChannel(notificationChannel);
      }
      NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(context);
      ;
      if (image.isEmpty()) {
        mBuilder.setDefaults(Notification.DEFAULT_SOUND);

      } else {
        getBitmapAsyncAndDoWork(image, context, mBuilder, mNotificationManager);

      }
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
        mBuilder.setSmallIcon(R.drawable.notification);
        mBuilder.setColorized(true);

      } else {
        mBuilder.setSmallIcon(R.drawable.notification);
      }
      mBuilder.setContentTitle(subject);
      mBuilder.setContentText(notice);
      if (image.isEmpty()) {
        mBuilder.setStyle(new NotificationCompat.BigTextStyle().bigText(notice));
      }
      mBuilder.setAutoCancel(true);
      mBuilder.setContentIntent(contentIntent);
      mBuilder.setChannelId("1");

      if (mNotificationManager != null) {
        mNotificationManager.notify(Integer.parseInt("1"), mBuilder.build());
      }
    }
  }

  private static void getBitmapAsyncAndDoWork(String imageUrl, Context context, NotificationCompat.Builder builder, NotificationManager mNotificationManager) {

    final Bitmap[] bitmap = {null};

    Glide.with(context)
      .asBitmap()
      .load(imageUrl)
      .into(new CustomTarget<Bitmap>() {

        @Override
        public void onResourceReady(@NonNull Bitmap resource, @Nullable com.bumptech.glide.request.transition.Transition<? super Bitmap> transition) {
          bitmap[0] = resource;
          builder.setStyle(new NotificationCompat.BigPictureStyle().bigPicture(bitmap[0]));
          builder.setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION));
          mNotificationManager.notify(1, builder.build());

        }

        @Override
        public void onLoadCleared(Drawable placeholder) {
        }
      });
  }

  private static void geExtra(Context context, Intent intent, int type, String link) {
    if (type == 1) {
      if (!link.isEmpty())
        intent.putExtra(FlutterFirebaseMessagingMyWorldLinkConstants.NOTIFICATION_TYPE_1_DATA, link);
      else
        intent.putExtra(FlutterFirebaseMessagingMyWorldLinkConstants.NOTIFICATION_TYPE_1_DATA, context.getPackageName());
    }

  }


  private static String getAction(int type) {
    if (type == 1) {
      return FlutterFirebaseMessagingMyWorldLinkConstants.NOTIFICATION_TYPE_1;
    } else if (type == 2) {
      return FlutterFirebaseMessagingMyWorldLinkConstants.NOTIFICATION_TYPE_2;
    } else if (type == 7) {
      return FlutterFirebaseMessagingMyWorldLinkConstants.NOTIFICATION_DELETE;
    }
    return "0";
  }


  /**
   * Starts the background isolate for the {@link FlutterFirebaseMessagingBackgroundService}.
   *
   * <p>Preconditions:
   *
   * <ul>
   *   <li>The given {@code callbackHandle} must correspond to a registered Dart callback. If the
   *       handle does not resolve to a Dart callback then this method does nothing.
   *   <li>A static {@link #pluginRegistrantCallback} must exist, otherwise a {@link
   *       PluginRegistrantException} will be thrown.
   * </ul>
   */
  @SuppressWarnings("JavadocReference")
  public static void startBackgroundIsolate(long callbackHandle, FlutterShellArgs shellArgs) {
    if (flutterBackgroundExecutor != null) {
      Log.w(TAG, "Attempted to start a duplicate background isolate. Returning...");
      return;
    }
    flutterBackgroundExecutor = new FlutterFirebaseMessagingBackgroundExecutor();
    flutterBackgroundExecutor.startBackgroundIsolate(callbackHandle, shellArgs);
  }

  /**
   * Called once the Dart isolate ({@code flutterBackgroundExecutor}) has finished initializing.
   *
   * <p>Invoked by {@link FlutterFirebaseMessagingPlugin} when it receives the {@code
   * FirebaseMessaging.initialized} message. Processes all messaging events that came in while the
   * isolate was starting.
   */
  /* package */
  static void onInitialized() {
    Log.i(TAG, "FlutterFirebaseMessagingBackgroundService started!");
    synchronized (messagingQueue) {
      // Handle all the message events received before the Dart isolate was
      // initialized, then clear the queue.
      for (Intent intent : messagingQueue) {
        flutterBackgroundExecutor.executeDartCallbackInBackgroundIsolate(intent, null);
      }
      messagingQueue.clear();
    }
  }

  /**
   * Sets the Dart callback handle for the Dart method that is responsible for initializing the
   * background Dart isolate, preparing it to receive Dart callback tasks requests.
   */
  public static void setCallbackDispatcher(long callbackHandle) {
    FlutterFirebaseMessagingBackgroundExecutor.setCallbackDispatcher(callbackHandle);
  }

  /**
   * Sets the Dart callback handle for the users Dart handler that is responsible for handling
   * messaging events in the background.
   */
  public static void setUserCallbackHandle(long callbackHandle) {
    FlutterFirebaseMessagingBackgroundExecutor.setUserCallbackHandle(callbackHandle);
  }

  /**
   * Sets the {@link io.flutter.plugin.common.PluginRegistry.PluginRegistrantCallback} used to
   * register the plugins used by an application with the newly spawned background isolate.
   *
   * <p>This should be invoked in {@link MainApplication.onCreate} with {@link
   * GeneratedPluginRegistrant} in applications using the V1 embedding API in order to use other
   * plugins in the background isolate. For applications using the V2 embedding API, it is not
   * necessary to set a {@link io.flutter.plugin.common.PluginRegistry.PluginRegistrantCallback} as
   * plugins are registered automatically.
   */
  @SuppressWarnings({"deprecation", "JavadocReference"})
  public static void setPluginRegistrant(
    io.flutter.plugin.common.PluginRegistry.PluginRegistrantCallback callback) {
    // Indirectly set in FlutterFirebaseMessagingBackgroundExecutor for backwards compatibility.
    FlutterFirebaseMessagingBackgroundExecutor.setPluginRegistrant(callback);
  }

  @Override
  public void onCreate() {
    super.onCreate();
    if (flutterBackgroundExecutor == null) {
      flutterBackgroundExecutor = new FlutterFirebaseMessagingBackgroundExecutor();
    }
    flutterBackgroundExecutor.startBackgroundIsolate();
  }

  /**
   * Executes a Dart callback, as specified within the incoming {@code intent}.
   *
   * <p>Invoked by our {@link JobIntentService} superclass after a call to {@link
   * JobIntentService#enqueueWork(Context, Class, int, Intent);}.
   *
   * <p>If there are no pre-existing callback execution requests, other than the incoming {@code
   * intent}, then the desired Dart callback is invoked immediately.
   *
   * <p>If there are any pre-existing callback requests that have yet to be executed, the incoming
   * {@code intent} is added to the {@link #messagingQueue} to be invoked later, after all
   * pre-existing callbacks have been executed.
   */
  @Override
  protected void onHandleWork(@NonNull final Intent intent) {
    if (!flutterBackgroundExecutor.isDartBackgroundHandlerRegistered()) {
      Log.w(
        TAG,
        "A background message could not be handled in Dart as no onBackgroundMessage handler has been registered.");
      return;
    }

    // If we're in the middle of processing queued messages, add the incoming
    // intent to the queue and return.
    synchronized (messagingQueue) {
      if (flutterBackgroundExecutor.isNotRunning()) {
        Log.i(TAG, "Service has not yet started, messages will be queued.");
        messagingQueue.add(intent);
        return;
      }
    }

    // There were no pre-existing callback requests. Execute the callback
    // specified by the incoming intent.
    final CountDownLatch latch = new CountDownLatch(1);
    new Handler(getMainLooper())
      .post(
        () -> flutterBackgroundExecutor.executeDartCallbackInBackgroundIsolate(intent, latch));

    try {
      latch.await();
    } catch (InterruptedException ex) {
      Log.i(TAG, "Exception waiting to execute Dart callback", ex);
    }
  }
}
