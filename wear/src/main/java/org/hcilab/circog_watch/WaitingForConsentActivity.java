package org.hcilab.circog_watch;

import android.content.Intent;
import android.os.Bundle;
import android.support.wearable.activity.WearableActivity;
import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.gms.wearable.DataClient;
import com.google.android.gms.wearable.DataEvent;
import com.google.android.gms.wearable.DataEventBuffer;
import com.google.android.gms.wearable.DataMapItem;
import com.google.android.gms.wearable.Wearable;

public class WaitingForConsentActivity extends WearableActivity implements DataClient.OnDataChangedListener {

    private static final String TAG = WaitingForConsentActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_waiting_for_consent);

        // Enables Always-on
        setAmbientEnabled();

        // Send message to handheld to launch the activity
        new MainActivity.StartActivityOnHandheld(this, MainActivity.HandheldActivity.ConsentActivity).execute();
    }

    @Override
    protected void onResume() {
        super.onResume();

        Wearable.getDataClient(this).addListener(this);
    }

    @Override
    protected void onPause() {
        super.onPause();

        Wearable.getDataClient(this).removeListener(this);
    }

    @Override
    public void onDataChanged(@NonNull DataEventBuffer dataEventBuffer) {
        Log.d(TAG, "onDataChanged(): " + dataEventBuffer);

        for (DataEvent event : dataEventBuffer) {
            if (event.getType() == DataEvent.TYPE_CHANGED) {
                String path = event.getDataItem().getUri().getPath();
                if (path != null && path.equals(CircogPrefs.CONSENT_RESULT_PATH)) {
                    DataMapItem dataMapItem = DataMapItem.fromDataItem(event.getDataItem());
                    boolean isConsentGiven = dataMapItem.getDataMap().getBoolean(CircogPrefs.CONSENT_SUCCESS_BOOL_KEY);
                    long time = dataMapItem.getDataMap().getLong(CircogPrefs.CONSENT_TIME_KEY);

                    Util.putBool(getApplicationContext(), CircogPrefs.PREF_CONSENT_GIVEN, isConsentGiven);
                    if (isConsentGiven) {
                        Util.putLong(getApplicationContext(), CircogPrefs.PREF_REGISTRATION_TIMESTAMP, time);

                        final Intent intent = new Intent(this, MainActivity.class);
                        startActivity(intent);

                        finish();
                    } else {
                        finish();
                    }

                } else {
                    Log.d(TAG, "Unrecognized path: " + path);
                }

            } else {
                Log.d(
                        TAG, "Unknown data event type \n Type = " + event.getType());
            }
        }
    }
}
