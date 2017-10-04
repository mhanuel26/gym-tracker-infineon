package app.gymassistant.contest.com.gymassistantapp;

import android.app.IntentService;
import android.content.Intent;
import android.util.Log;

import java.util.ArrayList;

/**
 * Created by mhanuel on 10/3/17.
 */

public class RSSPullService extends IntentService {

    private static final String TAG = RSSPullService.class.getName();

    public RSSPullService() {
        super("RSSPullService");
    }

    @Override
    protected void onHandleIntent(Intent workIntent) {
        // Gets data from the incoming Intent
//        String dataString = workIntent.getDataString();
        String jsonData = workIntent.getStringExtra("logData");

        Log.i(TAG, "Intent Service getStringExtra: " + jsonData);
    }
}