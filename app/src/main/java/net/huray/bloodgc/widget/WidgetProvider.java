package net.huray.bloodgc.widget;


import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.widget.RemoteViews;

import net.huray.bloodgc.NFCApp;
import net.huray.bloodgc.R;
import net.huray.bloodgc.model.BloodGlucose;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;



public class WidgetProvider extends AppWidgetProvider {

    @Override
    public void onReceive(Context context, Intent intent) {
        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
        ComponentName testWidget = new ComponentName(context.getPackageName(), WidgetProvider.class.getName());


        int[] widgetIds = appWidgetManager.getAppWidgetIds(testWidget);
        if(intent.getAction().equals(AppWidgetManager.ACTION_APPWIDGET_UPDATE)){
            if(widgetIds != null && widgetIds.length > 0) {
                this.onUpdate(context, AppWidgetManager.getInstance(context), widgetIds);
            }
        }
    }


    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager,
                         int[] appWidgetIds) {
        super.onUpdate(context, appWidgetManager, appWidgetIds);


        appWidgetIds = appWidgetManager.getAppWidgetIds(new ComponentName(
                context, getClass()));

        for (int i = 0; i < appWidgetIds.length; i++) {
            updateAppWidget(context, appWidgetManager, appWidgetIds[i]);
        }
    }


    @Override
    public void onEnabled(Context context) {
        super.onEnabled(context);
    }


    @Override
    public void onDisabled(Context context) {
        super.onDisabled(context);
    }


    @Override
    public void onDeleted(Context context, int[] appWidgetIds) {
        super.onDeleted(context, appWidgetIds);
    }


//	@SuppressLint({ "NewApi" })
//	@Override
//	public void onAppWidgetOptionsChanged(Context context,
//			AppWidgetManager appWidgetManager, int appWidgetId,
//			Bundle newOptions) {
//		super.onAppWidgetOptionsChanged(context, appWidgetManager, appWidgetId,
//				newOptions);
//
//		Calendar mCalendar = Calendar.getInstance();
//		SimpleDateFormat mFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm",
//				Locale.KOREA);
//
//		int minWidth = newOptions
//				.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_WIDTH);
//		int maxWidth = newOptions
//				.getInt(AppWidgetManager.OPTION_APPWIDGET_MAX_WIDTH);
//		int minHeight = newOptions
//				.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_HEIGHT);
//		int maxHeight = newOptions
//				.getInt(AppWidgetManager.OPTION_APPWIDGET_MAX_HEIGHT);
//
//		RemoteViews updateViews = null;
//
//		Log.d("minWidth", "" + minWidth);
//		Log.d("maxWidth", "" + maxWidth);
//		Log.d("minHeight", "" + minHeight);
//		Log.d("maxHeight", "" + maxHeight);
//		Log.d(" ", " ");
//
//			updateViews = new RemoteViews(context.getPackageName(),
//					R.layout.widget_layout);
//
//
//		updateViews.setTextViewText(R.id.tv_blood_glucose,
//				mFormat.format(mCalendar.getTime()));
//
//
//		Intent intent = new Intent(Intent.ACTION_VIEW,
//				Uri.parse("http://itmir.tistory.com/"));
//		PendingIntent pendingIntent = PendingIntent.getActivity(context, 0,
//				intent, 0);
//		updateViews.setOnClickPendingIntent(R.id.mLayout, pendingIntent);
//
//		appWidgetManager.updateAppWidget(appWidgetId, updateViews);
//	}

    public static void updateAppWidget(Context context,
                                       AppWidgetManager appWidgetManager, int appWidgetId) {
        RemoteViews updateViews = new RemoteViews(context.getPackageName(),
                R.layout.widget_layout);

        SimpleDateFormat beforeFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.KOREA);
        SimpleDateFormat mFormat = new SimpleDateFormat("yyyy년 MM월 dd일  HH시 mm분", Locale.KOREA);
        BloodGlucose glucose = getNewBg();
        try {
            if (glucose == null) {
                updateViews.setTextViewText(R.id.tv_mesure_date,
                        "측정시간: " + context.getString(R.string.not_have_record));
            } else {
                Date mesureTime = beforeFormat.parse(glucose.getRecordDttm());
                updateViews.setTextViewText(R.id.tv_mesure_date,
                        mFormat.format(mesureTime));

                updateViews.setTextViewText(R.id.tv_mesure_value,
                        "혈당치: " + glucose.getMeasure());

                updateViews.setTextViewText(R.id.tv_meal_time,
                        "측정시기: " + (glucose.isAteFood() ? "식후" : "식전"));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }


        Intent intent = new Intent(Intent.ACTION_VIEW,
                Uri.parse("selphone://record"));
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0,
                intent, 0);
        updateViews.setOnClickPendingIntent(R.id.mLayout, pendingIntent);

        appWidgetManager.updateAppWidget(appWidgetId, updateViews);
    }

    private static BloodGlucose getNewBg() {
        BloodGlucose glucose = null;

        if (NFCApp.getDataStore().select(BloodGlucose.class).get().firstOrNull() != null) {
            glucose = NFCApp.getDataStore().select(BloodGlucose.class).orderBy(BloodGlucose.RECORD_DTTM.desc()).get().first();
        }

        return glucose;
    }
}