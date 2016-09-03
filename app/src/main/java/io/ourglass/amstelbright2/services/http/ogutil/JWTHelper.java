package io.ourglass.amstelbright2.services.http.ogutil;

import android.util.Log;

import java.security.Key;
import java.util.Date;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.SignatureException;
import io.jsonwebtoken.impl.crypto.MacProvider;
import io.ourglass.amstelbright2.core.OGConstants;

/**
 * Created by ethan on 8/5/16.
 * singleton to manage JWT creation and verification
 */
public class JWTHelper {
    private static final String TAG = "JWTHelper";
    private static JWTHelper instance = null;

    private Key jwtKey;

    //todo replace with OGConstants key (suggested by tutorial)
    protected JWTHelper(){
        jwtKey = MacProvider.generateKey();
    }

    public static JWTHelper getInstance(){
        if(instance == null){
            instance = new JWTHelper();
        }
        return instance;
    }

    //todo add parameters to check before dishing out token
    public String generateToken(){
        long curTime = System.currentTimeMillis();
        Date curDate = new Date(curTime), deathDate = new Date(curTime + OGConstants.JWT_LIFESPAN);

        String compactJWS = Jwts.builder()
                .setIssuedAt(curDate)
                .setExpiration(deathDate)
                .signWith(SignatureAlgorithm.HS512, this.jwtKey)
                .compact();

        return compactJWS;
    }


    public boolean checkJWT(String compactJWT, OGConstants.AUTH_LEVEL desiredLevel){
        //add some way to check for different levels of authorization
        try {
            //this should automatically check if expired as well as being valid
            Jwts.parser().setSigningKey(jwtKey).parseClaimsJws(compactJWT);

            return true;
        } catch (SignatureException e){
            Log.v(TAG, e.getMessage());
            return false;
        }
    }

}
