package io.ourglass.amstelbright2.core;

import android.graphics.Color;

import java.util.regex.Pattern;

/**
 * Created by mkahn on 5/17/16.
 */
public class OGConstants {

    // Set to true to use the most stable ASAHI server
    public static final boolean USE_DEMO_ASAHI = true;

    public static final boolean TEST_MODE = true;
    public static final boolean SHOW_DB_TOASTS = false;

    public static final boolean USE_HTTPS = false;
    public static final String SSL_KEY_PASSWORD = "password";
    public static final String SSL_KEYSTORE = "src/main/resources/keystore2.jks";
    /**
     * Networking constants
     */

    public static final int HTTP_PORT = 9090;

    // Turn off UPNP to work with the old iOS app
    public static final boolean USE_UPNP_DISCOVERY = true;

    public static final int UDP_BEACON_PORT = 9091;
    // MAK: Created a new port so both discovery methods can be used at same time
    public static final int UDP_LISTEN_AND_RESPOND_PORT = 9092;
    public static final int UDP_BEACON_FREQ = 2000;

    public static final int CLOUD_SCRAPE_INTERVAL = 1000*15;
    public static final int TV_POLL_INTERVAL = 2500;
    public static final int TV_DISCOVER_INTERVAL = 1000 * 60;

    public static final int HEARTBEAT_TIMER_INTERVAL = 1000 * 60 * 60; //1 hour

    public static final int UPNP_UDP_BROADCAST_PORT = 1900;
    public static final String UPNP_UDP_BROADCAST_ADDR = "239.255.255.250";
    public static final int STB_SERVICE_CHANNEL_POLL_INTERVAL = 2000; //every two seconds upon completion
    public static Pattern LOC_PATTERN = Pattern.compile("location[^\n]*", Pattern.CASE_INSENSITIVE);
    public static final String[] discoverPacket = new String[]{
            "M-SEARCH * HTTP/1.1\r\n",
            "Host:239.255.255.250:1900\r\n",
            "ST:ssdp:all\r\n","Man:\"ssdp:discover\"\r\n",
            "MX:3\r\n\r\n"
    };

    public static final String ASAHI_ADDRESS = USE_DEMO_ASAHI ? "http://107.170.209.248" : "http://104.131.145.36";
    public static final String ASAHI_API_ENDPOINT = "/api/v1/";
    public static final String ASAHI_ACCEPTED_AD_ENDPOINT = "/ad/getAccepted";
    public static final String ASAHI_MEDIA_ENDPOINT = "/media/download/";

    public static final String INTERNAL_PATH_TO_MEDIA = "/data/data/io.ourglass.amstelbright2/media/";
    public static final String EXTERNAL_PATH_TO_MEDIA = "/api/admedia/";

    // When using git, use the one below
    //public static final String PATH_TO_ABWL = "/Android/data/me.sheimi.sgit/files/repo/AmstelBrightLimeWWW";


    // When manually pushing
    // Keep separate from DEMO release so we can run both on the same H/W
    public static final String PATH_TO_ABWL = "/wwwaqui";



    public static enum BootState {
        ABS_START(0),
        UDP_START(1),
        UPGRADE_START(2),
        HTTP_START(3);

        private final int mState;

        private BootState(int state){
            mState = state;
        }

        public int getValue(){
            return mState;
        }
    }


    /**
     * Endpoints for STBs
     */

    public static final String DIRECTV_CHANNEL_GET_ENDPOINT = "/tv/getTuned";
    public static final int DIRECTV_PORT = 8080;
    public static final int DIRECTV_API_CONNECTION_TIMEOUT = 15000;
    public static final int DIRECTV_PAIR_CANCELED_RESULT_CODE = 0;
    public static final int DIRECTV_PAIR_CONFIRMED_RESULT_CODE = 1;

    public static final int DIRECTV_PAIR_ACTIVITY_BACKGROUND_GREEN = Color.parseColor("#51B85E");

    public static final int WIFI_MANAGE_ACTIVITY_BACKGROUND_ORANGE = Color.parseColor("#F6921E");

    public static final String[] WIFI_STRENGTH_LEVELS = new String[]{"None", "Poor", "Fair", "Good", "Excellent"};

    public static final int BUTTON_CLICK_DEBOUNCE = 2000;

    // HARD CODED for DEMO
    public static final int STB_PORT = 8080;
    public static final String STB_TUNED_ENDPOINT = "/tv/getTuned";

    /**
     * constants for JWT
     */

    public static boolean USE_JWT = true;
    public static final int JWT_LIFESPAN = 1 /*day*/ * 24 /*hours*/ * 60 /*minutes*/ * 60 /*seconds*/ * 1000 /*milliseconds*/;
    public enum AUTH_LEVEL {PATRON, OWNER, OG};

    public static final String TEST_DIRECT_TV_INFO =
                 "{\n" +
                "  \"callsign\": \"ESPNHD\",\n" +
                "  \"date\": \"20160603\",\n" +
                "  \"duration\": 14400,\n" +
                "  \"isOffAir\": false,\n" +
                "  \"isPclocked\": 3,\n" +
                "  \"isPpv\": false,\n" +
                "  \"isRecording\": false,\n" +
                "  \"isVod\": false,\n" +
                "  \"major\": 206,\n" +
                "  \"minor\": 65535,\n" +
                "  \"offset\": 652,\n" +
                "  \"programId\": \"36417953\",\n" +
                "  \"rating\": \"No Rating\",\n" +
                "  \"startTime\": 1464994800,\n" +
                "  \"stationId\": 2220255,\n" +
                "  \"status\": {\n" +
                "    \"code\": 200,\n" +
                "    \"commandResult\": 0,\n" +
                "    \"msg\": \"OK.\",\n" +
                "    \"query\": \"/tv/getTuned\"\n" +
                "  },\n" +
                "  \"title\": \"X Games\"\n" +
                "}";



}
