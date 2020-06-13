package org.hcilab.circog_watch;

import android.os.Bundle;
import android.support.wearable.activity.WearableActivity;
import android.util.Log;
import android.view.View;

import androidx.annotation.NonNull;

import com.google.android.gms.wearable.DataClient;
import com.google.android.gms.wearable.DataEvent;
import com.google.android.gms.wearable.DataEventBuffer;
import com.google.android.gms.wearable.DataMapItem;
import com.google.android.gms.wearable.WearableListenerService;

public class RequestDemographicActivity extends WearableActivity {

    private static final String TAG = RequestDemographicActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_request_demographic);

        // Enables Always-on
        setAmbientEnabled();

        // Send message to handheld to launch the activity
        new MainActivity.StartActivityOnHandheld(this, MainActivity.HandheldActivity.DemographicActivity).execute();
    }

    public void onAckFillDemographic(View view) {
        finish();
    }

    public static class ListenerServiceFromHandheld extends WearableListenerService implements DataClient.OnDataChangedListener {

        @Override
        public void onDataChanged(@NonNull DataEventBuffer dataEventBuffer) {
            Log.d(TAG, "onDataChanged(): " + dataEventBuffer);

            for (DataEvent event : dataEventBuffer) {
                if (event.getType() == DataEvent.TYPE_CHANGED) {
                    String path = event.getDataItem().getUri().getPath();
                    if (path != null && path.equals(CircogPrefs.DEMOGRAPHIC_RESULT_PATH)) {
                        DataMapItem dataMapItem = DataMapItem.fromDataItem(event.getDataItem());

                        int age = dataMapItem.getDataMap().getInt(CircogPrefs.DEMOGRAPHIC_AGE_KEY);
                        String gender = dataMapItem.getDataMap().getString(CircogPrefs.DEMOGRAPHIC_GENDER_KEY);
                        int genderPos = dataMapItem.getDataMap().getInt(CircogPrefs.DEMOGRAPHIC_GENDER_POS_KEY);
                        String profession = dataMapItem.getDataMap().getString(CircogPrefs.DEMOGRAPHIC_PROFESSION_KEY);
                        String email = dataMapItem.getDataMap().getString(CircogPrefs.DEMOGRAPHIC_EMAIL_KEY);

                        if (CircogPrefs.DEBUG_MODE) {
                            Log.i(TAG, "** Demographics provided **");
                            Log.i(TAG, "age: " + age);
                            Log.i(TAG, "gender: " + gender);
                            Log.i(TAG, "profession: " + profession);
                            Log.i(TAG, "gender position: " + genderPos);
                            Log.i(TAG, "email: " + email);

                        }

                        Util.putInt(getApplicationContext(), CircogPrefs.PREF_AGE, age);
                        Util.putString(getApplicationContext(), CircogPrefs.PREF_GENDER, gender);
                        Util.putInt(getApplicationContext(), CircogPrefs.PREF_GENDER_POS, genderPos);
                        Util.putString(getApplicationContext(), CircogPrefs.PREF_PROFESSION, profession);
                        Util.putString(getApplicationContext(), CircogPrefs.PREF_EMAIL, email);
                        Util.putBool(getApplicationContext(), CircogPrefs.DEMOGRAPHICS_PROVIDED, true);

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
}