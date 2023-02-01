package io.flutter.plugins.firebase.messaging;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.util.Log;

import java.util.Date;

public class FirebaseCustomNotificationHandler extends BroadcastReceiver {
  private Context context;
  private String extras;
  private String link = "";
  private String notice = "";
  private String subject = "";
  private String image = "";
  private String singleMessageId = "";
  private int executionId;
  private String msgLabel = "";
  private String fcmResponseId = "";
  private String diagnosticIdx = "";
  private String macAddress = "";
  private String latitude = "";
  private String longitude = "";
  private Intent finalIntent;
  private static final String TAG = "FirebaseCustomNotificationHandler";
  String token = "";
  private long date;
  private Date mDate;
  private int type = 0;
  String username = "";

  @Override
  public void onReceive(Context context, Intent intent) {
    this.context = context;
    finalIntent = intent;

    if (intent.getExtras() != null) {
      notice = _getNotice(intent);
      link = _getLink(intent);
      subject = _getSubject(intent);
      mDate = _getDate(intent);
      image = _getImage(intent);
      singleMessageId = _getSingleMessageId(intent);
      executionId = _getExecutionId(intent);
      fcmResponseId = String.valueOf(_getFCMResponseId(intent));
      diagnosticIdx = _getDiagnosticIdx(intent);
      macAddress = _getMacAddress(intent);
      latitude = _getLatitude(intent);
      longitude = _getLongitude(intent);
      type = _getType(intent);
      msgLabel = _getMsgLabel(intent);
    }
    Log.d(TAG, "handleNotificationType onReceive: exec " + executionId);
    Log.d(TAG, "handleNotificationType onReceive: fcmresponse id  " + fcmResponseId);

    Log.d(TAG, "handleNotificationType onReceive: HERE" + intent.getAction());

    SharedPreferences preferences = context.getSharedPreferences("FlutterSharedPreferences", Context.MODE_PRIVATE);
    token = preferences.getString("flutter.PREFS_USER_TOKEN", "");
    username = preferences.getString("flutter.PREFS_USER_DEFAULT", "");
    preferences.edit().putBoolean("flutter.PREFS_USER_READ_STATUS_NOTIFICATION", true).apply();
    handleNotificationType(context, intent);
  }

