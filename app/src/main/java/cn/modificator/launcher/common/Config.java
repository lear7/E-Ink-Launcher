package cn.modificator.launcher.common;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.HashSet;
import java.util.Set;

/**
 * Created by mod on 16-4-23.
 */
public class Config {
    Context context;
    //列数
    public static int colNum = -1;
    //行数
    public static int rowNum = -1;
    public static int sortType = -1;
    public static float fontSize = -1;
    public static int appNameLines = 1;
    public static boolean hideDivider = false;
    public static boolean showStatusBar = false;
    public static boolean showCustomIcon = false;
    private String preferencesFileName = "launcherPropertyFile";
    private Set<String> hideApps = new HashSet<>();

    public Config(Context context) {
        this.context = context;
        getCustomIconShowStatus();
        getDividerHideStatus();
        getStatusBarShowStatus();
        getAppNameLines();
    }

    public int getSortType() {
        if (sortType == -1) {
            SharedPreferences preferences = context.getSharedPreferences(preferencesFileName, Context.MODE_PRIVATE);
            sortType = preferences.getInt(Constants.SORT_TYPE_KEY, 0);
        }
        return sortType;
    }

    public void setSortType(int sortType) {
        if (this.sortType == sortType)
            return;
        this.sortType = sortType;
        SharedPreferences preferences = context.getSharedPreferences(preferencesFileName, Context.MODE_PRIVATE);
        preferences.edit().putInt(Constants.SORT_TYPE_KEY, sortType).apply();
    }

    public int getColNum() {
        if (colNum == -1) {
            SharedPreferences preferences = context.getSharedPreferences(preferencesFileName, Context.MODE_PRIVATE);
            colNum = preferences.getInt(Constants.COL_NUM_KEY, 5);
        }
        return colNum;
    }

    public void setColNum(int colNum) {
        if (this.colNum == colNum)
            return;
        this.colNum = colNum;
        SharedPreferences preferences = context.getSharedPreferences(preferencesFileName, Context.MODE_PRIVATE);
        preferences.edit().putInt(Constants.COL_NUM_KEY, colNum).apply();
    }

    public int getRowNum() {
        if (rowNum == -1) {
            SharedPreferences preferences = context.getSharedPreferences(preferencesFileName, Context.MODE_PRIVATE);
            rowNum = preferences.getInt(Constants.ROW_NUM_KEY, 5);
        }
        return rowNum;
    }

    public void setRowNum(int rowNum) {
        if (this.rowNum == rowNum)
            return;
        this.rowNum = rowNum;
        SharedPreferences preferences = context.getSharedPreferences(preferencesFileName, Context.MODE_PRIVATE);
        preferences.edit().putInt(Constants.ROW_NUM_KEY, rowNum).apply();
    }

    public void addHideApp(String packageName) {
        hideApps.add(packageName);
        SharedPreferences preferences = context.getSharedPreferences(preferencesFileName, Context.MODE_PRIVATE);
        preferences.edit().putStringSet(Constants.HIDE_APPS_KEY, hideApps).apply();
    }

    public void removeHideApp(String packageName) {
        hideApps.remove(packageName);
        SharedPreferences preferences = context.getSharedPreferences(preferencesFileName, Context.MODE_PRIVATE);
        preferences.edit().putStringSet(Constants.HIDE_APPS_KEY, hideApps).apply();
    }

    public void setHideApps(Set<String> hideApps) {
        this.hideApps.clear();
        this.hideApps.addAll(hideApps);
        SharedPreferences preferences = context.getSharedPreferences(preferencesFileName, Context.MODE_PRIVATE);
        preferences.edit().putStringSet(Constants.HIDE_APPS_KEY, this.hideApps).apply();
    }

    public Set<String> getHideApps() {
        if (hideApps.isEmpty()) {
            hideApps.addAll(context.getSharedPreferences(preferencesFileName, Context.MODE_PRIVATE).getStringSet(Constants.HIDE_APPS_KEY, new HashSet<String>()));
        }
        return hideApps;
    }

    public float getFontSize() {
        if (fontSize == -1) {
            fontSize = context.getSharedPreferences(preferencesFileName, Context.MODE_PRIVATE).getFloat(Constants.LAUNCHER_FONT_SIZE, 14);
        }
        return fontSize;
    }

    public void saveFontSize() {
        SharedPreferences preferences = context.getSharedPreferences(preferencesFileName, Context.MODE_PRIVATE);
        preferences.edit().putFloat(Constants.LAUNCHER_FONT_SIZE, fontSize).apply();
    }

    public boolean getDividerHideStatus() {
        return hideDivider = context.getSharedPreferences(preferencesFileName, Context.MODE_PRIVATE).getBoolean(Constants.LAUNCHER_HIDE_DIVIDER, true);
    }

    public void setDividerHideStatus(boolean b) {
        hideDivider = b;
        SharedPreferences preferences = context.getSharedPreferences(preferencesFileName, Context.MODE_PRIVATE);
        preferences.edit().putBoolean(Constants.LAUNCHER_HIDE_DIVIDER, b).apply();
    }

    public boolean getStatusBarShowStatus() {
        return showStatusBar = context.getSharedPreferences(preferencesFileName, Context.MODE_PRIVATE).getBoolean(Constants.LAUNCHER_SHOW_STATUS_BAR, true);
    }

    public void setStatusBarShowStatus(boolean b) {
        showStatusBar = b;
        SharedPreferences preferences = context.getSharedPreferences(preferencesFileName, Context.MODE_PRIVATE);
        preferences.edit().putBoolean(Constants.LAUNCHER_SHOW_STATUS_BAR, b).apply();
    }

    public boolean getCustomIconShowStatus() {
        return showCustomIcon = context.getSharedPreferences(preferencesFileName, Context.MODE_PRIVATE).getBoolean(Constants.LAUNCHER_SHOW_CUSTOM_ICON, false);
    }

    public void setCustomIconShowStatus(boolean b) {
        showCustomIcon = b;
        SharedPreferences preferences = context.getSharedPreferences(preferencesFileName, Context.MODE_PRIVATE);
        preferences.edit().putBoolean(Constants.LAUNCHER_SHOW_CUSTOM_ICON, b).apply();
    }

    public void setAppNameLines(int lineNum) {
        appNameLines = lineNum;
        SharedPreferences preferences = context.getSharedPreferences(preferencesFileName, Context.MODE_PRIVATE);
        preferences.edit().putInt(Constants.APP_NAME_LINES_KEY, lineNum).apply();
    }

    public int getAppNameLines() {
        return appNameLines = context.getSharedPreferences(preferencesFileName, Context.MODE_PRIVATE).getInt(Constants.APP_NAME_LINES_KEY, Integer.MAX_VALUE);
    }
}
