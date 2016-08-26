package io.ourglass.amstelbright2.realm;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import io.realm.Realm;
import io.realm.RealmObject;
import io.realm.RealmResults;
import io.realm.annotations.PrimaryKey;

/**
 * Created by ethan on 8/24/16.
 */

public class OGAdvertisement extends RealmObject{
    String text1;
    String text2;
    String text3;

    public byte[] crawlerImg;
    public byte[] widgetImg;

    @PrimaryKey
    private String id;

    public OGAdvertisement(){

    }

    public OGAdvertisement(String id){
        this.id = id;
    }

    public String getId(){
        return id;
    }

    public JSONObject adAsJSON() throws JSONException{
        JSONObject toReturn = new JSONObject();

        JSONArray arr = new JSONArray();
        if(text1 != null) arr.put(text1);
        if(text2 != null) arr.put(text2);
        if(text3 != null) arr.put(text3);

        toReturn.put("textAds", arr);

        toReturn.put("crawlerImg", crawlerImg);
        toReturn.put("widgetImg", widgetImg);

        return toReturn;
    }

    public void setNextText(String toSet){
        if(toSet != null){
            if(text1 == null){
                text1 = toSet;
            }
            else if(text2 == null){
                text2 = toSet;
            }
            else if(text3 == null){
                text3 = toSet;
            }
        }
    }
}
