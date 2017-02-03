package io.ourglass.amstelbright2.services.http.handlers;

import android.util.Log;

import org.json.JSONObject;

import java.io.IOException;
import java.util.Map;

import io.ourglass.amstelbright2.core.ABApplication;
import io.ourglass.amstelbright2.core.OGConstants;
import io.ourglass.amstelbright2.services.http.NanoHTTPBase.NanoHTTPD;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import static io.ourglass.amstelbright2.services.http.NanoHTTPBase.NanoHTTPD.Method.POST;

/**
 * Created by mkahn on 5/9/16.
 */
public class JSONSpamHandler extends JSONHandler {

    public static final String TAG = "JSONSpamHandler";

    public String getText(Map<String, String> urlParams, NanoHTTPD.IHTTPSession session) {

        if (session.getMethod() != POST){
            responseStatus = NanoHTTPD.Response.Status.NOT_ACCEPTABLE;
            return makeErrorJson("That ain't gonna work for me, jack.");
        }

        JSONObject putJSON = null;

        try {

            putJSON = getBodyAsJSONObject(session);
            String apikey = putJSON.optString("apikey");
            if (apikey==null){
                responseStatus = NanoHTTPD.Response.Status.UNAUTHORIZED;
                return makeErrorJson("Security FAIL, bitch!");
            }

            String to = putJSON.optString("to");
            if (to==null){
                responseStatus = NanoHTTPD.Response.Status.NOT_ACCEPTABLE;
                return makeErrorJson("So where's you want that to go?");
            }

            String emailbody = putJSON.optString("emailbody");
            if (emailbody==null){
                responseStatus = NanoHTTPD.Response.Status.NOT_ACCEPTABLE;
                return makeErrorJson("Got nothing to say, huh?");
            }


        } catch (Exception e) {
            responseStatus = NanoHTTPD.Response.Status.INTERNAL_ERROR;
            return makeErrorJson(e);
        }

        final JSONObject finalPutJSON = putJSON;
        final OkHttpClient client = ABApplication.okclient;  // share it

        Runnable uploadRunnable = new Runnable() {
            @Override
            public void run() {

                String postBody = finalPutJSON.toString();

                RequestBody body = RequestBody.create(MediaType.parse("application/json"), postBody);
                Request request = new Request.Builder()
                        .url(OGConstants.ASAHI_ADDRESS + "/sendMail/generic")
                        .put(body)
                        .build();

                try {
                    Response response = client.newCall(request).execute();
                    if(!response.isSuccessful()) throw new IOException("Unexpected code " + response);

                    Log.v(TAG, "successfully uploaded email to Asahi");

                } catch (Exception e){
                    Log.w(TAG, "there was an error uploading email (" + e.getMessage() + ")");
                }
            }
        };

        Thread t = new Thread(uploadRunnable);
        t.start();

        responseStatus = NanoHTTPD.Response.Status.OK;
        return "{ \"message\": \"Giving it a shot!\" }";

    }


}
