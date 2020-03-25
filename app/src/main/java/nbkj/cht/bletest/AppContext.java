package nbkj.cht.bletest;

import android.app.Application;

public class AppContext extends Application {

    private static AppContext instance;
    public static AppContext getInstance() {
        return instance;
    }
    @Override
    public void onCreate() {
        // TODO Auto-generated method stub
        super.onCreate();
        instance = this;
    }
}
