package io.ourglass.amstelbright2.tvui;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * Created by mkahn on 5/10/16.
 */

// TODO having two so similar classes is wonky and not DRY, fix it. Maybe just pass a cb instead of interface

public class OGBroadcastStatusReceiver extends BroadcastReceiver {

    private OGBroadcastReceiverListener mListener;

    public interface OGBroadcastReceiverListener {

        public void receivedStatus(Intent intent);

    }

    public OGBroadcastStatusReceiver(OGBroadcastReceiverListener listener){
        mListener = listener;
    }

    @Override
    public void onReceive(Context context, Intent intent) {

        if (mListener!=null){
            mListener.receivedStatus(intent);
        }

    }

}