  private void handleNotificationType(Context context, Intent intent) {
    switch (intent.getAction()) {
      case FlutterFirebaseMessagingMyWorldLinkConstants.NOTIFICATION_DELETE:
        Log.d(TAG, "handleNotificationType: delete");
        FirebaseMessagingMyWorldLinkUtils.sendNotificationReadStatus(context, "dismiss", msgLabel, username, String.valueOf(executionId), token);
        break;

      case FlutterFirebaseMessagingMyWorldLinkConstants.NOTIFICATION_TYPE_1:
        Log.d(TAG, "handleNotificationType: notification 1");
        String appPackageName = intent.getExtras().getString(FlutterFirebaseMessagingMyWorldLinkConstants.NOTIFICATION_TYPE_1_DATA);
        try {
          finalIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + appPackageName));
        } catch (android.content.ActivityNotFoundException anfe) {
          finalIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + appPackageName));
        }
        finalIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(finalIntent);
        FirebaseMessagingMyWorldLinkUtils.sendNotificationReadStatus(context, "seen", msgLabel, username, String.valueOf(executionId), token);
        break;

      case FlutterFirebaseMessagingMyWorldLinkConstants.NOTIFICATION_TYPE_2:
        Log.d(TAG, "handleNotificationType: notification 2");
        link = intent.getExtras().getString(FlutterFirebaseMessagingMyWorldLinkConstants.NOTIFICATION_LINK);
        if (!link.startsWith("http://") && !link.startsWith("https://"))
          link = "http://" + link;
        intent = new Intent(Intent.ACTION_VIEW, Uri.parse(link));
        if (intent.resolveActivity(context.getPackageManager()) != null) {
          intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
          FirebaseMessagingMyWorldLinkUtils.sendNotificationReadStatus(context, "seen", msgLabel, username, String.valueOf(executionId), token);
          context.startActivity(intent);
        }
        break;

      default:
        FirebaseMessagingMyWorldLinkUtils.sendNotificationReadStatus(context, "seen", msgLabel, username, String.valueOf(executionId), token);
        break;

    }
  }

  private int _getType(Intent intent) {
    return intent.getExtras().getInt(FlutterFirebaseMessagingMyWorldLinkConstants.NOTIFICATION_TYPE);
  }

  private Date _getDate(Intent intent) {
    if (intent.getExtras().getLong(FlutterFirebaseMessagingMyWorldLinkConstants.NOTIFICATION_DATE) < 0) {
      date = intent.getExtras().getLong(FlutterFirebaseMessagingMyWorldLinkConstants.NOTIFICATION_LINK);
      return new Date(date);
    }
    return new Date();
  }

  private String _getSubject(Intent intent) {
    if (intent.getExtras().getString(FlutterFirebaseMessagingMyWorldLinkConstants.NOTIFICATION_SUBJECT) != null) {
      return intent.getExtras().getString(FlutterFirebaseMessagingMyWorldLinkConstants.NOTIFICATION_SUBJECT);
    }
    return "";
  }

  private String _getLink(Intent intent) {
    if (intent.getExtras().getString(FlutterFirebaseMessagingMyWorldLinkConstants.NOTIFICATION_LINK) != null) {
      return intent.getExtras().getString(FlutterFirebaseMessagingMyWorldLinkConstants.NOTIFICATION_LINK);
    }
    return "";
  }

  private String _getNotice(Intent intent) {
    if (intent.getExtras().getString(FlutterFirebaseMessagingMyWorldLinkConstants.NOTIFICATION_NOTICE) != null) {
      return intent.getExtras().getString(FlutterFirebaseMessagingMyWorldLinkConstants.NOTIFICATION_NOTICE);
    }
    return "";
  }

  private String _getDiagnosticIdx(Intent intent) {
    if (intent.getExtras().getString(FlutterFirebaseMessagingMyWorldLinkConstants.NOTIFICATION_DIAGNOSTIC_IDX) != null) {
      Log.d(TAG, "_getDiagnosticIdx: " + intent.getExtras().getString(FlutterFirebaseMessagingMyWorldLinkConstants.NOTIFICATION_DIAGNOSTIC_IDX));

      return intent.getExtras().getString(FlutterFirebaseMessagingMyWorldLinkConstants.NOTIFICATION_DIAGNOSTIC_IDX);
    }
    return "";
  }

  private String _getMacAddress(Intent intent) {
    if (intent.getExtras().getString(FlutterFirebaseMessagingMyWorldLinkConstants.NOTIFICATION_MAC_ADDRESS) != null) {
      Log.d(TAG, "_getMacAddress: " + intent.getExtras().getString(FlutterFirebaseMessagingMyWorldLinkConstants.NOTIFICATION_MAC_ADDRESS));

      return intent.getExtras().getString(FlutterFirebaseMessagingMyWorldLinkConstants.NOTIFICATION_MAC_ADDRESS);
    }
    return "";
  }

  private String _getLatitude(Intent intent) {
    if (intent.getExtras().getString(FlutterFirebaseMessagingMyWorldLinkConstants.NOTIFICATION_LATITUDE) != null) {
      Log.d(TAG, "_getLatitude: " + intent.getExtras().getString(FlutterFirebaseMessagingMyWorldLinkConstants.NOTIFICATION_LATITUDE));

      return intent.getExtras().getString(FlutterFirebaseMessagingMyWorldLinkConstants.NOTIFICATION_LATITUDE);
    }
    return "";
  }

  private String _getLongitude(Intent intent) {
    if (intent.getExtras().getString(FlutterFirebaseMessagingMyWorldLinkConstants.NOTIFICATION_LONGITUDE) != null) {
      Log.d(TAG, "_getLongitude: " + intent.getExtras().getString(FlutterFirebaseMessagingMyWorldLinkConstants.NOTIFICATION_LONGITUDE));

      return intent.getExtras().getString(FlutterFirebaseMessagingMyWorldLinkConstants.NOTIFICATION_LONGITUDE);
    }
    return "";
  }

  private String _getImage(Intent intent) {
    if (intent.getExtras().getString(FlutterFirebaseMessagingMyWorldLinkConstants.NOTIFICATION_IMAGE) != null) {
      return intent.getExtras().getString(FlutterFirebaseMessagingMyWorldLinkConstants.NOTIFICATION_IMAGE, "");
    }
    return "";
  }

  private String _getSingleMessageId(Intent intent) {
    if (intent.getExtras().getString(FlutterFirebaseMessagingMyWorldLinkConstants.NOTIFICATION_SINGLE_MESSAGE_ID) != null) {
      return intent.getExtras().getString(FlutterFirebaseMessagingMyWorldLinkConstants.NOTIFICATION_SINGLE_MESSAGE_ID, "");
    }
    return "";
  }

  private String _getFCMResponseId(Intent intent) {
    if (intent.getExtras().getString(FlutterFirebaseMessagingMyWorldLinkConstants.NOTIFICATION_FCM_RESPONSE_ID) != null) {
      Log.d(TAG, "_getFCMResponseId: " + intent.getExtras().getString(FlutterFirebaseMessagingMyWorldLinkConstants.NOTIFICATION_FCM_RESPONSE_ID));

      return intent.getExtras().getString(FlutterFirebaseMessagingMyWorldLinkConstants.NOTIFICATION_FCM_RESPONSE_ID, "");
    }
    return "";
  }

  private int _getExecutionId(Intent intent) {
    Log.d(TAG, "_getExecutionId: " + intent.getExtras().getInt(FlutterFirebaseMessagingMyWorldLinkConstants.NOTIFICATION_EXECUTION_ID));
    return intent.getExtras().getInt(FlutterFirebaseMessagingMyWorldLinkConstants.NOTIFICATION_EXECUTION_ID);
  }

  private String _getMsgLabel(Intent intent) {
    if (intent.getExtras().getString(FlutterFirebaseMessagingMyWorldLinkConstants.NOTIFICATION_MSG_LABEL) != null) {
      return intent.getExtras().getString(FlutterFirebaseMessagingMyWorldLinkConstants.NOTIFICATION_MSG_LABEL, "");
    }
    return "";
  }
}
