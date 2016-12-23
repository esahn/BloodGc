package net.huray.bloodgc.util;

import android.support.annotation.NonNull;
import android.text.TextUtils;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;



public class ApiUtils {

    private static ThreadLocal<SimpleDateFormat> dfDate = new ThreadLocal<SimpleDateFormat>() {
        @Override
        protected SimpleDateFormat initialValue() {
            return new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        }
    };

    private static ThreadLocal<SimpleDateFormat> dfSqliteDate = new ThreadLocal<SimpleDateFormat>() {
        @Override
        protected SimpleDateFormat initialValue() {
            return new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        }
    };

    private static ThreadLocal<SimpleDateFormat> dfDateTime = new ThreadLocal<SimpleDateFormat>() {
        @Override
        protected SimpleDateFormat initialValue() {
            return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        }
    };

    public static String formatDate(long time) {
        return dfDate.get().format(new Date(time));
    }

    public static String formatSqliteDate(long time) {
        return dfSqliteDate.get().format(new Date(time));
    }

    public static String formatDateTime(long time) {
        return dfDateTime.get().format(new Date(time));
    }

    public static long parseDate(@NonNull String date) throws ParseException {
        return dfDate.get().parse(date).getTime();
    }

    public static long parseSqliteDate(@NonNull String day) throws ParseException {
        return dfSqliteDate.get().parse(day).getTime();
    }

    public static long parseDateTime(@NonNull String dateTime) throws ParseException {
        return dfDateTime.get().parse(dateTime).getTime();
    }

    public static long[] getDatesForLastNDays(int nDays) {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        cal.add(Calendar.DATE, 1);

        long todayEnd = cal.getTimeInMillis() - 1;

        cal.add(Calendar.DATE, -1 * nDays);
        long start = cal.getTimeInMillis();

        return new long[]{start, todayEnd};
    }

    public static String encode(String str, String charset) {
        StringBuilder sb = new StringBuilder();
        try {
            byte[] key_source = str.getBytes(charset);
            for (byte b : key_source) {
                String hex = String.format("%02x", b).toUpperCase(Locale.getDefault());
                sb.append("%");
                sb.append(hex);
            }
        } catch (UnsupportedEncodingException e) {
        }//Exception
        return sb.toString();
    }

    public static String decode(String hex, String charset) {
        byte[] bytes = new byte[hex.length() / 3];
        int len = hex.length();
        for (int i = 0; i < len; ) {
            int pos = hex.substring(i).indexOf("%");
            if (pos == 0) {
                String hex_code = hex.substring(i + 1, i + 3);
                bytes[i / 3] = (byte) Integer.parseInt(hex_code, 16);
                i += 3;
            } else {
                i += pos;
            }
        }
        try {
            return new String(bytes, charset);
        } catch (UnsupportedEncodingException e) {
        }//Exception
        return "";
    }


}
