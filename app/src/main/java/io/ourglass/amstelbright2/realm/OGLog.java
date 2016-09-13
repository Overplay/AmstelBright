package io.ourglass.amstelbright2.realm;

import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.UUID;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;
import io.realm.annotations.Required;

import static android.R.attr.required;

/**
 * Created by ethan on 8/23/16.
 */

public class OGLog extends RealmObject {

    @Required
    private String type;

    @Required
    private String data = "{}";

    @PrimaryKey
    private String uuid = UUID.randomUUID().toString();

    private Long createdAt = System.currentTimeMillis();

    private Long uploaded = -1L;

    public JSONObject getLogAsJSON(){
        try {
            JSONObject toReturn = new JSONObject();

            toReturn.put("logType", this.type.toLowerCase());
            toReturn.put("message", new JSONObject(data));
            toReturn.put("loggedAt", createdAt);
            toReturn.put("deviceUniqueId", uuid);

            return toReturn;
        } catch (JSONException e){
            return null;
        }
    }

    public void setType(String type){
        //can add some checking on type string before setting
        this.type = type;
    }

    public void setData(JSONObject obj){
        this.data = obj.toString();
    }

    public JSONObject getData(){
        try {
            return new JSONObject(data);
        } catch (JSONException e){
            return null;
        }
    }

    /**
     * note make sure you are in a transaction or else will explode
     * @param timeUploadedAt time the upload was completed
     */
    public void setUploaded(Long timeUploadedAt){
        if(timeUploadedAt < 0){
            Log.e("OGLog", "invalid uploaded time entered, ignoring");
            return;
        }
        //check if uploaded has already been set, should only be set once by log clean and push service
        if(this.uploaded != -1L){
            Log.e("OGLog", "uploaded has already been set, " +
                    "if this error is coming from LogCleanAndPushService, " +
                    "then there is probably something bad going on");
            return;
        }

        this.uploaded = timeUploadedAt;
    }

    public boolean isUploaded(){
        return this.uploaded != -1L;
    }
}