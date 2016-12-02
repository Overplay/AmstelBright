package io.ourglass.amstelbright2.core;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by mkahn on 12/1/16.
 */

public class JSONHTTPResponse {

    public JSONObject jsonResponseObject;
    public String stringResponse;
    public int responseCode;
    public boolean isGoodJSON;
    public boolean isGoodResponse;

    public JSONHTTPResponse(String stringResponse, int responseCode){
        this.stringResponse = stringResponse;
        this.responseCode = responseCode;

        this.isGoodResponse = this.responseCode>=200 && this.responseCode<300;
        try {
            this.jsonResponseObject = new JSONObject(this.stringResponse);
            this.isGoodJSON = true;
        } catch (JSONException e) {
            this.jsonResponseObject = null;
            this.isGoodJSON = false;
        }
    }

}
