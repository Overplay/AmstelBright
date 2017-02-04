package io.ourglass.amstelbright2.services.http.handlers;

import android.util.Log;

import java.util.Map;

import io.ourglass.amstelbright2.core.ABApplication;
import io.ourglass.amstelbright2.services.http.NanoHTTPBase.NanoHTTPD;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import static io.ourglass.amstelbright2.services.http.NanoHTTPBase.NanoHTTPD.Method.GET;

/**
 * Created by mkahn on 5/9/16.
 */
public class JSONProxyAppDataHandler extends JSONHandler {

    public static final String TAG = "JSONProxyAppDataHandler";

    public String getText(Map<String, String> urlParams, NanoHTTPD.IHTTPSession session) {

        if (session.getMethod() != GET) {
            responseStatus = NanoHTTPD.Response.Status.NOT_ACCEPTABLE;
            return makeErrorJson("Only proxy GET supported right now");
        }

        String appId = urlParams.get("appid");
        String remoteHost = session.getParms().get("remote");

        if (remoteHost == null) {
            responseStatus = NanoHTTPD.Response.Status.NOT_ACCEPTABLE;
            return makeErrorJson("Need a remote host");
        }

        OkHttpClient mClient = ABApplication.okclient;

        try {

            Request req = new Request.Builder()
                    .url("http://"+remoteHost+":9090/api/appdata/"+appId)
                    .build();
            Response response = mClient.newCall(req).execute();

            String respString = response.body().string();

            if (response.isSuccessful()) {
                responseStatus = NanoHTTPD.Response.Status.OK;
                return respString;
            }

        } catch (Exception e) {
            Log.e(TAG, "Exception PROXY getting");
        }

        responseStatus = NanoHTTPD.Response.Status.INTERNAL_ERROR;
        return makeErrorJson("upstream error");

    }


}
