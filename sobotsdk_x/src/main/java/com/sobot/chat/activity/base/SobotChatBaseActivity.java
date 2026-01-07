package com.sobot.chat.activity.base;


import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.content.ContextCompat;
import androidx.core.view.OnApplyWindowInsetsListener;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.sobot.chat.MarkConfig;
import com.sobot.chat.R;
import com.sobot.chat.ZCSobotApi;
import com.sobot.chat.ZCSobotConstant;
import com.sobot.chat.activity.SobotCameraActivity;
import com.sobot.chat.api.ZhiChiApi;
import com.sobot.chat.api.apiUtils.SobotApp;
import com.sobot.chat.api.apiUtils.SobotBaseUrl;
import com.sobot.chat.api.model.HelpConfigModel;
import com.sobot.chat.api.model.ZhiChiInitModeBase;
import com.sobot.chat.application.MyApplication;
import com.sobot.chat.core.HttpUtils;
import com.sobot.chat.core.channel.SobotMsgManager;
import com.sobot.chat.listener.PermissionListener;
import com.sobot.chat.listener.PermissionListenerImpl;
import com.sobot.chat.notchlib.INotchScreen;
import com.sobot.chat.notchlib.NotchScreenManager;
import com.sobot.chat.utils.ChatUtils;
import com.sobot.chat.utils.CommonUtils;
import com.sobot.chat.utils.LogUtils;
import com.sobot.chat.utils.SharedPreferencesUtil;
import com.sobot.chat.utils.StringUtils;
import com.sobot.chat.utils.ZhiChiConstant;
import com.sobot.chat.widget.statusbar.StatusBarUtil;
import com.sobot.chat.widget.toast.ToastUtil;
import com.sobot.utils.SobotSharedPreferencesUtil;

import java.io.File;
import java.util.Locale;

public abstract class SobotChatBaseActivity extends AppCompatActivity {

    public ZhiChiApi zhiChiApi;

    protected File cameraFile;

