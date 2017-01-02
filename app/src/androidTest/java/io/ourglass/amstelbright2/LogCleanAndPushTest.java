package io.ourglass.amstelbright2;

import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.test.ActivityTestCase;
import android.test.ActivityUnitTestCase;
import android.test.ApplicationTestCase;
import android.test.suitebuilder.annotation.LargeTest;
import android.util.Log;

import io.ourglass.amstelbright2.core.OGCore;
import io.ourglass.amstelbright2.realm.OGLog;
import io.ourglass.amstelbright2.services.applejack_comm.LogCleanAndPushService;
import io.ourglass.amstelbright2.services.stbservice.TVShow;
import io.ourglass.amstelbright2.tvui.Mainframe;
import io.ourglass.amstelbright2.tvui.MainframeActivity;
import io.ourglass.amstelbright2.tvui.Point;
import io.realm.Realm;
import io.realm.RealmConfiguration;
import io.realm.RealmResults;

/**
 * Created by ethan on 9/7/16.
 */

public class LogCleanAndPushTest extends ActivityUnitTestCase<MainframeActivity> {

    MainframeActivity mainframeActivity;

    public LogCleanAndPushTest(Class<MainframeActivity> activityClass) {
        super(activityClass);
    }

    public LogCleanAndPushTest(){
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

    private void createTestLogs(){
        //create 1 of each log type
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
        OGCore.log_alert(null, null);
        OGCore.log_placementOverride(null, null, null, 0);
        //OGCore.log_adImpression(null);
        OGCore.log_heartbeat(null, null, null);

    }

    @LargeTest
    public void testUpload(){
        Log.v("ordering", "upload has began");

        Realm realm = Realm.getDefaultInstance();

        //make sure there is nothing in the database
        RealmResults<OGLog> logs = realm.where(OGLog.class).findAll();

        assertEquals("There were already logs in the database, likely there is a service running that is creating them",
                0, logs.size());

        createTestLogs();

        logs = realm.where(OGLog.class).findAll();
        assertEquals("There was a problem creating OGLogs", 4, logs.size());

        //make sure that all logs are not already uploaded
        for(OGLog log : logs){
            if(log.isUploaded()){
                fail("Freshly inserted log should not already be uploaded");
            }
        }

        //upload the logs
        LogCleanAndPushService service = new LogCleanAndPushService();
        service.uploadLogs(Realm.getDefaultInstance(),logs);

        //check to see if these logs are now uploaded
        logs = realm.where(OGLog.class).findAll();

        for(OGLog log : logs){
            if(!log.isUploaded()){
                fail("Log upload failed");
            }
        }

        //clean up
        realm.beginTransaction();
        realm.deleteAll();
        realm.commitTransaction();
        realm.close();
        Log.v("ordering", "upload has ended");

    }

//    @LargeTest
//    public void testReapNotOld() throws Exception{
//        Log.v("ordering", "notOld has began");
//        Realm realm = Realm.getDefaultInstance();
//
//        //make sure there is nothing in the database
//        RealmResults<OGLog> logs = realm.where(OGLog.class).findAll();
//
//        assertEquals("There were already logs in the database, likely there is a service running that is creating them",
//                0, logs.size());
//
//        createTestLogs();
//
//        //ensure that logs were created
//        logs = realm.where(OGLog.class).findAll();
//        assertEquals("There was a problem creating OGLogs", 4, logs.size());
//
//        //set the time uploaded at shorter than one week (should not be reaped)
//        realm.beginTransaction();
//        for(OGLog log : logs){
//            log.setUploaded();
//        }
//        realm.commitTransaction();
//
//        LogCleanAndPushService service = new LogCleanAndPushService();
//        service.onStartCommand(null, 0, 0);
//
//        Thread.sleep(5000);
//        logs = realm.where(OGLog.class).findAll();
//        assertEquals("Not the expected amount of logs in Realm, possibly incorrectly reaped", 4, logs.size());
//
//        //clean up
//        realm.beginTransaction();
//        realm.deleteAll();
//        realm.commitTransaction();
//        realm.close();
//        service.onDestroy();
//
//        assert(true);
//        Log.v("ordering", "notOld has finished");
//
//    }
//
//    @LargeTest
//    public void testReapOld() throws Exception{
//        Log.v("ordering", "old has began");
//        Realm realm = Realm.getDefaultInstance();
//
//        //make sure there is nothing in the database
//        RealmResults<OGLog> logs = realm.where(OGLog.class).findAll();
//
//        assertEquals("There were already logs in the database, likely there is a service running that is creating them",
//                0, logs.size());
//
//        createTestLogs();
//
//        //ensure that logs were created
//        logs = realm.where(OGLog.class).findAll();
//        assertEquals("There was a problem creating OGLogs", 4, logs.size());
//
//        //todo change this when settings are implemented so that the uploaded time of the logs is longer than the set time
//        Long oldUploadTime = System.currentTimeMillis() - (1000 * 60 * 60 * 24 * 7) - (1000 * 60 * 30); //30 minutes older than a week old
//
//        //set the time uploaded at longer than a week (should be reaped)
//        realm.beginTransaction();
//        for(OGLog log : logs) {
//            log.setUploadedAt(oldUploadTime);
//        }
//        realm.commitTransaction();
//
//        //make sure that they are uploaded
//        for(OGLog log : logs)
//            if(!log.isUploaded())
//                fail("log upload failed");
//
//        LogCleanAndPushService service = new LogCleanAndPushService();
//        service.onStartCommand(null,0,0);
//        //sleep for 5 seconds to let the service work
//        Thread.sleep(5000);
//
//        logs = realm.where(OGLog.class).findAll();
//
////        assertEquals("Logs incorrectly identified as not old", 4, logs.size());
////
////        realm.beginTransaction();
////        service.reapLogs(logs);
////        realm.commitTransaction();
////
////        logs = realm.where(OGLog.class).findAll();
//
//        //these logs should have been reaped from the database
//        assertEquals("Old logs not successfully reaped", 0, logs.size());
//
//        //clean up
//        realm.beginTransaction();
//        realm.deleteAll();
//        realm.commitTransaction();
//        realm.close();
//        service.onDestroy();
//
//        Log.v("ordering", "notOld has finished");
//
//    }
}
