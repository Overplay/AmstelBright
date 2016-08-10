package io.ourglass.amstelbright2.core;

/**
 * Created by mkahn on 5/17/16.
 */
public class OGConstants {

    public static final boolean TEST_MODE = true;
    public static final boolean SHOW_DB_TOASTS = true;

    public static final boolean USE_HTTPS = true;
    public static final String SSL_KEY_PASSWORD = "password";
    public static final String SSL_KEYSTORE = "src/main/resources/keystore2.jks";
    /**
     * Networking constants
     */

    public static final int HTTP_PORT = 9090;
    public static final int UDP_BEACON_PORT = 9191;
    public static final int UDP_BEACON_FREQ = 2000;

    public static final int CLOUD_SCRAPE_INTERVAL = 1000*60;
    public static final int TV_POLL_INTERVAL = 2500;


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

    // HARD CODED for DEMO
    public static final String STB_ENDPOINT = "http://10.1.10.38:8080/tv/getTuned";


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
