package io.ourglass.amstelbright2.core;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;

/**
 * Created by mkahn on 6/23/16.
 */

// Just here for reference
class LooperThread extends Thread {
    public Handler mHandler;

    public void run() {
        Looper.prepare();

        mHandler = new Handler() {
            public void handleMessage(Message msg) {
                // process incoming messages here
            }
        };

        Looper.loop();
    }
}
