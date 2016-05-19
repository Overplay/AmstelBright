package io.ourglass.amstelbright.core;

import android.app.Application;

import io.realm.Realm;
import io.realm.RealmConfiguration;

/**
 * Created by mkahn on 5/18/16.
 */
public class ABApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        // The realm file will be located in Context.getFilesDir() with name "default.realm"
        RealmConfiguration config = new RealmConfiguration.Builder(this).build();
        Realm.setDefaultConfiguration(config);
    }

}
