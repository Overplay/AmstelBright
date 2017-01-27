package io.ourglass.amstelbright2.services.http.handlers;

import java.util.Map;

import io.ourglass.amstelbright2.core.OGConstants;
import io.ourglass.amstelbright2.realm.OGTVStation;
import io.ourglass.amstelbright2.services.http.NanoHTTPBase.NanoHTTPD;
import io.ourglass.amstelbright2.services.http.ogutil.JWTHelper;
import io.realm.Realm;


/**
 * Created by mkahn on 5/9/16.
 */
public class JSONChannelFavoriteHandler extends JSONHandler {

    private void markChannelFavorite(Realm realm, final OGTVStation station, final boolean isFav) {

        realm.executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                station.favorite = isFav;
            }
        });

    }

    public String getText(Map<String, String> urlParams, NanoHTTPD.IHTTPSession session) {

        if (session.getMethod() != NanoHTTPD.Method.POST) {
            responseStatus = NanoHTTPD.Response.Status.BAD_REQUEST;
            return makeErrorJson("Bad Verb");
        }
        //these operations require owner level permissions
        String tok = session.getHeaders().get("authorization");
        if (!OGConstants.USE_JWT && (tok == null || !JWTHelper.getInstance().checkJWT(tok, OGConstants.AUTH_LEVEL.OWNER))) {
            responseStatus = NanoHTTPD.Response.Status.UNAUTHORIZED;
            return makeErrorJson("Missing JWT or not Authorized");
        }

        int channel = Integer.parseInt(urlParams.get("channel"));

        Realm realm = Realm.getDefaultInstance();
        final OGTVStation station = OGTVStation.getByChannelNumber(realm, channel);

        if (station == null) {
            responseStatus = NanoHTTPD.Response.Status.NOT_ACCEPTABLE;
            realm.close();
            return makeErrorJson("No such channel");
        }

        Boolean shouldSetFav = session.getParms().get("clear") == null;

        markChannelFavorite(realm, station, shouldSetFav);

        responseStatus = NanoHTTPD.Response.Status.OK;
        String rval = station.toJson().toString();
        realm.close();
        return rval;
    }


}

