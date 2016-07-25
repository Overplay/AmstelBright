package io.ourglass.amstelbright2.realm;

import com.google.gson.Gson;

import io.realm.Realm;
import io.realm.RealmObject;
import io.realm.RealmResults;
import io.realm.annotations.PrimaryKey;


public class OGTVBestPosition extends RealmObject{

    @PrimaryKey
    public String callsign;
    public String programId;
    public String title;
    public int crawlerPosition;
    public int widgetPosition;
    public String clearRectJson;


    public static OGTVBestPosition getBestPosition(Realm realm, String callsign, String programId, String title){

       RealmResults<OGTVBestPosition> resultChannelPid = realm
                .where(OGTVBestPosition.class)
                .equalTo("callsign", callsign)
                .equalTo("programId", programId)
                .findAll();


        // Theoretically a great match...same channel, same PID
        if(resultChannelPid.size() > 0){
            return resultChannelPid.first();
        }

        // OK we missed above, let's try with title
        RealmResults<OGTVBestPosition> resultChannelTitle = realm
                .where(OGTVBestPosition.class)
                .equalTo("callsign", callsign)
                .equalTo("title", title)
                .findAll();

        // Theoretically a great match...same channel, same PID
        if(resultChannelTitle.size() > 0){
            return resultChannelTitle.first();
        }

        // Well that sucked, let's just try channel generic guess
        RealmResults<OGTVBestPosition> resultChannel = realm
                .where(OGTVBestPosition.class)
                .equalTo("callsign", callsign)
                .equalTo("programId", "")
                .equalTo("title", "")
                .findAll();


        if(resultChannel.size() > 0){
            return resultChannel.first();
        }

        return null;

    }

    public String toJson(){

        Gson gson = new Gson();
        return gson.toJson(this);

    }

}
