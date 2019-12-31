package com.example.zzdk;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Notification;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.PixelFormat;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import cn.pedant.SweetAlert.SweetAlertDialog;
import ezy.assist.compat.SettingsCompat;

import static com.example.zzdk.RootUtil.upgradeRootPermission;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "ZZDK";
    private static WindowManager.LayoutParams winParams;
    private Button btnStar;
    private static Button btnStarApp;
    private int y;
    private WindowManager windowManager;
    private VoiceLinerLayout playView;

    public static void notifyReceive(String packageName, Notification notification) {
        Log.i("zzz", "notifyReceive packageName=" + packageName + "notification:" + notification);
        if ("com.android.mms".equals(packageName)) {
         /*   PackageManager packageManager = MyApp.app.getPackageManager();
            Intent intent = null;
            intent = packageManager.getLaunchIntentForPackage("com.sand.airdroid");
            if (intent != null) {
              //  intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                MyApp.app.startActivity(intent);
            }*/
            if (btnStarApp != null) {
                btnStarApp.performClick();
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //当前应用的代码执行目录
        upgradeRootPermission(getPackageCodePath());
        btnStar = findViewById(R.id.btn_star);
//在MainActivity.onCreate里初始化
        Intent upservice = new Intent(this, NeNotificationService.class);
        startService(upservice);
        btnStar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openAccessibility(NeNotificationService.class.getName(), MainActivity.this);
            }
        });
        findViewById(R.id.tv_star_app).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //先关闭应用，再重启。防止airDroid进程被休眠
                SuUtil.kill("com.sand.airdroid");
                PackageManager packageManager = MyApp.app.getPackageManager();
                Intent intent = null;
                intent = packageManager.getLaunchIntentForPackage("com.sand.airdroid");
                if (intent != null) {
                    //  intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    MyApp.app.startActivity(intent);
                }
            }
        });
        btnStarApp = findViewById(R.id.tv_star_app);
        findViewById(R.id.btn_star_window)
                .setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        showWindow();
                    }
                });
        findViewById(R.id.btn_close_app)
                .setOnClickListener(v->{
                    //SuUtil.closeApp(this,"com.sand.airdroid");
                 //   doExec("am force-stop com.sand.airdroid");
                    SuUtil.kill("com.sand.airdroid");
                });
    }

    private void doExec(String cmd) {
        List<String> cmds = new ArrayList<String>();
        cmds.add( "sh");
        cmds.add( "-c");
        cmds.add(cmd);
        ProcessBuilder pb = new ProcessBuilder(cmds);
        try {
            Process p = pb.start();
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }


     View.OnTouchListener onTouchListener = new View.OnTouchListener() {
        // 触屏监听
        float lastY;
        int oldOffsetY;

        @Override
        public boolean onTouch(View v, MotionEvent event) {
            final int action = event.getAction();
            float y = -event.getY();
            oldOffsetY = winParams.y; // 偏移量
            if (action == MotionEvent.ACTION_DOWN) {
                Log.i("zzz", "event down");
                lastY = y;
            } else if (action == MotionEvent.ACTION_MOVE) {
                Log.i("zzz", "event move");
                winParams.y += (int) (y - lastY) / 3; // 减小偏移量,防止过度抖动
                //  winParams.y += (int) (y - lastY); // 减小偏移量,防止过度抖动
                windowManager.updateViewLayout(playView, winParams);
            } else {
                Log.i("zzz", "event " + action);
            }
            return true;
        }
    };


    private void showWindow() {
        //检查权限
        if (!SettingsCompat.canDrawOverlays(this)) {
            SweetAlertDialog alertDialog = new SweetAlertDialog(this);
            alertDialog.setCancelText("取消");
            alertDialog.setTitleText("该功能需要悬浮窗权限");
            alertDialog.setContentText("是否授予？");
            alertDialog.setConfirmText("去授予");
            alertDialog.setConfirmClickListener(sweetAlertDialog -> {
                // 跳转到悬浮窗权限设置页
//                SettingsCompat.manageDrawOverlays(BaseActivity.getCurrentActivity());
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION);
                    intent.setData(Uri.parse("package:" + this.getPackageName()));
                  this.startActivity(intent);
                } else {
                    SettingsCompat.manageDrawOverlays(this);
                }
                alertDialog.dismiss();
            });
            alertDialog.setOnCancelListener(dialog -> {
                alertDialog.dismiss();
            });
            alertDialog.show();
            return;
        }

        LayoutInflater inflater = LayoutInflater.from(this.getApplicationContext());
        playView = (VoiceLinerLayout) inflater.inflate(R.layout.layout_baohuo, null, false);
        playView.setOnTouchListener(onTouchListener);
        windowManager = (WindowManager) this.getSystemService(this.WINDOW_SERVICE);
        createWindowParams();
        playView.setUpdatePositionListener(new VoiceLinerLayout.UpdatePositionListener() {
            @Override
            public int getLastY() {
                return winParams.y;
            }

            @Override
            public void updateY(int y) {
                winParams.y = y;
                windowManager.updateViewLayout(playView, winParams);
            }
        });
        windowManager.addView(playView,winParams);
    }

    private void createWindowParams() {
        winParams = new WindowManager.LayoutParams(WindowManager.LayoutParams.WRAP_CONTENT, WindowManager.LayoutParams.WRAP_CONTENT,
                0, 0, PixelFormat.RGBA_8888);
        // flag 设置 Window 属性
        winParams.flags = WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
        // type 设置 Window 类别（层级）
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            winParams.type = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
        } else {
            winParams.type = WindowManager.LayoutParams.TYPE_PHONE;
        }
        winParams.width = dip2px(this, 50);
        winParams.height = dip2px(this, 50);

        winParams.gravity = Gravity.LEFT | Gravity.BOTTOM;
        y = dip2px(this, 65);
        winParams.y = y;
    }

    /**
     * 根据手机的分辨率从 dp 的单位 转成为 px(像素)
     */
    public int dip2px(Context context, float dpValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }


    @Override
    protected void onResume() {
        super.onResume();
        if (isAccessibilitySettingsOn(NeNotificationService.class.getName(), this)) {

            btnStar.setText("已经启动");
        } else {
            btnStar.setText("点击启动");
        }

    }

    /**
     * 该辅助功能开关是否打开了
     *
     * @param accessibilityServiceName：指定辅助服务名字
     * @param context：上下文
     * @return
     */
    private boolean isAccessibilitySettingsOn(String accessibilityServiceName, Context context) {
        int accessibilityEnable = 0;
        String serviceName = context.getPackageName() + "/" + accessibilityServiceName;
        try {
            accessibilityEnable = Settings.Secure.getInt(context.getContentResolver(), Settings.Secure.ACCESSIBILITY_ENABLED, 0);
        } catch (Exception e) {
            Log.e(TAG, "get accessibility enable failed, the err:" + e.getMessage());
        }
        if (accessibilityEnable == 1) {
            TextUtils.SimpleStringSplitter mStringColonSplitter = new TextUtils.SimpleStringSplitter(':');
            String settingValue = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES);
            if (settingValue != null) {
                mStringColonSplitter.setString(settingValue);
                while (mStringColonSplitter.hasNext()) {
                    String accessibilityService = mStringColonSplitter.next();
                    if (accessibilityService.equalsIgnoreCase(serviceName)) {
                        Log.v(TAG, "We've found the correct setting - accessibility is switched on!");
                        return true;
                    }
                }
            }
        } else {
            Log.d(TAG, "Accessibility service disable");
        }
        return false;
    }

    /**
     * 跳转到系统设置页面开启辅助功能
     *
     * @param accessibilityServiceName：指定辅助服务名字
     * @param context：上下文
     */
    private void openAccessibility(String accessibilityServiceName, Context context) {
        if (!isAccessibilitySettingsOn(accessibilityServiceName, context)) {
            Intent intent = new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS);
            startActivity(intent);
        }
    }

}
