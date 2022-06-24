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
  private String executionId = "";
  private String msgLabel = "";
  private String url = "";
  private String postType = "";
  private String operator = "";
  private String uniqueIdentifier = "";
  private Intent finalIntent;
  private long date;
  private Date mDate;
  private int type = 0;
  private String notificationId = "";
  private static final String TAG = FirebaseCustomNotificationHandler.class.getSimpleName();
  String token = "";
  String username = "";

  @Override
  public void onReceive(Context context, Intent intent) {
    this.context = context;
    finalIntent = intent;

    if (intent.getExtras() != null) {
      notificationId = _getNotificationID(intent);
      notice = _getNotice(intent);
      link = _getLink(intent);
      subject = _getSubject(intent);
      mDate = _getDate(intent);
      image = _getImage(intent);
      singleMessageId = _getSingleMessageId(intent);
      executionId = _getExecutionId(intent);
      msgLabel = _getMsgLabel(intent);
      url = _getUrl(intent);
      postType = _getPostType(intent);
      operator = _getOperator(intent);
      uniqueIdentifier = _getUniqueIdentifier(intent);
    }
    SharedPreferences preferences = context.getSharedPreferences("FlutterSharedPreferences", Context.MODE_PRIVATE);
    token = preferences.getString("flutter.PREFS_USER_TOKEN", "");
    username = preferences.getString("flutter.PREFS_USER_DEFAULT", "");
    handleNotificationType(context, intent);

  }

  private void handleNotificationType(Context context, Intent intent) {
    switch (intent.getAction()) {
      case FlutterFirebaseMessagingMyWorldLinkConstants.NOTIFICATION_DELETE:
        FirebaseMessagingMyWorldLinkUtils.sendNotificationReadStatus(context, notificationId, "2", username, token);
        break;

      case FlutterFirebaseMessagingMyWorldLinkConstants.NOTIFICATION_TYPE_1:
        String appPackageName = intent.getExtras().getString(FlutterFirebaseMessagingMyWorldLinkConstants.NOTIFICATION_TYPE_1_DATA);
        try {
          finalIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + appPackageName));
        } catch (android.content.ActivityNotFoundException anfe) {
          finalIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + appPackageName));
        }
        finalIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(finalIntent);
        FirebaseMessagingMyWorldLinkUtils.sendNotificationReadStatus(context, notificationId, "1", username, token);
        break;

      case FlutterFirebaseMessagingMyWorldLinkConstants.NOTIFICATION_TYPE_2:
        link = intent.getExtras().getString(FlutterFirebaseMessagingMyWorldLinkConstants.NOTIFICATION_LINK);
        if (!link.startsWith("http://") && !link.startsWith("https://"))
          link = "http://" + link;
        intent = new Intent(Intent.ACTION_VIEW, Uri.parse(link));
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        FirebaseMessagingMyWorldLinkUtils.sendNotificationReadStatus(context, notificationId, "1", username, token);
        context.startActivity(intent);
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

  private String _getNotificationID(Intent intent) {
    return intent.getExtras().getString(FlutterFirebaseMessagingMyWorldLinkConstants.NOTIFICATION_ID, "");
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

  private String _getExecutionId(Intent intent) {
    if (intent.getExtras().getString(FlutterFirebaseMessagingMyWorldLinkConstants.NOTIFICATION_EXECUTION_ID) != null) {
      return intent.getExtras().getString(FlutterFirebaseMessagingMyWorldLinkConstants.NOTIFICATION_EXECUTION_ID, "");
    }
    return "";
  }

  private String _getMsgLabel(Intent intent) {
    if (intent.getExtras().getString(FlutterFirebaseMessagingMyWorldLinkConstants.NOTIFICATION_MSG_LABEL) != null) {
      return intent.getExtras().getString(FlutterFirebaseMessagingMyWorldLinkConstants.NOTIFICATION_MSG_LABEL, "");
    }
    return "";
  }

  private String _getUrl(Intent intent) {
    if (intent.getExtras().getString(FlutterFirebaseMessagingMyWorldLinkConstants.NOTIFICATION_URL) != null) {
      return intent.getExtras().getString(FlutterFirebaseMessagingMyWorldLinkConstants.NOTIFICATION_URL, "");
    }
    return "";
  }

  private String _getPostType(Intent intent) {
    if (intent.getExtras().getString(FlutterFirebaseMessagingMyWorldLinkConstants.NOTIFICATION_POSTTYPE) != null) {
      return intent.getExtras().getString(FlutterFirebaseMessagingMyWorldLinkConstants.NOTIFICATION_POSTTYPE, "");
    }
    return "";
  }

  private String _getOperator(Intent intent) {
    if (intent.getExtras().getString(FlutterFirebaseMessagingMyWorldLinkConstants.NOTIFICATION_OPERATOR) != null) {
      return intent.getExtras().getString(FlutterFirebaseMessagingMyWorldLinkConstants.NOTIFICATION_OPERATOR, "");
    }
    return "";
  }

  private String _getUniqueIdentifier(Intent intent) {
    if (intent.getExtras().getString(FlutterFirebaseMessagingMyWorldLinkConstants.NOTIFICATION_UNIQUE_IDENTIFIER) != null) {
      return intent.getExtras().getString(FlutterFirebaseMessagingMyWorldLinkConstants.NOTIFICATION_UNIQUE_IDENTIFIER, "");
    }
    return "";
  }
}
