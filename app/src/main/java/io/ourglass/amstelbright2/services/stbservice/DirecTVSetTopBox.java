package io.ourglass.amstelbright2.services.stbservice;

import android.util.Log;

import org.json.JSONObject;

/**
 * Created by mkahn on 11/11/16.
 */

public class DirecTVSetTopBox extends SetTopBox {

    private final static String TAG = "DirecTVSetTopBox";

    public String ssdpResponse = "";
    public String upnpInfoUrl = "";


    public DirecTVSetTopBox(SetTopBoxListener listener, String ipAddress, STBConnectionType connectionType, String ssdpResponse ){
        super(listener, ipAddress, STBCarrier.DIRECTV, connectionType, "");
        this.ssdpResponse = ssdpResponse;
        extractUpnpUrl();
    }

    private void extractUpnpUrl(){

        //TODO: This extraction should be done with REGEX
        try {
            int locIdx = ssdpResponse.indexOf("Location:");
            String s1 = ssdpResponse.substring(locIdx+"Location:".length());
            int eol = s1.indexOf("\n");
            String url1 = s1.substring(0, eol);
            String url = url1.trim();
            this.upnpInfoUrl = url;

        } catch (Exception e){
            Log.d(TAG, "There was a problem parsing the SSDP payload info, probably pairing sequence issue, not a biggie.");
            this.upnpInfoUrl = null;
        }


    }

    @Override
    public void updateWhatsOn() {

        final String ipAddr = this.ipAddress;

        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                whatsOn();
            }
        });

        t.start();

    }


    @Override
    public TVShow updateWhatsOnSync(){

        return whatsOn();

    }

    private TVShow whatsOn(){

        JSONObject onobj = DirecTVAPI.whatsOn(this.ipAddress);

        if (onobj==null){
            // error situation
            return null;
        }

        TVShow show = new TVShow();
        String fallback = "???";

        show.networkName = onobj.optString("callsign", fallback);
        show.title = onobj.optString("title", fallback);
        show.episodeTitle = onobj.optString("episodeTitle", fallback);
        show.channelNumber = onobj.optString("major", fallback);
        //show.uniqueId = onobj.getString("uniqueId");
        show.programId = onobj.optString("programId", fallback);
        // Assign to parent
        nowPlaying = show;
        lastUpdated = System.currentTimeMillis();

        return show;
    }

    private void getModelInfo(){

        this.modelName = DirecTVAPI.modelInfo(upnpInfoUrl);
        this.receiverId = DirecTVAPI.receiverId(this.ipAddress);
        lastUpdated = System.currentTimeMillis();
    }

    @Override
    public DirecTVSetTopBox updateAllSync(){
        whatsOn();
        getModelInfo();
        lastUpdated = System.currentTimeMillis();
        return this;
    }

}

/*

HTTP/1.1 200 OK
Cache-Control: max-age=1800
EXT:
Location: http://10.1.10.118:49152/2/description.xml
Server: Linux/2.6.18.5, UPnP/1.0 DIRECTV JHUPnP/1.0
ST: upnp:rootdevice
USN: uuid:29bbe0e1-1a6e-47f6-8f8d-0003784ebf0c::upnp:rootdevice


 */