    //权限回调
    public PermissionListener permissionListener;
    private int initMode;
    private View overlay;//权限用途提示蒙层
    private ViewGroup viewGroup;//根view content
    public boolean isContinueShooting = false;//是否点击过继续拍摄

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        //修改国际化语言
        changeAppLanguage();
        super.onCreate(savedInstanceState);
        if (getSobotBaseContext() != null && getDelegate() != null) {
            //暗夜模式设置：默认跟随系统，可以根据设置切换
            int local_night_mode = SharedPreferencesUtil.getIntData(getSobotBaseContext(), ZCSobotConstant.LOCAL_NIGHT_MODE, AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
            if (local_night_mode != 0) {
                getDelegate().setLocalNightMode(local_night_mode); //切换模式
            }
        }
        initMode = getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK;
        if (Build.VERSION.SDK_INT != Build.VERSION_CODES.O) {
            if (!ZCSobotApi.getSwitchMarkStatus(MarkConfig.LANDSCAPE_SCREEN)) {
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT);//竖屏
            } else {
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE);//横屏

            }
        }
        if (ZCSobotApi.getSwitchMarkStatus(MarkConfig.LANDSCAPE_SCREEN) && ZCSobotApi.getSwitchMarkStatus(MarkConfig.DISPLAY_INNOTCH)) {
            // 支持显示到刘海区域
            NotchScreenManager.getInstance().setDisplayInNotch(this);
            // 设置Activity全屏
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        }

        setContentView(getContentViewResId());
        String host = SharedPreferencesUtil.getStringData(getSobotBaseContext(), ZhiChiConstant.SOBOT_SAVE_HOST_AFTER_INITSDK, SobotBaseUrl.getApi_Host());
        if (!host.equals(SobotBaseUrl.getApi_Host())) {
            SobotBaseUrl.setApi_Host(host);
        }

        try {
            View decorView = getWindow().getDecorView();
            ViewCompat.setOnApplyWindowInsetsListener(decorView, new OnApplyWindowInsetsListener() {
                @Override
                public WindowInsetsCompat onApplyWindowInsets(View v, WindowInsetsCompat insets) {
                    int statusBarHeight = insets.getInsets(WindowInsetsCompat.Type.systemBars()).top;
                    int bottomInset = insets.getInsets(WindowInsetsCompat.Type.systemBars()).bottom;
                    View rootView = findViewById(R.id.view_root);
                    int targetSdkVersion = CommonUtils.getTargetSdkVersion(getSobotBaseActivity());
                    if (rootView != null && Build.VERSION.SDK_INT >= 35 && targetSdkVersion >= 35) {
                        //android 15 api 35 全屏沉侵式 底部避让
                        rootView.setPadding(0, 0, 0, bottomInset);
                    }
                    LogUtils.d("状态栏高度: " + statusBarHeight);
                    StatusBarUtil.SOBOT_STATUS_HIGHT = statusBarHeight;
                    if (SobotApp.getApplicationContext() != null) {
                        SobotSharedPreferencesUtil.getInstance(SobotApp.getApplicationContext()).put("SobotStatusBarHeight", statusBarHeight);
                    }
                    setUpToolBar();
                    return insets;
                }
            });
        } catch (Exception e) {
            setUpToolBar();
        }
        setUpToolBar();
        zhiChiApi = SobotMsgManager.getInstance(getApplicationContext()).getZhiChiApi();
        MyApplication.getInstance().addActivity(this);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
        View toolBar = findViewById(R.id.sobot_layout_titlebar);
        if (toolBar != null) {
            setUpToolBarLeftMenu();

            setUpToolBarRightMenu();
        }
        try {
            initBundleData(savedInstanceState);
            initView();
            initData();
        } catch (Exception e) {
            e.printStackTrace();
        }

        //左上角返回按钮水滴屏适配
        if (getLeftMenu() != null) {
            displayInNotch(getLeftMenu());
        }
    }

    public void displayInNotch(final View view) {
        if (ZCSobotApi.getSwitchMarkStatus(MarkConfig.LANDSCAPE_SCREEN) && ZCSobotApi.getSwitchMarkStatus(MarkConfig.DISPLAY_INNOTCH) && view != null) {
            // 获取刘海屏信息
            NotchScreenManager.getInstance().getNotchInfo(this, new INotchScreen.NotchScreenCallback() {
                @Override
                public void onResult(INotchScreen.NotchScreenInfo notchScreenInfo) {
                    if (notchScreenInfo.hasNotch) {
                        for (Rect rect : notchScreenInfo.notchRects) {
                            if (view instanceof WebView && view.getParent() instanceof LinearLayout) {
                                LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) view.getLayoutParams();
                                layoutParams.rightMargin = (rect.right > 90 ? 90 : rect.right) + 44;
                                layoutParams.leftMargin = (rect.right > 90 ? 90 : rect.right) + 44;
                                view.setLayoutParams(layoutParams);
                            } else if (view instanceof WebView && view.getParent() instanceof RelativeLayout) {
                                RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) view.getLayoutParams();
                                layoutParams.rightMargin = (rect.right > 90 ? 90 : rect.right) + 44;
                                layoutParams.leftMargin = (rect.right > 90 ? 90 : rect.right) + 44;
                                view.setLayoutParams(layoutParams);
                            } else {
                                view.setPadding((rect.right > 90 ? 90 : rect.right) + view.getPaddingLeft(), view.getPaddingTop(), (rect.right > 90 ? 90 : rect.right) + view.getPaddingRight(), view.getPaddingBottom());
                            }
                        }
                    }
                }
            });

        }
    }

    public void changeAppLanguage() {
        Locale language = (Locale) SharedPreferencesUtil.getObject(SobotChatBaseActivity.this, ZhiChiConstant.SOBOT_LANGUAGE);
        if (language != null) {
            try {
                // 本地语言设置
                Resources res = getResources();
                DisplayMetrics dm = res.getDisplayMetrics();
                Configuration conf = new Configuration();
                conf.locale = language;
                res.updateConfiguration(conf, dm);
            } catch (Exception e) {
            }
        }
    }


    protected void setUpToolBarRightMenu() {
        if (getRightMenu() != null) {
            //找到 Toolbar 的返回按钮,并且设置点击事件,点击关闭这个 Activity
            getRightMenu().setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onRightMenuClick(v);
                }
            });
        }
    }

    protected void setUpToolBarLeftMenu() {
        if (getLeftMenu() != null) {
            //找到 Toolbar 的返回按钮,并且设置点击事件,点击关闭这个 Activity
            getLeftMenu().setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onLeftMenuClick(v);
                }
            });
        }
    }


    protected void setUpToolBar() {
        View toolBar = getToolBar();
        if (toolBar == null) {
            return;
        }
        updateToolBarBg();
    }

    protected View getToolBar() {
        return findViewById(R.id.sobot_layout_titlebar);
    }

    protected ImageView getLeftMenu() {
        return findViewById(R.id.sobot_iv_left);
    }

    protected ImageView getRightImagMenu() {
        return findViewById(R.id.sobot_iv_right);
    }

    protected TextView getRightMenu() {
        return findViewById(R.id.sobot_tv_right);
    }

    protected TextView getTitleView() {
        return findViewById(R.id.sobot_text_title);
    }


    /**
     * @param resourceId
     * @param textId
     * @param isShow
     */
    protected void showRightMenu(int resourceId, String textId, boolean isShow) {
        View tmpMenu = getRightMenu();
        if (tmpMenu == null || !(tmpMenu instanceof TextView)) {
            return;
        }
        TextView rightMenu = (TextView) tmpMenu;
        if (!TextUtils.isEmpty(textId)) {
            rightMenu.setText(textId);
        } else {
            rightMenu.setText("");
        }

        if (resourceId != 0) {
            Drawable img = getResources().getDrawable(resourceId);
            img.setBounds(0, 0, img.getMinimumWidth(), img.getMinimumHeight());
            rightMenu.setCompoundDrawables(null, null, img, null);
        } else {
            rightMenu.setCompoundDrawables(null, null, null, null);
        }

        if (isShow) {
            rightMenu.setVisibility(View.VISIBLE);
        } else {
            rightMenu.setVisibility(View.GONE);
        }
    }

    /**
     * @param isShow
     */
    protected void showLeftMenu(boolean isShow) {
        View tmpMenu = getLeftMenu();
        if (tmpMenu == null) {
            return;
        }
        if (isShow) {
            tmpMenu.setVisibility(View.VISIBLE);
        } else {
            tmpMenu.setVisibility(View.GONE);
        }
    }

    @Override
    protected void onDestroy() {
        HttpUtils.getInstance().cancelTag(SobotChatBaseActivity.this);
        MyApplication.getInstance().deleteActivity(this);
        super.onDestroy();
    }

    /**
     * 导航栏左边点击事件
     *
     * @param view
     */
    protected void onLeftMenuClick(View view) {
        onBackPressed();
    }

    /**
     * 导航栏右边点击事件
     *
     * @param view
     */
    protected void onRightMenuClick(View view) {

    }

    public void setTitle(CharSequence title) {
        View tmpMenu = getTitleView();
        if (tmpMenu == null || !(tmpMenu instanceof TextView)) {
            return;
        }
        TextView tvTitle = (TextView) tmpMenu;
        tvTitle.setText(title);
    }

    public void setTitle(int title) {
        View tmpMenu = getTitleView();
        if (tmpMenu == null || !(tmpMenu instanceof TextView)) {
            return;
        }
        TextView tvTitle = (TextView) tmpMenu;
        tvTitle.setVisibility(View.VISIBLE);
        tvTitle.setText(title);
    }

    //返回布局id
    protected abstract int getContentViewResId();

    protected void initBundleData(Bundle savedInstanceState) {
    }

    protected abstract void initView();

    protected abstract void initData();

    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case ZhiChiConstant.SOBOT_PERMISSIONS_REQUEST_ACTIVITY_CODE:
                try {
                    for (int i = 0; i < grantResults.length; i++) {
                        //判断权限的结果，如果有被拒绝，就return
                        if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                            if (permissions[i] != null && permissions[i].equals(Manifest.permission.RECORD_AUDIO)) {
                                showPerssionSettingUi();
                                return;
                            } else if (permissions[i] != null && permissions[i].equals(Manifest.permission.CAMERA)) {
                                showPerssionSettingUi();
                                return;
                            }
                        }
                    }
                    if (permissionListener != null) {
                        permissionListener.onPermissionSuccessListener();
                    }
                    removePerssionUi();
                } catch (Exception e) {
//                    e.printStackTrace();
                }
                break;
        }
    }

    /**
     * 检测是否没有对应的权限，没有权限显示提示蒙层
     *
     * @param type                1：文件 2：麦克风 3：相机
     * @param checkPermissionType 0：图片权限 1：视频权限，2：音频权限，3，所有细分的权限， android 13 使用
     * @return true :有权限 false:没有权限
     */
    public boolean isHasPermission(int type, int checkPermissionType) {
        boolean isHasPermission = false;
        if (type == 1) {
            isHasPermission = true;
        } else if (type == 2) {
            isHasPermission = checkAudioPermission();
            if (!isHasPermission) {
                showPerssionUi(2);
                if (ZCSobotApi.getSwitchMarkStatus(MarkConfig.LANDSCAPE_SCREEN)) {
                    //横屏
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            hidePerssionUi();
                            //申请麦克风权限
                            requestAudioPermission();
                        }
                    }, 2000);
                } else {
                    //申请麦克风权限
                    requestAudioPermission();
                }
            }
        } else if (type == 3) {
            isHasPermission = checkCameraPermission();
            if (!isHasPermission) {
                showPerssionUi(3);
                if (ZCSobotApi.getSwitchMarkStatus(MarkConfig.LANDSCAPE_SCREEN)) {
                    //横屏
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            hidePerssionUi();
                            //申请相机权限
                            requestCameraPermission();
                        }
                    }, 2000);
                } else {
                    //申请相机权限
                    requestCameraPermission();
                }
            }
        } else if (type == 4) {
            isHasPermission = checkAudioPermission();
            if (!isHasPermission) {
                showPerssionUi(4);
                if (ZCSobotApi.getSwitchMarkStatus(MarkConfig.LANDSCAPE_SCREEN)) {
                    //横屏
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            hidePerssionUi();
                            //申请麦克风权限
                            requestAudioPermission();
                        }
                    }, 2000);
                } else {
                    //申请麦克风权限
                    requestAudioPermission();
                }
            }
        }
        return isHasPermission;
    }

    /**
     * 显示权限蒙层
     *
     * @param type 0：照片和视频 1：文件 2：麦克风 3：相机
     */
    public void showPerssionUi(int type) {
        isContinueShooting = false;
        overlay = LayoutInflater.from(getSobotBaseActivity()).inflate(R.layout.sobot_layout_overlay, null);
        if (overlay != null) {
            overlay.setVisibility(View.VISIBLE);
            final LinearLayout ll_info = overlay.findViewById(R.id.ll_info);
            final LinearLayout ll_setting = overlay.findViewById(R.id.ll_setting);
            TextView tv_content = overlay.findViewById(R.id.tv_content);
            Button btn_left = overlay.findViewById(R.id.btn_left);
            Button btn_right = overlay.findViewById(R.id.btn_right);
            TextView tv_setting_title = overlay.findViewById(R.id.tv_setting_title);
            TextView tv_setting_content = overlay.findViewById(R.id.tv_setting_content);
            if (type == 0) {
                tv_content.setText("\"" + CommonUtils.getAppName(getSobotBaseActivity()) + "\" " + getResources().getString(R.string.sobot_album_permission_yongtu));
                tv_setting_title.setText(getResources().getString(R.string.sobot_please_open_album));
                tv_setting_content.setText(getResources().getString(R.string.sobot_use_album));
            } else if (type == 1) {
                tv_content.setText("\"" + CommonUtils.getAppName(getSobotBaseActivity()) + "\" " + getResources().getString(R.string.sobot_storage_permission_yongtu));
                tv_setting_title.setText(getResources().getString(R.string.sobot_please_open_storage));
                tv_setting_content.setText(getResources().getString(R.string.sobot_use_storage));
            } else if (type == 2) {
                tv_content.setText("\"" + CommonUtils.getAppName(getSobotBaseActivity()) + "\" " + getResources().getString(R.string.sobot_microphone_permission_yongtu));
                tv_setting_title.setText(getResources().getString(R.string.sobot_please_open_microphone));
                tv_setting_content.setText(getResources().getString(R.string.sobot_use_microphone));
            } else if (type == 3) {
                tv_content.setText("\"" + CommonUtils.getAppName(getSobotBaseActivity()) + "\" " + getResources().getString(R.string.sobot_camera_permission_yongtu));
                tv_setting_title.setText(getResources().getString(R.string.sobot_please_open_camera));
                tv_setting_content.setText(getResources().getString(R.string.sobot_use_camera));
            } else if (type == 4) {
                tv_content.setText("\"" + CommonUtils.getAppName(getSobotBaseActivity()) + "\" " + getResources().getString(R.string.sobot_microphone_permission_yongtu_camera));
                tv_setting_title.setText(getResources().getString(R.string.sobot_no_microphone));
                String tempStr = getResources().getString(R.string.sobot_no_microphone_des);
                tv_setting_content.setText(String.format(tempStr, CommonUtils.getAppName(getSobotBaseActivity())));
                btn_left.setText(getResources().getString(R.string.sobot_continue_shooting));
            }
            viewGroup = getSobotBaseActivity().findViewById(android.R.id.content);
            viewGroup.addView(overlay);
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    if (ll_setting.getVisibility() == View.GONE) {
                        ll_info.setVisibility(View.VISIBLE);
                    }
                }
            }, 200);//延迟0.3s 是避免多次拒绝后ll_info 隐藏会出现闪一下的问题
            overlay.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    removePerssionUi();
                }
            });
            btn_left.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    removePerssionUi();
                    if (getResources().getString(R.string.sobot_continue_shooting).equals(btn_left.getText().toString())) {
                        isContinueShooting = true;
                    }
                }
            });
            btn_right.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    removePerssionUi();
                    Uri packageURI = Uri.parse("package:" + getSobotBaseActivity().getPackageName());
                    Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, packageURI);
                    startActivity(intent);
                }
            });
        }
    }

    //拒绝权限后显示 去设置UI
    public void showPerssionSettingUi() {
        String permissionTitle = "";
        if (overlay != null) {
            overlay.setVisibility(View.VISIBLE);
            LinearLayout ll_info = overlay.findViewById(R.id.ll_info);
            LinearLayout ll_setting = overlay.findViewById(R.id.ll_setting);
            TextView tv_content = overlay.findViewById(R.id.tv_content);
            ll_info.setVisibility(View.GONE);
            ll_setting.setVisibility(View.VISIBLE);
            if (tv_content != null && StringUtils.isNoEmpty(tv_content.getText().toString())) {
                permissionTitle = tv_content.getText().toString();
            }
        }
        if (permissionListener != null) {
            permissionListener.onPermissionErrorListener(getSobotBaseActivity(), permissionTitle);
        }
    }

    //移除权限提示蒙层
    public void removePerssionUi() {
        if (overlay != null) {
            if (viewGroup == null) {
                viewGroup = findViewById(android.R.id.content);
            }
            viewGroup.removeView(overlay);
        }
    }

    //隐藏权限提示蒙层
    public void hidePerssionUi() {
        if (overlay != null) {
            overlay.setVisibility(View.GONE);
        }
    }


    /**
     * 检查录音权限
     *
     * @return true, 已经获取权限;false,没有权限,尝试获取
     */
    protected boolean checkAudioPermission() {
        if (Build.VERSION.SDK_INT >= 23 && CommonUtils.getTargetSdkVersion(getSobotBaseActivity().getApplicationContext()) >= 23) {
            if (ContextCompat.checkSelfPermission(getSobotBaseActivity(), Manifest.permission.RECORD_AUDIO)
                    != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }

    /**
     * 申请录音权限
     */
    protected void requestAudioPermission() {
        if (Build.VERSION.SDK_INT >= 23 && CommonUtils.getTargetSdkVersion(getSobotBaseActivity().getApplicationContext()) >= 23) {
            if (ContextCompat.checkSelfPermission(getSobotBaseActivity(), Manifest.permission.RECORD_AUDIO)
                    != PackageManager.PERMISSION_GRANTED) {
                this.requestPermissions(new String[]{Manifest.permission.RECORD_AUDIO},
                        ZhiChiConstant.SOBOT_PERMISSIONS_REQUEST_ACTIVITY_CODE);
            }
        }
    }


    /**
     * 检查相机权限
     *
     * @return true, 已经获取权限;false,没有权限,尝试获取
     */
    protected boolean checkCameraPermission() {
        if (Build.VERSION.SDK_INT >= 23 && CommonUtils.getTargetSdkVersion(getSobotBaseActivity().getApplicationContext()) >= 23) {
            if (ContextCompat.checkSelfPermission(getSobotBaseActivity(), Manifest.permission.CAMERA)
                    != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }

    /**
     * 申请相机权限
     */
    protected boolean requestCameraPermission() {
        if (Build.VERSION.SDK_INT >= 23 && CommonUtils.getTargetSdkVersion(getSobotBaseActivity().getApplicationContext()) >= 23) {
            if (ContextCompat.checkSelfPermission(getSobotBaseActivity(), Manifest.permission.CAMERA)
                    != PackageManager.PERMISSION_GRANTED) {
                this.requestPermissions(new String[]{Manifest.permission.CAMERA}
                        , ZhiChiConstant.SOBOT_PERMISSIONS_REQUEST_ACTIVITY_CODE);
                return false;
            }
        }
        return true;
    }


    /**
     * 通过照相上传图片
     */
    public void selectPicFromCamera() {
        if (!CommonUtils.isExitsSdcard()) {
            ToastUtil.showCustomToast(getSobotBaseActivity().getApplicationContext(), getString(R.string.sobot_sdcard_does_not_exist),
                    Toast.LENGTH_SHORT);
            return;
        }

        permissionListener = new PermissionListenerImpl() {
            @Override
            public void onPermissionSuccessListener() {
                //如果有拍照所需的权限，跳转到拍照界面
                startActivityForResult(SobotCameraActivity.newIntent(getSobotBaseContext()), ChatUtils.REQUEST_CODE_CAMERA);
            }
        };

        if (!isHasPermission(3, 3)) {
            return;
        }

        // 打开拍摄页面
        startActivityForResult(SobotCameraActivity.newIntent(getSobotBaseContext()), ChatUtils.REQUEST_CODE_CAMERA);
    }

    //判断相机是否可用
    public boolean isCameraCanUse() {
        if (getSobotBaseActivity() == null) return false;

        CameraManager cameraManager = (CameraManager) getSobotBaseActivity().getSystemService(Context.CAMERA_SERVICE);
        if (cameraManager == null) return false;

        try {
            for (String cameraId : cameraManager.getCameraIdList()) {
                CameraCharacteristics characteristics = cameraManager.getCameraCharacteristics(cameraId);
                Integer facing = characteristics.get(CameraCharacteristics.LENS_FACING);
                // 只检测后置或前置摄像头是否存在
                if (facing != null && facing == CameraCharacteristics.LENS_FACING_BACK) {
                    return true; // 存在后置摄像头
                }
            }
        } catch (CameraAccessException e) {
        }

        return false;
    }

    /**
     * 从图库获取图片
     */
    public void selectPicFromLocal() {
        try {
            Intent intent = new Intent(MediaStore.ACTION_PICK_IMAGES);
            intent.setType("image/*");
            startActivityForResult(intent, ZhiChiConstant.REQUEST_CODE_picture);
        } catch (Exception e) {
        }
    }


    /**
     * 从图库获取视频
     */
    public void selectVedioFromLocal() {
        try {
            Intent intent = new Intent(MediaStore.ACTION_PICK_IMAGES);
            intent.setType("video/*");
            startActivityForResult(intent, ZhiChiConstant.REQUEST_CODE_picture);
        } catch (Exception e) {
        }
    }

    public SobotChatBaseActivity getSobotBaseActivity() {
        return this;
    }

    public Context getSobotBaseContext() {
        return this;
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        int currentNightMode = newConfig.uiMode & Configuration.UI_MODE_NIGHT_MASK;
        if (initMode != currentNightMode) {
            initMode = currentNightMode;
            recreate();
        }
    }

    /**
     * 是否是全屏
     *
     * @return
     */
    protected boolean isFullScreen() {
        return (getWindow().getAttributes().flags & WindowManager.LayoutParams.FLAG_FULLSCREEN) == WindowManager.LayoutParams.FLAG_FULLSCREEN;
    }

    /**
     * 导航栏渐变逻辑
     * 先判断客户开发是否设置，如果设置了 直接使用；如果没有修改（和系统默认一样），就就绪判断后端接口返回的颜色；
     * 如果接口返回的也和系统一样，就不处理（默认渐变色）；如果不一样，直接按照接口的设置渐变色
     */
    private void updateToolBarBg() {
        try {
            ZhiChiInitModeBase initModel = (ZhiChiInitModeBase) SharedPreferencesUtil.getObject(getSobotBaseActivity(),
                    ZhiChiConstant.sobot_last_current_initModel);
            if (initModel == null) {
                setToolBarDefBg();
                return;
            }
            if (getToolBar() == null) {
                return;
            }
            if (initModel.getVisitorScheme() != null) {
                //导航条显示1 开启 0 关闭
//                if (initModel.getVisitorScheme().getTopBarFlag() == 1) {
//                    getToolBar().setVisibility(View.VISIBLE);
//                } else {
//                    getToolBar().setVisibility(View.GONE);
//                }
                getToolBar().setVisibility(View.VISIBLE);
            }
            if (initModel.getVisitorScheme() != null) {
                //服务端返回的导航条背景颜色
                if (!TextUtils.isEmpty(initModel.getVisitorScheme().getTopBarColor())) {
                    String topBarColor[] = initModel.getVisitorScheme().getTopBarColor().split(",");
                    if (topBarColor.length > 1) {
                        int[] colors = new int[topBarColor.length];
                        for (int i = 0; i < topBarColor.length; i++) {
                            colors[i] = Color.parseColor(topBarColor[i]);
                        }
                        GradientDrawable gradientDrawable = new GradientDrawable();
                        gradientDrawable.setShape(GradientDrawable.RECTANGLE);
                        gradientDrawable.setColors(colors); //添加颜色组
                        gradientDrawable.setGradientType(GradientDrawable.LINEAR_GRADIENT);//设置线性渐变
                        gradientDrawable.setOrientation(GradientDrawable.Orientation.LEFT_RIGHT);//设置渐变方向
                        getToolBar().setBackground(gradientDrawable);
                        GradientDrawable aDrawable = new GradientDrawable(GradientDrawable.Orientation.LEFT_RIGHT, colors);
                        if (ZCSobotApi.getSwitchMarkStatus(MarkConfig.LANDSCAPE_SCREEN) && ZCSobotApi.getSwitchMarkStatus(MarkConfig.DISPLAY_INNOTCH)) {
                        } else {
                            StatusBarUtil.setColor(getSobotBaseActivity(), aDrawable);
                        }
                    }
                }
            } else {
                setToolBarDefBg();
            }
        } catch (Exception e) {
        }
    }

    /**
     * 设置默认导航栏渐变色
     */
    private void setToolBarDefBg() {
        try {
            HelpConfigModel configModel = (HelpConfigModel) SharedPreferencesUtil.getObject(getSobotBaseActivity(), "SobotHelpConfigModel");
            int[] colors = null;
            if (configModel != null && StringUtils.isNoEmpty(configModel.getTopBarColor())) {
                String topBarColor[] = configModel.getTopBarColor().split(",");
                if (topBarColor.length > 1) {
                    colors = new int[topBarColor.length];
                    for (int i = 0; i < topBarColor.length; i++) {
                        colors[i] = Color.parseColor(topBarColor[i]);
                    }
                } else {
                    colors = new int[]{Color.parseColor(topBarColor[0])};
                }
            } else {
                colors = new int[]{getResources().getColor(R.color.sobot_color_title_bar_left_bg), getResources().getColor(R.color.sobot_color_title_bar_bg)};
            }
            GradientDrawable gradientDrawable = new GradientDrawable();
            gradientDrawable.setShape(GradientDrawable.RECTANGLE);
            gradientDrawable.setColors(colors); //添加颜色组
            gradientDrawable.setGradientType(GradientDrawable.LINEAR_GRADIENT);//设置线性渐变
            gradientDrawable.setOrientation(GradientDrawable.Orientation.LEFT_RIGHT);//设置渐变方向
            getToolBar().setBackground(gradientDrawable);
            if (ZCSobotApi.getSwitchMarkStatus(MarkConfig.LANDSCAPE_SCREEN) && ZCSobotApi.getSwitchMarkStatus(MarkConfig.DISPLAY_INNOTCH)) {
            } else {
                GradientDrawable aDrawable = new GradientDrawable(GradientDrawable.Orientation.LEFT_RIGHT, colors);
                StatusBarUtil.setColor(getSobotBaseActivity(), aDrawable);
            }
        } catch (Exception e) {
        }
    }
}