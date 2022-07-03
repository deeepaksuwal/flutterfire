package io.flutter.plugins.firebase.messaging;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Bundle;

import androidx.annotation.Nullable;

import com.google.firebase.messaging.RemoteMessage;

import java.util.Map;
import java.text.SimpleDateFormat;
import java.util.Date;

public class InsertNotificationDBHelper extends SQLiteOpenHelper {
  public InsertNotificationDBHelper(@Nullable Context context) {
    super(context, DATABASE_NAME, null, DATABASE_VERSION);

  }

  private static int DATABASE_VERSION = 2;
  private static String DATABASE_NAME = "wlink.db";
  String TABLE_NAME = "NotificationDb";

  // Column Names
  private static String ID = "id";
  private static String SUBJECT = "subject";
  private static String MESSAGE = "message";
  private static String LINK = "link";
  private static String DATE = "date";
  private static String PRIORITY = "priority";
  private static String TYPE = "type";
  private static String MESSAGE_TYPE = "messageType";
  private static String CONTENT_TYPE = "contentType";
  private static String STATUS = "status";
  private static String USERNAME = "username";
  private static String IMAGE_DISPLAY = "image_display";
  private static String SINGLE_MESSAGE_ID = "single_message_id";
  private static String EXECUTION_ID = "execution_id";
  private static String MSG_LABEL = "msg_label";
  private static String URL = "url";
  private static String POSTTYPE = "posttype";
  private static String READ_STATUS = "read_status";
  private static String OPERATOR = "operator";
  private static String UNIQUE_IDENTIFIER = "unique_identifier";


  @Override
  public void onCreate(SQLiteDatabase sqLiteDatabase) {
  }

  @Override
  public void onUpgrade(SQLiteDatabase sqLiteDatabase, int oldVersion, int newVersion) {
  }

  void addNotification(RemoteMessage remoteMessage, String username) {

    ContentValues contentValues = new ContentValues();
    contentValues.put(ID, remoteMessage.getMessageId());
    contentValues.put(SUBJECT, remoteMessage.getData().get("subject"));
    contentValues.put(MESSAGE, remoteMessage.getData().get("Notice"));
    contentValues.put(LINK, remoteMessage.getData().get("link"));
    contentValues.put(DATE, getFormattedDate(new Date()));
    contentValues.put(PRIORITY, remoteMessage.getPriority());
    contentValues.put(TYPE, remoteMessage.getData().get("type"));
    contentValues.put(MESSAGE_TYPE, "fcm");
    contentValues.put(CONTENT_TYPE, "text/plain");
    contentValues.put(IMAGE_DISPLAY, remoteMessage.getData().get("image_display"));
    contentValues.put(SINGLE_MESSAGE_ID, remoteMessage.getData().get("single_message_id"));
    contentValues.put(EXECUTION_ID, remoteMessage.getData().get("execution_id"));
    contentValues.put(MSG_LABEL, remoteMessage.getData().get("msg_label"));
    contentValues.put(URL, remoteMessage.getData().get("url"));
    contentValues.put(POSTTYPE, remoteMessage.getData().get("posttype"));
    contentValues.put(READ_STATUS, "read");
    contentValues.put(OPERATOR, remoteMessage.getData().get("operator"));
    contentValues.put(UNIQUE_IDENTIFIER, remoteMessage.getData().get("unique_identifier"));
    contentValues.put(STATUS, "0");
    contentValues.put(USERNAME, username);
    SQLiteDatabase db = this.getWritableDatabase();
    db.insert(TABLE_NAME, null, contentValues);
    db.close();
  }

  public static String getFormattedDate(Date date) {
    SimpleDateFormat formatter = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");

    return formatter.format(date);
  }

}
