package io.ourglass.amstelbright2;

import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.test.ActivityTestCase;
import android.test.ActivityUnitTestCase;
import android.test.ApplicationTestCase;
import android.test.suitebuilder.annotation.LargeTest;

import io.ourglass.amstelbright2.core.OGCore;
import io.ourglass.amstelbright2.realm.OGLog;
import io.ourglass.amstelbright2.services.applejack_comm.LogCleanAndPushService;
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
        OGCore.log_channelChange(null, null, null, null, null, null);
        OGCore.log_alert(null, null);
        OGCore.log_placementOverride(null, null, null, 0);
        //OGCore.log_adImpression(null);
        OGCore.log_heartbeat(null, null, null);

    }

    @LargeTest
    public void testUpload(){
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
        realm.beginTransaction();
        service.uploadLogs(logs);
        realm.commitTransaction();

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
    }

    @LargeTest
    public void testReapNotOld(){
        Realm realm = Realm.getDefaultInstance();

        //make sure there is nothing in the database
        RealmResults<OGLog> logs = realm.where(OGLog.class).findAll();

        assertEquals("There were already logs in the database, likely there is a service running that is creating them",
                0, logs.size());

        createTestLogs();

        //ensure that logs were created
        logs = realm.where(OGLog.class).findAll();
        assertEquals("There was a problem creating OGLogs", 4, logs.size());

        //set the time uploaded at shorter than one week (should not be reaped)
        realm.beginTransaction();
        for(OGLog log : logs){
            log.setUploaded(System.currentTimeMillis());
        }
        realm.commitTransaction();

        LogCleanAndPushService service = new LogCleanAndPushService();
        logs = service.getOldLogs();

        assertEquals("Logs were incorrectly identified as old", 0, logs.size());

        //clean up
        realm.beginTransaction();
        realm.deleteAll();
        realm.commitTransaction();
        realm.close();
    }

    @LargeTest
    public void testReapOld(){
        Realm realm = Realm.getDefaultInstance();

        //make sure there is nothing in the database
        RealmResults<OGLog> logs = realm.where(OGLog.class).findAll();

        assertEquals("There were already logs in the database, likely there is a service running that is creating them",
                0, logs.size());

        createTestLogs();

        //ensure that logs were created
        logs = realm.where(OGLog.class).findAll();
        assertEquals("There was a problem creating OGLogs", 4, logs.size());

        //todo change this when settings are implemented so that the uploaded time of the logs is longer than the set time
        Long oldUploadTime = System.currentTimeMillis() - (1000 * 60 * 60 * 24 * 7) - (1000 * 60 * 30); //30 minutes older than a week old

        //set the time uploaded at longer than a week (should be reaped)
        realm.beginTransaction();
        for(OGLog log : logs)
            log.setUploaded(oldUploadTime);
        realm.commitTransaction();

        //make sure that they are uploaded
        for(OGLog log : logs)
            if(!log.isUploaded())
                fail("log upload failed");

        LogCleanAndPushService service = new LogCleanAndPushService();
        logs = service.getOldLogs();

        assertEquals("Logs incorrectly identified as not old", 4, logs.size());

        realm.beginTransaction();
        service.reapLogs(logs);
        realm.commitTransaction();

        logs = realm.where(OGLog.class).findAll();

        //these logs should have been reaped from the database
        assertEquals("Old logs not successfully reaped", 0, logs.size());

        //clean up
        realm.beginTransaction();
        realm.deleteAll();
        realm.commitTransaction();
        realm.close();
    }
}
