package cn.modificator.launcher.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.admin.DevicePolicyManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.BatteryManager;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.text.format.DateFormat;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

import cn.modificator.launcher.AppCenter;
import cn.modificator.launcher.R;
import cn.modificator.launcher.common.Config;
import cn.modificator.launcher.common.Constants;
import cn.modificator.launcher.fragments.SettingFragment;
import cn.modificator.launcher.ftpservice.FTPReceiver;
import cn.modificator.launcher.ftpservice.FTPService;
import cn.modificator.launcher.model.AdminReceiver;
import cn.modificator.launcher.model.HomeEntranceService;
import cn.modificator.launcher.model.WifiControl;
import cn.modificator.launcher.utils.Utils;
import cn.modificator.launcher.widgets.EInkLauncherView;

/**
 * Created by mod on 16-4-22.
 */
public class LauncherActivity extends Activity {

    EInkLauncherView launcherView;
    AppCenter appCenter = null;
    Config config;
    LauncherUpdateReceiver updateReceiver;
    TextView pageStatus;
    // BatteryView batteryProgress;
    TextView tvBattery;
    TextView batteryStatus, textClock;

    BroadcastReceiver timeListener;
    Calendar mCalendar;

    DevicePolicyManager policyManager;
    File iconFile;
    boolean isChina = true;
    FTPReceiver ftpReceiver = new FTPReceiver();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_launcher);
        config = new Config(this);
        WifiControl.init(this);
        toggleStatusBar();
        if (getExternalCacheDir() != null) {
            iconFile = new File(getExternalCacheDir().getParentFile().getParentFile().getParentFile().getParentFile(), "E-Ink Launcher" + File.separator + "icon");
            if (!iconFile.exists()) {
                iconFile.mkdir();
            }
        }

        isChina = getResources().getConfiguration().locale.getCountry().equals("CN");

        initView();
        checkLaunchHomeNotification();
    }


    private void initView() {
        policyManager = (DevicePolicyManager) getSystemService(Context.DEVICE_POLICY_SERVICE);
        launcherView = findViewById(R.id.mList);
        pageStatus = findViewById(R.id.pageStatus);
        // batteryProgress = findViewById(R.id.batteryProgress);
        tvBattery = findViewById(R.id.tv_battery);
        batteryStatus = findViewById(R.id.batteryStatus);
        textClock = findViewById(R.id.textClock);

        launcherView.setHideAppPkg(config.getHideApps());
        launcherView.setHideDivider(config.getDividerHideStatus());
        launcherView.setFontSize(config.getFontSize());

        appCenter = new AppCenter(this);
        appCenter.setHideApps(config.getHideApps());
        appCenter.setPageStatus(pageStatus);
        appCenter.setLauncherView(launcherView);

        //加载之前保存的桌面数据
        updateColNum(config.getColNum());
        updateRowNum(config.getRowNum());
        updateSortType(config.getSortType());

        findViewById(R.id.lastPage).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                appCenter.showLastPage();
            }
        });
        findViewById(R.id.nextPage).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                appCenter.showNextPage();
            }
        });
        findViewById(R.id.toSetting).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getFragmentManager().beginTransaction()
                        .replace(android.R.id.content, new SettingFragment())
                        .addToBackStack(null)
                        .commit();
            }
        });
        findViewById(R.id.pageStatus).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getFragmentManager().beginTransaction()
                        .replace(android.R.id.content, new SettingFragment())
                        .addToBackStack(null)
                        .commit();
            }
        });
        findViewById(R.id.deleteFinish).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                launcherView.setDelete(false);
                appCenter.refreshAppList();
                config.setHideApps(appCenter.getHideApps());
                v.setVisibility(View.GONE);
            }
        });
        launcherView.setTouchListener(new EInkLauncherView.TouchListener() {
            @Override
            public void toNext() {
                appCenter.showNextPage();
            }

            @Override
            public void toLast() {
                appCenter.showLastPage();
            }
        });
        mCalendar = Calendar.getInstance();

        updateTimeShow();

        updateReceiver = new LauncherUpdateReceiver();
        IntentFilter filter = new IntentFilter();
        filter.addAction(Constants.LAUNCHER_ACTION);
        registerReceiver(updateReceiver, filter);

        IntentFilter appChangeFilter = new IntentFilter();
        appChangeFilter.addAction(Intent.ACTION_PACKAGE_ADDED);
        appChangeFilter.addAction(Intent.ACTION_PACKAGE_REMOVED);
        appChangeFilter.addAction(Intent.ACTION_PACKAGE_REPLACED);
        appChangeFilter.addDataScheme("package");
        registerReceiver(appChangeReceiver, appChangeFilter);

        try {
            launcherView.setSystemApp(!isUserApp(getPackageManager().getPackageInfo(getPackageName(), 0)));
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
    }

    private void updateRowNum(int rowNum) {
        launcherView.setRowNum(rowNum);
        appCenter.setRowNum(rowNum);
        config.setRowNum(rowNum);
    }

    private void updateColNum(int colNum) {
        launcherView.setColNum(colNum);
        appCenter.setColNum(colNum);
        config.setColNum(colNum);
    }

    private void updateSortType(int sortType) {
        launcherView.setSortType(sortType);
        appCenter.setSortType(sortType);
        config.setSortType(sortType);
        // 刷新所有App
        appCenter.refreshAppList();
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_PAGE_UP) {
            appCenter.showLastPage();
        } else if (keyCode == KeyEvent.KEYCODE_PAGE_DOWN) {
            appCenter.showNextPage();
        } else if (keyCode == KeyEvent.KEYCODE_BACK) {
            return true;
        }
        return super.onKeyUp(keyCode, event);
    }

    @Override
    protected void onResume() {
        super.onResume();
        IntentFilter batteryLevelFilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
        registerReceiver(batteryLevelRcvr, batteryLevelFilter);
        timeListener = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                updateTimeShow();
            }
        };
        updateTimeShow();

        registerReceiver(timeListener, new IntentFilter(Intent.ACTION_TIME_TICK));
        if (launcherView != null) launcherView.refreshReplaceIcon();
        detectionUSB();

        IntentFilter ftpIntentFilter = new IntentFilter(FTPService.ACTION_START_FTPSERVER);
        ftpIntentFilter.addAction(FTPService.ACTION_STOP_FTPSERVER);
        registerReceiver(ftpReceiver, ftpIntentFilter);
    }

    /**
     * 刷新时间显示
     */
    private void updateTimeShow() {
        if (textClock != null && mCalendar != null) {
            boolean is24Hour = DateFormat.is24HourFormat(this);
            mCalendar.setTimeInMillis(System.currentTimeMillis());
            StringBuilder timeFormatTextBuilder = new StringBuilder();
            if (!is24Hour && isChina) {
                timeFormatTextBuilder.append(Utils.getAMPMCNString(mCalendar.get(Calendar.HOUR), mCalendar.get(Calendar.AM_PM)));
            }
            if (is24Hour) {
                timeFormatTextBuilder.append(" HH:mm");
            } else {
                timeFormatTextBuilder.append(" hh:mm");
            }
            if (!is24Hour && !isChina) {
                timeFormatTextBuilder.append(" a");
            }
            // timeFormatTextBuilder.append(" EEEE");
            textClock.setText(new SimpleDateFormat(timeFormatTextBuilder.toString(), Locale.getDefault()).format(mCalendar.getTime()));
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(batteryLevelRcvr);
        unregisterReceiver(timeListener);
        unregisterReceiver(usbReceiver);
        unregisterReceiver(ftpReceiver);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(updateReceiver);
        unregisterReceiver(appChangeReceiver);
        try {
            unregisterReceiver(usbReceiver);
        } catch (Throwable throwable) {
            throwable.printStackTrace();
        }
    }

    class LauncherUpdateReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            Bundle bundle = intent.getExtras();
            if (bundle.containsKey(Constants.ROW_NUM_KEY)) {
                updateRowNum(bundle.getInt(Constants.ROW_NUM_KEY));
            } else if (bundle.containsKey(Constants.COL_NUM_KEY)) {
                updateColNum(bundle.getInt(Constants.COL_NUM_KEY));
            } else if (bundle.containsKey(Constants.SORT_TYPE_KEY)) {
                updateSortType(bundle.getInt(Constants.SORT_TYPE_KEY));
            } else if (bundle.containsKey(Constants.DELETEAPP)) {
                launcherView.setDelete(true);
                // 刷新所有App
                appCenter.refreshAppList(true);
                findViewById(R.id.deleteFinish).setVisibility(View.VISIBLE);
            } else if (bundle.containsKey(Constants.LAUNCHER_FONT_SIZE)) {
                launcherView.setFontSize(bundle.getFloat(Constants.LAUNCHER_FONT_SIZE));
            } else if (bundle.containsKey(Constants.LAUNCHER_HIDE_DIVIDER)) {
                launcherView.setHideDivider(bundle.getBoolean(Constants.LAUNCHER_HIDE_DIVIDER));
                config.setDividerHideStatus(bundle.getBoolean(Constants.LAUNCHER_HIDE_DIVIDER));
            } else if (bundle.containsKey(Constants.LAUNCHER_SHOW_STATUS_BAR)) {
                config.setStatusBarShowStatus(bundle.getBoolean(Constants.LAUNCHER_SHOW_STATUS_BAR));
                toggleStatusBar();
            } else if (bundle.containsKey(Constants.LAUNCHER_SHOW_CUSTOM_ICON)) {
                config.setCustomIconShowStatus(bundle.getBoolean(Constants.LAUNCHER_SHOW_CUSTOM_ICON));
                launcherView.refreshReplaceIcon();
            } else if (bundle.containsKey(Constants.APP_NAME_LINES_KEY)) {
                int lines = bundle.getInt(Constants.APP_NAME_LINES_KEY);
                if (lines == 3) lines = Integer.MAX_VALUE;
                config.setAppNameLines(lines);
                launcherView.updateAppNameLines();
            }
        }
    }

    BroadcastReceiver appChangeReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            appCenter.refreshAppList(launcherView.isDelete());
        }
    };

    BroadcastReceiver batteryLevelRcvr = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            int rawlevel = intent.getIntExtra("level", -1);
            int scale = intent.getIntExtra("scale", -1);
            int status = intent.getIntExtra("status", -1);
            int health = intent.getIntExtra("health", -1);
            int level = -1; // percentage, or -1 for unknown
            if (rawlevel >= 0 && scale > 0) {
                level = (rawlevel * 100) / scale;
            }
            // batteryProgress.setProgress(level);
            tvBattery.setText(level + "");

            batteryStatus.setVisibility(View.VISIBLE);
            if (BatteryManager.BATTERY_HEALTH_OVERHEAT == health) {
                batteryStatus.setText(R.string.battery_heat);
            } else {
                switch (status) {
                    case BatteryManager.BATTERY_STATUS_UNKNOWN:
                        batteryStatus.setText(R.string.battery_unknown);
                        break;
                    case BatteryManager.BATTERY_STATUS_CHARGING:
                        batteryStatus.setText(R.string.battery_charging);
                        break;
                    case BatteryManager.BATTERY_STATUS_DISCHARGING:
                    case BatteryManager.BATTERY_STATUS_NOT_CHARGING:
                        if (level < 15) {
                            batteryStatus.setText(R.string.battery_low);
                        } else {
                            batteryStatus.setVisibility(View.GONE);
                        }
                        break;
                    case BatteryManager.BATTERY_STATUS_FULL:
                        batteryStatus.setText(R.string.battery_full);
                        break;
                    default:
                        batteryStatus.setText(R.string.battery_wtf);
                        break;
                }
            }

        }
    };

    private void detectionUSB() {
        IntentFilter usbFilter = new IntentFilter();
        usbFilter.addAction(Intent.ACTION_UMS_DISCONNECTED);
        usbFilter.addAction(Intent.ACTION_MEDIA_MOUNTED);
        usbFilter.addAction(Intent.ACTION_MEDIA_UNMOUNTED);
        usbFilter.addAction(Intent.ACTION_MEDIA_REMOVED);
        usbFilter.addDataScheme("file");
        registerReceiver(usbReceiver, usbFilter);
    }

    private BroadcastReceiver usbReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equals(Intent.ACTION_MEDIA_REMOVED)
                    || action.equals(Intent.ACTION_MEDIA_UNMOUNTED)) {
                //设备卸载成功;
            } else if (action.equals(Intent.ACTION_MEDIA_MOUNTED)) {
                //设备挂载成功
                launcherView.refreshReplaceIcon();
            }
        }
    };

    @Override
    public void onBackPressed() {
        if (getFragmentManager().getBackStackEntryCount() > 0) {
            super.onBackPressed();
            config.saveFontSize();
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK && getFragmentManager().getBackStackEntryCount() == 0) {
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    public void lockScreen() {
        try {
            if (policyManager.isAdminActive(new ComponentName(this, AdminReceiver.class))) {
                policyManager.lockNow();
            } else {
                activeManage();
            }
        } catch (Exception e) {
            openDevicePolicyManager();
        }
    }

    private void openDevicePolicyManager() {
        new AlertDialog.Builder(this)
                .setTitle(R.string.launch_failed)
                .setMessage(R.string.launch_devicemanager_failed)
                .setPositiveButton(R.string.launch_devicemanager, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        try {
                            Intent intent = Intent.parseUri("intent:#Intent;component=com.android.settings/.DeviceAdminSettings;end", Intent.URI_INTENT_SCHEME);
                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            startActivity(intent);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                })
                .setNegativeButton(R.string.dialog_cancel, null).show();
    }

    private void activeManage() {
        Intent intent = new Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN);
        intent.putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, new ComponentName(this, AdminReceiver.class));
        intent.putExtra(DevicePolicyManager.EXTRA_ADD_EXPLANATION, "E-Ink Launcher 获取锁屏权限");
        startActivity(intent);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case 10001:
                    policyManager.lockNow();
                    break;
            }
        }
    }

    public boolean isSystemApp(PackageInfo pInfo) {
        return ((pInfo.applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) != 0);
    }

    public boolean isSystemUpdateApp(PackageInfo pInfo) {
        return ((pInfo.applicationInfo.flags & ApplicationInfo.FLAG_UPDATED_SYSTEM_APP) != 0);
    }

    public boolean isUserApp(PackageInfo pInfo) {
        return (!isSystemApp(pInfo) && !isSystemUpdateApp(pInfo));
    }

    public void toggleStatusBar() {
        int windowFlags = WindowManager.LayoutParams.FLAG_FULLSCREEN;
        if (Config.showStatusBar) {
            getWindow().setFlags(windowFlags, windowFlags);
        } else {
            getWindow().clearFlags(windowFlags);
        }
    }

    private void checkLaunchHomeNotification() {
        if (!TextUtils.equals(Build.DEVICE, "virgo-perf1")) {
            return;
        }
        Intent service = new Intent(this, HomeEntranceService.class);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(service);
        } else {
            startService(service);
        }
    }
}
