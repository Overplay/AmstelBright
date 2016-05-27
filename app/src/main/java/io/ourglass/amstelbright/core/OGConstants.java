package io.ourglass.amstelbright.core;

/**
 * Created by mkahn on 5/17/16.
 */
public class OGConstants {

    /**
     * Networking constants
     */

    public static final int HTTP_PORT = 9090;
    public static final int UDP_BEACON_PORT = 9191;

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

}
