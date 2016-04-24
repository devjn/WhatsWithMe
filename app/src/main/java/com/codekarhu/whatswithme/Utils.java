package com.codekarhu.whatswithme;

import android.content.Context;
import android.util.DisplayMetrics;

/**
 * Created by @author ${user} on ${date}
 * <p>
 * ${file_name}
 */
public class Utils {

    public static float density = 1;

    public Utils(Context context){
        density = context.getResources().getDisplayMetrics().density;
    }

    public static int dp(float value) {
        if (value == 0) {
            return 0;
        }
        return (int) Math.ceil(density * value);
    }

    public static void init(Context context){
        density = context.getResources().getDisplayMetrics().density;
    }

    public static int getScreenHeight(Context context) {
        DisplayMetrics metrics = context.getResources().getDisplayMetrics();
        return metrics.heightPixels;
    }
}
