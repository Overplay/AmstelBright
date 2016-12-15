package io.ourglass.amstelbright2.core;

import org.joda.time.DateTime;

import java.util.Date;

/**
 * Created by mkahn on 12/14/16.
 */

public class TimeHelpers {

    public static final long FOUR_HOURS_AS_MS = 1000*60*60*4;

    /**
     * Leverages Joda-Time to provide a UTC string suitable for API calls
     * @param offsetMinutes positive or negative minute offset from now
     * @return an ISO 8601 UTC string
     */
    public static String utcISOTimeStringWithOffset(int offsetMinutes){

        DateTime dtNow = DateTime.now();
        return dtNow.plusMinutes(offsetMinutes).toString();

    }

    public static Date currentDateWithTimeFlooredToLastHour(int offsetMinutes){

        DateTime dtNow = DateTime.now();
        DateTime dtFloor = dtNow.plusMinutes(offsetMinutes).withMinuteOfHour(0);
        return dtFloor.toDate();

    }

}
