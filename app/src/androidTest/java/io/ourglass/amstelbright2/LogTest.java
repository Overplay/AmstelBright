package io.ourglass.amstelbright2;

import android.content.Intent;
import android.test.ActivityUnitTestCase;
import android.test.suitebuilder.annotation.LargeTest;

import org.json.JSONException;
import org.json.JSONObject;

import io.ourglass.amstelbright2.core.OGCore;
import io.ourglass.amstelbright2.realm.OGLog;
import io.ourglass.amstelbright2.services.stbservice.TVShow;
import io.ourglass.amstelbright2.tvui.MainframeActivity;
import io.realm.Realm;
import io.realm.RealmConfiguration;
import io.realm.RealmResults;

/**
 * Created by ethan on 12/29/16.
 * Fulfills requirement #1 of logging acceptance criteria
 */

public class LogTest extends ActivityUnitTestCase<MainframeActivity> {

    MainframeActivity mainframeActivity;

    public LogTest(Class<MainframeActivity> activityClass) {
        super(activityClass);
    }

    public LogTest() {
        super(MainframeActivity.class);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();

        //setup the testing realm database
        RealmConfiguration config = new RealmConfiguration.Builder(getInstrumentation().getTargetContext())
                .schemaVersion(1)
                .name("test.ab.realm")
                .deleteRealmIfMigrationNeeded()
                .build();

        Realm.setDefaultConfiguration(config);

        //create the intent to start the activity in test mode
        Intent testModeIntent = new Intent(getInstrumentation().getTargetContext(), MainframeActivity.class);
        testModeIntent.putExtra("testMode", true);
        //start the activity
        startActivity(testModeIntent, null, null);

        //get the reference to the activity
        mainframeActivity = getActivity();

        //clear out the test database
        Realm realm = Realm.getDefaultInstance();
        realm.beginTransaction();
        realm.deleteAll();
        realm.commitTransaction();
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
    }

    @LargeTest
    public void testChannelChangeLog() {
        Realm realm = Realm.getDefaultInstance();
        long count = realm.where(OGLog.class).count();

        assertD(count == 0);

        TVShow oldShow = new TVShow(), newShow = new TVShow();
        oldShow.title = "old";
        oldShow.episodeTitle = "oldEp";
        oldShow.networkName = "oldNet";
        oldShow.channelNumber = "oldNum";
        oldShow.programId = "oldProg";

        newShow.title = "new";
        newShow.episodeTitle = "newEp";
        newShow.networkName = "newNet";
        newShow.channelNumber = "newNum";
        newShow.programId = "newProg";

        OGCore.log_channelChange(oldShow, newShow);
        count = realm.where(OGLog.class).count();

        assertD(count == 1);

        RealmResults<OGLog> logs = realm.where(OGLog.class).findAll();
        OGLog ccLog = logs.get(0);

        try {
            JSONObject ccLogJSON = ccLog.getLogAsJSON();
            assertD(ccLogJSON.has("logType") && ccLogJSON.getString("logType").equals("channel"));
        } catch(JSONException e){
            realm.beginTransaction();
            logs.deleteAllFromRealm();
            realm.commitTransaction();
            fail();
        }

        count = realm.where(OGLog.class).count();

        assertD(count == 1);

        OGCore.log_alert(null, null);
        count = realm.where(OGLog.class).count();

        assertD(count == 2);

        logs = realm.where(OGLog.class).findAll();
        ccLog = logs.get(1);

        try {
            JSONObject ccLogJSON = ccLog.getLogAsJSON();
            assertD(ccLogJSON.has("logType") && ccLogJSON.getString("logType").equals("alert"));
        } catch(JSONException e){
            realm.beginTransaction();
            logs.deleteAllFromRealm();
            realm.commitTransaction();
            fail();
        }

        count = realm.where(OGLog.class).count();

        assertD(count == 2);

        OGCore.log_placementOverride("","","",0);
        count = realm.where(OGLog.class).count();

        assertD(count == 3);

        logs = realm.where(OGLog.class).findAll();
        ccLog = logs.get(2);

        try {
            JSONObject ccLogJSON = ccLog.getLogAsJSON();
            assertD(ccLogJSON.has("logType") && ccLogJSON.getString("logType").equals("placement"));
        } catch(JSONException e){
            realm.beginTransaction();
            logs.deleteAllFromRealm();
            realm.commitTransaction();
            fail();
        }


        count = realm.where(OGLog.class).count();

        assertD(count == 3);

        OGCore.log_heartbeat("","","");
        count = realm.where(OGLog.class).count();

        assertD(count == 4);

        logs = realm.where(OGLog.class).findAll();
        ccLog = logs.get(3);

        try {
            JSONObject ccLogJSON = ccLog.getLogAsJSON();
            assertD(ccLogJSON.has("logType") && ccLogJSON.getString("logType").equals("heartbeat"));
        } catch(JSONException e){
            realm.beginTransaction();
            logs.deleteAllFromRealm();
            realm.commitTransaction();
            fail();
        }

        realm.beginTransaction();
        logs.deleteAllFromRealm();
        realm.commitTransaction();
        assert(true);
    }

    /**
     * overriding the default assert method because android studio suggested a different convention
     * http://stackoverflow.com/questions/23113497/what-are-buildconfig-debug-conditional-checks
     */
    public void assertD(boolean toTest){
        if(BuildConfig.DEBUG && !toTest){
            throw new RuntimeException();
        }
    }
}
