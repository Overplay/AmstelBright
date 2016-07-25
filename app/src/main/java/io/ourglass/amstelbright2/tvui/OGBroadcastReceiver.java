package io.ourglass.amstelbright2.tvui;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * Created by mkahn on 5/10/16.
 */
public class OGBroadcastReceiver extends BroadcastReceiver {

    private OGBroadcastReceiverListener mListener;

    public interface OGBroadcastReceiverListener {

        void receivedCommand(Intent intent);

    }

    public OGBroadcastReceiver(OGBroadcastReceiverListener listener){
        mListener = listener;
    }

    @Override
    public void onReceive(Context context, Intent intent) {

        if (mListener!=null){
            mListener.receivedCommand(intent);
        }

        //Toast.makeText(context, command+" for app "+targetApp, Toast.LENGTH_SHORT).show();
    }

}
