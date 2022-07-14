package io.flutter.plugins.firebase.messaging;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.media.MediaSyncEvent;
import android.os.Bundle;

import androidx.annotation.Nullable;

import com.google.firebase.messaging.RemoteMessage;

import java.text.SimpleDateFormat;
import java.util.Map;
import java.util.Date;

public class InsertNotificationDBHelper extends SQLiteOpenHelper {
  public InsertNotificationDBHelper(@Nullable Context context) {
    super(context, DATABASE_NAME, null, DATABASE_VERSION);

  }

  private static int DATABASE_VERSION = 3;
  private static String DATABASE_NAME = "wlink.db";
  String TABLE_NAME = "NotificationDb";

  // Column Names
  private static String ID = "id";
  private static String ACCOUNT_STATUS_DISABLE = "account_status_disable";
  private static String DATE = "date";
  private static String EXECUTION_ID = "execution_id";
  private static String FCM_REPONSE_ID = "fcm_response_id";
  private static String GRACE_STATUS = "grace_status";
  private static String IMAGE_DISPLAY = "image";
  private static String LINK = "link";
  private static String MESSAGE = "message";
  private static String MSG_LABEL = "msg_label";
  private static String OPERATOR = "operator";
  private static String PLAN_CATEGORY_ID = "plan_category_id";
  private static String RESPONSE = "response";
  private static String SUBJECT = "subject";
  private static String SUPPORT_ZONE_ID = "support_zone_id";
  private static String TYPE = "type";
  private static String USER_ACTION = "user_action";
  private static String USER_ACTION_DATE = "user_action_date";
  private static String USERNAME = "username";

  @Override
  public void onCreate(SQLiteDatabase sqLiteDatabase) {
  }

  @Override
  public void onUpgrade(SQLiteDatabase sqLiteDatabase, int oldVersion, int newVersion) {
  }

  void addNotification(RemoteMessage remoteMessage, String username) {

    ContentValues contentValues = new ContentValues();
    contentValues.put(ID, remoteMessage.getData().get("single_message_id"));
    contentValues.put(SUBJECT, remoteMessage.getData().get("subject"));
    contentValues.put(MESSAGE, remoteMessage.getData().get("Notice"));
    contentValues.put(TYPE, remoteMessage.getData().get("type"));
    contentValues.put(LINK, remoteMessage.getData().get("link"));
    contentValues.put(DATE,getFormattedDate(new Date()));
    contentValues.put(IMAGE_DISPLAY, remoteMessage.getData().get("image"));
    contentValues.put(EXECUTION_ID, remoteMessage.getData().get("execution_id"));
    contentValues.put(MSG_LABEL, remoteMessage.getData().get("msg_label"));
    contentValues.put(FCM_REPONSE_ID, remoteMessage.getData().get("fcm_response_id"));
    contentValues.put(USERNAME, username);

    contentValues.put(ACCOUNT_STATUS_DISABLE,"");
    contentValues.put(GRACE_STATUS, "");
    contentValues.put(OPERATOR, "");
    contentValues.put(PLAN_CATEGORY_ID, "");
    contentValues.put(RESPONSE, "");
    contentValues.put(SUPPORT_ZONE_ID, "");
    contentValues.put(USER_ACTION, "");
    contentValues.put(USER_ACTION_DATE, "");

    SQLiteDatabase db = this.getWritableDatabase();
    db.insert(TABLE_NAME, null, contentValues);
    db.close();
  }
  public static String getFormattedDate(Date date) {
    SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    return formatter.format(date);
  }

}
