package io.ourglass.amstelbright2.services.stbservice;

import android.util.Log;

import static io.ourglass.amstelbright2.services.ssdpservice.SSDPHandlerThread.TAG;

/**
 * Created by mkahn on 11/14/16.
 */

public class TVShow {

    // Setting ANY of these field to null can cause crashes, make sure they are not
    public String title = "";
    public String episodeTitle = "";
    public String networkName = "";
    public String channelNumber = "";
    //String uniqueId;
    public String programId = "";

    public boolean equals(TVShow show){

        boolean rval = false;
        // If any of these are null BOOM
        try {
            rval = this.title.equalsIgnoreCase(show.title) &&
                    this.networkName.equalsIgnoreCase(show.networkName) &&
                    this.channelNumber.equalsIgnoreCase(show.channelNumber);
        } catch (Exception e){
            Log.d(TAG, "Exception thrown checking show equality. Probably a null field. No bigggie smalls.");
        }

        return rval;

    }

}


