package app.gymassistant.contest.com.gymassistantapp;

/**
 * Created by mhanuel on 9/29/17.
 */

import android.content.Context;
import android.content.SharedPreferences;

import ai.api.util.BluetoothController;

public class SettingsManager {

    private static final String SETTINGS_PREFS_NAME = "ai.api.APP_SETTINGS";
    private static final String PREF_USE_BLUETOOTH = "USE_BLUETOOTH";

    private final Context context;
    private SharedPreferences prefs;

    private boolean useBluetooth;

    public SettingsManager(final Context context) {
        this.context = context;
        prefs = context.getSharedPreferences(SETTINGS_PREFS_NAME, Context.MODE_PRIVATE);

        useBluetooth = prefs.getBoolean(PREF_USE_BLUETOOTH, true);
    }

    public void setUseBluetooth(final boolean useBluetooth) {
        this.useBluetooth = useBluetooth;

        prefs.edit().putBoolean(PREF_USE_BLUETOOTH, useBluetooth).commit();
        final BluetoothController controller = ((AIApplication) context.getApplicationContext()).getBluetoothController();
        if (useBluetooth) {
            controller.start();
        } else {
            controller.stop();
        }
    }

    public boolean isUseBluetooth() {
        return useBluetooth;
    }

}