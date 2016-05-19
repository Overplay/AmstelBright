package io.ourglass.amstelbright.services.http.handlers;

import java.io.ByteArrayInputStream;
import java.util.Map;

import io.ourglass.amstelbright.core.exceptions.OGServerException;
import io.ourglass.amstelbright.services.http.NanoHTTPBase.NanoHTTPD;
import io.ourglass.amstelbright.services.http.OGRouterNanoHTTPD;

/**
 * Created by mkahn on 5/18/16.
 */
public abstract class JSONHandler extends OGRouterNanoHTTPD.DefaultHandler {

    protected String makeErrorJson(Exception e) {

        return "{ \"error\":\"" + e.toString() + "\"}";
    }

    protected String makeErrorJson(String eString) {

        return "{ \"error\":\"" + eString + "\"}";
    }

    protected String processOGException(OGServerException e) {
        if (e.type == OGServerException.ErrorType.NO_SUCH_APP) {
            responseStatus = NanoHTTPD.Response.Status.NOT_FOUND;
            return ("no data for that app");
        } else {
            e.printStackTrace();
            responseStatus = NanoHTTPD.Response.Status.INTERNAL_ERROR;
            // Should not get here
            return makeErrorJson(e);
        }
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

}
