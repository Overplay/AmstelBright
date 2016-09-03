package io.ourglass.amstelbright2.services.http.handlers;

import android.nfc.Tag;
import android.util.Log;

import java.util.Map;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.impl.crypto.MacProvider;
import java.security.Key;


import io.ourglass.amstelbright2.services.http.NanoHTTPBase.NanoHTTPD;
import io.ourglass.amstelbright2.services.http.ogutil.JWTHelper;

/**
 * Created by ethan on 8/5/16.
 */
public class JSONJWTHandler extends JSONHandler {

    private static final String TAG = "JSONJWTHandler";

    @Override
    public String getText(Map<String, String> urlParams, NanoHTTPD.IHTTPSession session) {
        //TODO check asahi? (or something) here before issuing token

        switch(session.getMethod()){
            case GET:
                JWTHelper jwtHelper = JWTHelper.getInstance();
                String token = jwtHelper.generateToken();

                responseStatus = NanoHTTPD.Response.Status.OK;

                return token;
            default :
                responseStatus = NanoHTTPD.Response.Status.NOT_ACCEPTABLE;
                return "";
        }
    }
}
