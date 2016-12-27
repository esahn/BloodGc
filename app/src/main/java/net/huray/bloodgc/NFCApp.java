package net.huray.bloodgc;

import android.app.Activity;
import android.app.Application;

import com.facebook.stetho.Stetho;
import com.kakao.auth.KakaoSDK;

import net.huray.bloodgc.logic.KakaoSDKAdapter;
import net.huray.bloodgc.model.Models;

import io.requery.Persistable;
import io.requery.android.sqlite.DatabaseSource;
import io.requery.sql.Configuration;
import io.requery.sql.EntityDataStore;
import io.requery.sql.TableCreationMode;


public class NFCApp extends Application {
    private static final int SCHEMA_VERSION = 1;
    private static DatabaseSource sDatabaseSource;
    private static volatile NFCApp instance = null;
    private static volatile Activity currentActivity = null;
    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
        Stetho.initializeWithDefaults(this);
        initDatabaseSource();
        KakaoSDK.init(new KakaoSDKAdapter());
    }

    public static Activity getCurrentActivity() {
        return currentActivity;
    }

    public static void setCurrentActivity(Activity currentActivity) {
        NFCApp.currentActivity = currentActivity;
    }

    public static NFCApp getGlobalApplicationContext() {
        if(instance == null)
            throw new IllegalStateException("this application does not inherit com.kakao.GlobalApplication");
        return instance;
    }

    public void initDatabaseSource() {
        sDatabaseSource = new DatabaseSource(this, Models.DEFAULT, SCHEMA_VERSION);
    }

    public static EntityDataStore<Persistable> getDataStore() {
        sDatabaseSource.setTableCreationMode(TableCreationMode.DROP_CREATE);
        Configuration configuration = sDatabaseSource.getConfiguration();
        return new EntityDataStore<>(configuration);
    }

    @Override
    public void onTerminate() {
        super.onTerminate();
    }


//    class MyDatabaseSource extends  DatabaseSource{
//
//        MyDatabaseSource(Context context, EntityModel model, int version) {
//            super(context, model, version);
//        }
//
//        public MyDatabaseSource(Context context, EntityModel model, @Nullable String name, int version) {
//            super(context, model, name, version);
//        }
//
//        public MyDatabaseSource(Context context, EntityModel model, @Nullable String name, @Nullable SQLiteDatabase.CursorFactory factory, int version) {
//            super(context, model, name, factory, version);
//        }
//
//        public MyDatabaseSource(Context context, EntityModel model, @Nullable String name, @Nullable SQLiteDatabase.CursorFactory factory, int version, SQLite platform) {
//            super(context, model, name, factory, version, platform);
//        }
//
//        @Override
//        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
//
//            if(newVersion == SCHEMA_VERSION){
//                db.execSQL("DROP TABLE FoodEvaluation");
//            }
//            super.onUpgrade(db, oldVersion, newVersion);
//
//        }
//
//    }

}