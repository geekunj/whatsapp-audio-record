package com.prototype.whatsaudiorecord.data;

import android.content.Context;
import android.util.Log;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import com.prototype.whatsaudiorecord.data.local.dao.RecordDao;
import com.prototype.whatsaudiorecord.models.Recording;

import kotlin.jvm.Volatile;

@Database(entities = {Recording.class}, version = 1, exportSchema = false)
public abstract class AppDatabase extends RoomDatabase {

  private static final String LOG_TAG = AppDatabase.class.getSimpleName();
  private static final Object LOCK = new Object();
  private static final String DATABASE_NAME = "recordingsdb";
  private static AppDatabase sInstance;

  public static AppDatabase getInstance(Context context) {
    if (sInstance == null){
      synchronized (LOCK){
        Log.d(LOG_TAG, "Creating new database instance...");
        sInstance = Room.databaseBuilder(context.getApplicationContext(),
                AppDatabase.class, AppDatabase.DATABASE_NAME)
                .allowMainThreadQueries()
                .build();
      }
    }
    Log.d(LOG_TAG, "Getting the database instance...");
    return sInstance;
  }


  public abstract RecordDao RecordDao();
}
