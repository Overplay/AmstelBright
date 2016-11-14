package io.ourglass.amstelbright2.services.stbservice;

import org.json.JSONObject;

/**
 * Created by mkahn on 11/11/16.
 */

public abstract class SetTopBox {

    String mIPAddress;
    STBConnectionType mConnectionType;
    STBCarrier mCarrier;
    String mFriendlyName;
    SetTopBoxListener mListener;


    // We'll probably use this somewhere down the line
    public enum STBConnectionType {
        WIFI, ETHERNET, MOCA, IPGENERIC
    }

    public enum STBCarrier {
        DIRECTV, XFINITY
    }

    public interface SetTopBoxListener {
        public void newWhatsOnData(JSONObject newChannelData);
        public void newGuideData(JSONObject newChannelData);
    }

    public SetTopBox(){

    }

    public SetTopBox(SetTopBoxListener listener, String ipAddress, STBCarrier carrier, STBConnectionType connectionType ){

        mIPAddress = ipAddress;
        mConnectionType = connectionType;
        mCarrier = carrier;
    }

    // These will be box specific (DirecTV, Xfinity, etc.)
    public abstract void updateWhatsOn();
    public abstract void updateGuide();


}
