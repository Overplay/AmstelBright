package io.ourglass.amstelbright2.services.stbservice;

/**
 * Created by mkahn on 11/11/16.
 */

public class DirecTVSetTopBox extends SetTopBox {

    private final static String TAG = "DirecTVSetTopBox";




    public DirecTVSetTopBox(SetTopBoxListener listener, String ipAddress, String friendlyName, STBConnectionType connectionType){
        super(listener, ipAddress, STBCarrier.DIRECTV, connectionType);
    }

    @Override
    public void updateWhatsOn() {

//
//
//        // Do callback, if there is an assigned listener
//        if (mListener!=null){
//            mListener.newWhatsOnData(rval);
//        }
//

    }

    @Override
    public void updateGuide() {
    }


}
