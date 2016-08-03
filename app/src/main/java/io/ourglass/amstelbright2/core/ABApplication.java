package io.ourglass.amstelbright2.core;

import android.app.Application;
import android.content.Context;
import android.util.Log;
import android.widget.Toast;

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
        Log.d("ABAPP", "Loading AB application");

        RealmConfiguration config = new RealmConfiguration.Builder(this)
                .name("ab.realm")
                .schemaVersion(1)
                .build();

        Realm.setDefaultConfiguration(config);
    }

    public static void dbToast(Context context, String message){
        if (OGConstants.SHOW_DB_TOASTS){
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
        }
    }


}
