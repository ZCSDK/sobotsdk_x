package com.sobot.chat.utils;

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.View;
import android.view.WindowManager;

import androidx.annotation.ColorRes;
import androidx.core.graphics.drawable.DrawableCompat;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ScreenUtils {

    /**
     * 把dip单位转成px单位
     *
     * @param context context对象
     * @param dip     dip数值
     * @return
     */
    public static int formatDipToPx(Context context, int dip) {
        DisplayMetrics dm = new DisplayMetrics();
        ((Activity) context).getWindowManager().getDefaultDisplay()
                .getMetrics(dm);
        return (int) Math.ceil(dip * dm.density);
    }

    /**
     * 把dip单位转成px单位
     *
     * @param context context对象
     * @param dip     dip数值
     * @return
     */
    public static float formatDipToPx(Context context, float dip) {
        DisplayMetrics dm = new DisplayMetrics();
        ((Activity) context).getWindowManager().getDefaultDisplay()
                .getMetrics(dm);
        return dip * dm.density;
    }

    /**
     * 把px单位转成dip单位
     *
     * @param context context对象
     * @param px      px数值
     * @return
     */
    public static int formatPxToDip(Context context, int px) {
        DisplayMetrics dm = new DisplayMetrics();
        ((Activity) context).getWindowManager().getDefaultDisplay()
                .getMetrics(dm);
        return (int) Math.ceil(((px * 160) / dm.densityDpi));
    } // www.2cto.com

    // 判断手机格式是否正确
    public static boolean isMobileNO(String mobiles) {

        Pattern p = HtmlTools.getPhoneNumberPattern();

        Matcher m = p.matcher(mobiles);

        return m.matches();

    }

    // 判断email格式是否正确
    public static boolean isEmail(String email) {

        String str = "^([a-zA-Z0-9_\\-\\.]+)@((\\[[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}\\.)|(([a-zA-Z0-9\\-]+\\.)+))([a-zA-Z]{2,4}|[0-9]{1,3})(\\]?)$";

        Pattern p = Pattern.compile(str);

        Matcher m = p.matcher(email);

        return m.matches();
    }

    /**
     * 将dp单位的值转换为px为单位的值
     *
     * @param context  上下文对象
     * @param dipValue dp为单位的值
     * @return 返回转换后的px为单位的值
     */
    public static int dip2px(Context context, float dipValue) {
        float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dipValue * scale + 0.5F);
    }

    /**
     * 将px单位的值转换为dp为单位的值
     *
     * @param context 上下文对象
     * @param pxValue px为单位的值
     * @return 返回转换后的dp为单位的值
     */
    public static int px2dip(Context context, float pxValue) {
        float scale = context.getResources().getDisplayMetrics().density;
        return (int) (pxValue / scale + 0.5F);
    }

    /**
     * 获取屏幕宽度
     *
     * @param context
     *            上下文对象
     * @return 返回屏幕宽度
     */
    /**
     * 获取屏幕的宽度
     */
    public final static int getScreenWidth(Activity activity) {
        DisplayMetrics dm = new DisplayMetrics();
        activity.getWindowManager().getDefaultDisplay().getMetrics(dm);
        return dm.widthPixels;
    }

    /**
     * 获取屏幕宽高
     *
     * @return 返回屏幕宽高
     */
    public static int[] getScreenWH(Context context) {
        //context的方法，获取windowManager
        WindowManager windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        //获取屏幕对象
        Display defaultDisplay = windowManager.getDefaultDisplay();
        //获取屏幕的宽、高
        int width = defaultDisplay.getWidth();
        int height = defaultDisplay.getHeight();
        return new int[]{width, height};
    }


    /**
     * 获取屏幕高度
     *
     * @param activity 上下文对象
     * @return 返回屏幕高度
     */
    public static int getScreenHeight(Activity activity) {
        try {
            DisplayMetrics dm = new DisplayMetrics();
            activity.getWindowManager().getDefaultDisplay().getMetrics(dm);
            return dm.heightPixels;
        } catch (Exception e) {
            return 1920;
        }
    }

    /**
     * 将px值转换为sp值，保证文字大小不变
     *
     * @param context
     * @param pxValue （DisplayMetrics类中属性scaledDensity）
     * @return
     */
    public static int px2sp(Context context, float pxValue) {
        final float fontScale = context.getResources().getDisplayMetrics().scaledDensity;
        return (int) (pxValue / fontScale + 0.5f);
    }

    /**
     * 将sp值转换为px值，保证文字大小不变
     *
     * @param context
     * @param spValue （DisplayMetrics类中属性scaledDensity）
     * @return
     */
    public static int sp2px(Context context, float spValue) {
        final float fontScale = context.getResources().getDisplayMetrics().scaledDensity;
        return (int) (spValue * fontScale + 0.5f);
    }

    public static DisplayMetrics getDisplayMetrics(Context context) {
        DisplayMetrics displaymetrics = new DisplayMetrics();
        ((WindowManager) context.getSystemService(
                Context.WINDOW_SERVICE)).getDefaultDisplay().getMetrics(
                displaymetrics);
        return displaymetrics;
    }

    public static float pixelsToDp(Context context, float f) {
        return f / (getDisplayMetrics(context).densityDpi / 160F);
    }

    public static float dpToPixel(Context context, float dp) {
        return dp * (getDisplayMetrics(context).densityDpi / 160F);
    }


    public static void setBubbleBackGroud(Context context, View view, int coumterResId) {
        if (view == null) {
            return;
        }

        Drawable tintDrawable = tintDrawable(context, view.getBackground(), coumterResId);
        setBackground(view, tintDrawable);
    }

    public static Drawable tintDrawable(Context context, Drawable drawable, @ColorRes int color) {
        if (drawable == null) {
            return null;
        }

        final Drawable wrappedDrawable = DrawableCompat.wrap(drawable);
        DrawableCompat.setTint(wrappedDrawable, context.getResources().getColor(color));
        return wrappedDrawable;
    }

    private static void setBackground(View v, Drawable bgDrawable) {
        if (v == null) {
            return;
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            v.setBackground(bgDrawable);
        } else {
            v.setBackgroundDrawable(bgDrawable);
        }
    }

    /**
     * 是否是全屏
     *
     * @return
     */
    public static boolean isFullScreen(Activity activity) {
        if (activity == null) {
            return false;
        }
        return (activity.getWindow().getAttributes().flags & WindowManager.LayoutParams.FLAG_FULLSCREEN) == WindowManager.LayoutParams.FLAG_FULLSCREEN;
    }

    /**
     * 虚拟导航栏高度
     * @param context
     * @return
     */
    public static int getNavigationBarHeight(Context context) {
        Resources resources = context.getResources();
        int resourceId = resources.getIdentifier( "navigation_bar_height_landscape", "dimen", "android");
        if (resourceId > 0) {
            int hight = resources.getDimensionPixelSize(resourceId);
            return hight;
        }
        return 0;
    }

}