package io.ourglass.amstelbright2.services.stbservice;

/**
 * Created by mkahn on 11/11/16.
 */

public abstract class SetTopBox {

    public String ipAddress = "";
    public STBConnectionType connectionType = STBConnectionType.IPGENERIC;
    public STBCarrier carrier = STBCarrier.DIRECTV;
    public String modelName = "";
    public TVShow nowPlaying;
    public String receiverId = "";
    public long lastUpdated = 0;
    SetTopBoxListener mListener;

    public static final Object syncLock = new Object();

    // We'll probably use this somewhere down the line
    public enum STBConnectionType {
        WIFI, ETHERNET, MOCA, IPGENERIC
    }

    public enum STBCarrier {
        DIRECTV, XFINITY
    }

    public interface SetTopBoxListener {
        public void newWhatsOnData(TVShow newShow);
        //public void newGuideData(JSONObject newChannelData);
    }

    public SetTopBox(){

    }

    public SetTopBox(SetTopBoxListener listener, String ipAddress, STBCarrier carrier, STBConnectionType connectionType, String modelName ){

        this.ipAddress = ipAddress;
        this.connectionType = connectionType;
        this.carrier = carrier;
        this.modelName = modelName;

    }

    // These will be box specific (DirecTV, Xfinity, etc.)
    public abstract void updateWhatsOn();
    public abstract TVShow updateWhatsOnSync();
    public abstract SetTopBox updateAllSync();
    //public abstract void updateGuide();

    public void updateInBackground(){
        // TODO: think about Synclock on these objects. Do we need it? Seems like if we lock here, we
        // should lock everywhere, no? MAK
        synchronized (syncLock){
            Thread t = new Thread(new Runnable() {
                @Override
                public void run() {
                    updateAllSync();
                }
            });
            t.start();
        }
    }

}
