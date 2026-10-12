package cn.modificator.launcher;

import android.app.Application;

import cn.modificator.launcher.activity.LauncherActivity;
import cn.modificator.launcher.common.CrashCapture;

public class App extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        CrashCapture.getInstance().init(this, 1, LauncherActivity.class);
    }
}
