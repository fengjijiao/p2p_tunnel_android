package us.syh.april;

import android.app.Application;

import com.qmuiteam.qmui.arch.QMUISwipeBackActivityManager;

public class AprilApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        QMUISwipeBackActivityManager.init(this);
    }
}
