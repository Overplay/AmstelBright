package io.ourglass.amstelbright2.realm;

import org.json.JSONException;
import org.json.JSONObject;

import io.realm.RealmObject;
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

    Long createdAt = System.currentTimeMillis();

    public JSONObject getLogAsJSON(){
        try {
            JSONObject toReturn = new JSONObject();

            toReturn.put("type", this.type);
            toReturn.put("data", new JSONObject(data));
            toReturn.put("createdAt", createdAt);

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
}