package io.ourglass.amstelbright2.services.http.handlers;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import io.ourglass.amstelbright2.core.exceptions.OGServerException;
import io.ourglass.amstelbright2.services.http.NanoHTTPBase.NanoHTTPD;
import io.ourglass.amstelbright2.services.http.OGRouterNanoHTTPD;

/**
 * Created by mkahn on 5/18/16.
 */
public abstract class JSONHandler extends OGRouterNanoHTTPD.DefaultHandler {

    protected JSONObject getBodyAsJSONObject(NanoHTTPD.IHTTPSession session) throws IOException, NanoHTTPD.ResponseException, JSONException {

        //parse the body of the request
        // TODO this does not work for PUT which makes me think it is not the right way to do it
        Map<String, String> files = new HashMap<String, String>();
        session.parseBody(files);
        String json = files.get("postData");
        return new JSONObject(json);

    }

    protected String makeErrorJson(Exception e) {

        return "{ \"error\":\"" + e.toString() + "\"}";
    }

    protected String makeErrorJson(String eString) {

        return "{ \"error\":\"" + eString + "\"}";
    }

    protected String processOGException(OGServerException e) {

        switch (e.type){

            case NO_SUCH_APP:

                responseStatus = NanoHTTPD.Response.Status.NOT_FOUND;
                break;

            case APP_NOT_RUNNING:
                responseStatus = NanoHTTPD.Response.Status.NOT_ACCEPTABLE;
                break;

            case UNKNOWN:
            default:

                responseStatus = NanoHTTPD.Response.Status.INTERNAL_ERROR;

        }

        return (e.toJson());


    }

    protected NanoHTTPD.Response.IStatus responseStatus;


    @Override
    public String getText() {
        return "not implemented";
    }

    @Override
    public String getMimeType() {
        return "application/json";
    }

    @Override
    public NanoHTTPD.Response.IStatus getStatus() {

        return responseStatus;
    }

    public abstract String getText(Map<String, String> urlParams, NanoHTTPD.IHTTPSession session);

    public NanoHTTPD.Response get(OGRouterNanoHTTPD.UriResource uriResource, Map<String, String> urlParams, NanoHTTPD.IHTTPSession session) {

        String text = getText(urlParams, session);
        ByteArrayInputStream inp = new ByteArrayInputStream(text.getBytes());
        int size = text.getBytes().length;
        // If you want to add to the response header, you can do that because this method
        // returns a standard response object. See:
        // http://stackoverflow.com/questions/25361457/how-to-send-file-name-with-nanohttpd-response
        return NanoHTTPD.newFixedLengthResponse(getStatus(), getMimeType(), inp, size);
    }

    public boolean JWTPresent(NanoHTTPD.IHTTPSession session){
        Map<String, String> headers = session.getHeaders();
        return headers.containsKey("authorization") && headers.get("authorization").contains("JWT");
    }
}
