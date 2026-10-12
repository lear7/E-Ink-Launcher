package cn.modificator.launcher;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.ResolveInfo;
import android.os.Build;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import cn.modificator.launcher.widgets.EInkLauncherView;

/**
 * Created by mod on 16-4-22.
 */
public class AppCenter {
    Context mContext;
    private static List<ResolveInfo> mApps;
    int pageIndex = 0;
    int pageCount = 1;
    //列数
    int colNum = 5;
    //行数
    int rowNum = 5;
    // 排序类型
    int sortType = 0;
    EInkLauncherView launcherView;
    TextView pageStatus;
    private Set<String> hideApps = new HashSet<>();

    public static final String wifiPackageName = "E-ink_Launcher.WiFi";
    public static final String oneKeyLockPackageName = "E-ink_Launcher.Lock";

    public AppCenter(Context context) {
        this.mContext = context;
        mApps = new ArrayList<>();
    }

    public void setLauncherView(EInkLauncherView launcherView) {
        this.launcherView = launcherView;
        this.launcherView.setOnSingleAppHideChangeListener(new EInkLauncherView.OnSingleAppHideChange() {
            @Override
            public void change(String pkg) {
                refreshAppList();
            }
        });

        launcherView.setHideAppPkg(hideApps);
        setPageShow();
    }

    public void setPageStatus(TextView pageStatus) {
        this.pageStatus = pageStatus;
        pageStatus.setText((pageIndex + 1) + "/" + (pageCount + 1));
    }

    public void setHideApps(Set<String> hideApps) {
        this.hideApps.clear();
        this.hideApps.addAll(hideApps);
        loadApps();
    }

    public Set<String> getHideApps() {
        return hideApps;
    }

    private String getAppName(ResolveInfo resolveInfo) {
        CharSequence appName = resolveInfo.loadLabel(mContext.getPackageManager());
        String appNameResult = appName == null ? "" : appName.toString();
        return appNameResult;
    }

    private void loadApps() {
        Intent mainIntent = new Intent(Intent.ACTION_MAIN, null);
        mainIntent.addCategory(Intent.CATEGORY_LAUNCHER);
        if (launcherView != null) {
            hideApps.clear();
            hideApps.addAll(launcherView.getHideAppPkg());
        }
        mApps.clear();
        for (ResolveInfo singleApp : mContext.getPackageManager().queryIntentActivities(mainIntent, 0)) {
            if ("cn.modificator.launcher.Launcher".equals(singleApp.activityInfo.name)) continue;
            if (!hideApps.contains(singleApp.activityInfo.packageName)) {
                mApps.add(singleApp);
            }
        }

        // 排序
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            mApps.sort((resolveInfo, t1) -> sortType == 0 ? getAppName(resolveInfo).compareTo(getAppName(t1)) : -getAppName(resolveInfo).compareTo(getAppName(t1)));
        }

        if (!hideApps.contains(oneKeyLockPackageName))
            mApps.add(createPowerIcon());

        if (!hideApps.contains(wifiPackageName))
            mApps.add(createWifiIcon());

        updatePageCount();
    }

    private void loadAllApps() {
        Intent mainIntent = new Intent(Intent.ACTION_MAIN, null);
        mainIntent.addCategory(Intent.CATEGORY_LAUNCHER);
        mApps.clear();
        mApps.addAll(mContext.getPackageManager().queryIntentActivities(mainIntent, 0));
        mApps.add(createPowerIcon());
        mApps.add(createWifiIcon());
        launcherView.setHideAppPkg(hideApps);
        updatePageCount();
    }

    public void showNextPage() {
        if (pageIndex >= pageCount) return;
        pageIndex++;
        setPageShow();
    }

    public void showLastPage() {
        if (pageIndex <= 0) return;
        pageIndex--;
        setPageShow();
    }

    public void setColNum(int colNum) {
        this.colNum = colNum;
        updatePageCount();
        setPageShow();
    }

    public void setRowNum(int rowNum) {
        this.rowNum = rowNum;
        updatePageCount();
        setPageShow();
    }

    public void setSortType(int type) {
        this.sortType = type;
        updatePageCount();
        setPageShow();
    }

    public void refreshAppList() {
        refreshAppList(false);
    }

    public void refreshAppList(boolean showAll) {
        if (showAll) loadAllApps();
        else loadApps();
        setPageShow();
    }

    private void setPageShow() {
        int itemCount = colNum * rowNum;
        int pageStart = pageIndex * itemCount;
        int pageEnd = (pageStart + itemCount) > mApps.size() ? mApps.size() : (pageStart + itemCount);
        launcherView.setAppList(mApps.subList(pageStart, pageEnd));
        pageStatus.setText((pageIndex + 1) + "/" + (pageCount + 1));
    }

    private void updatePageCount() {
        pageCount = mApps.size() / (colNum * rowNum) - (mApps.size() % (colNum * rowNum) == 0 ? 1 : 0);
        pageCount = pageCount < 0 ? 0 : pageCount;
        pageIndex = pageIndex > pageCount ? pageCount : pageIndex;
    }

    private ResolveInfo createWifiIcon() {
        ResolveInfo resolveInfo = new ResolveInfo();
        resolveInfo.icon = R.drawable.wifi_on;
        resolveInfo.activityInfo = new ActivityInfo();
        resolveInfo.activityInfo.packageName = wifiPackageName;
        return resolveInfo;
    }

    private ResolveInfo createPowerIcon() {
        ResolveInfo resolveInfo = new ResolveInfo();
        resolveInfo.icon = R.drawable.ic_onekeylock;
        resolveInfo.activityInfo = new ActivityInfo();
        resolveInfo.activityInfo.packageName = oneKeyLockPackageName;
        return resolveInfo;
    }
}
