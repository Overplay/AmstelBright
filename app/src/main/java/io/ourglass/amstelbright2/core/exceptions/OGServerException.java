package io.ourglass.amstelbright2.core.exceptions;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by mkahn on 5/9/16.
 */
public class OGServerException extends Exception {

    public enum ErrorType { NO_SUCH_APP, UNKNOWN, APP_NOT_RUNNING }
    public ErrorType type = ErrorType.UNKNOWN;;

    public OGServerException(String s) {
        super(s);
    }

    public OGServerException ofType(ErrorType errorType){
        type = errorType;
        return this;
    }

    public String toJson(){
        JSONObject jobj = new JSONObject();
        try {
            jobj.put("reason", this.getMessage());
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return jobj.toString();
    }
}
