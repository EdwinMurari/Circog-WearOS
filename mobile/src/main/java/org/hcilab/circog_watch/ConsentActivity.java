package org.hcilab.circog_watch;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.wearable.DataItem;
import com.google.android.gms.wearable.PutDataMapRequest;
import com.google.android.gms.wearable.PutDataRequest;
import com.google.android.gms.wearable.Wearable;

public class ConsentActivity extends Activity {

    private static final String TAG = ConsentActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_consent);
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    private void startNextActivity() {
        /*final Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);*/
        finish();
    }

    public void onGiveConsent(final View view) {
        if (CircogPrefs.DEBUG_MODE) {
            Log.i(TAG, "onGiveConsent()");
        }

        sendConsentResultToWearApp(true);
        startNextActivity();
    }

    private void sendConsentResultToWearApp(boolean isSuccess) {
        PutDataMapRequest dataMap = PutDataMapRequest.create(CircogPrefs.CONSENT_RESULT_PATH);
        dataMap.getDataMap().putBoolean(CircogPrefs.CONSENT_SUCCESS_BOOL_KEY, isSuccess);
        dataMap.getDataMap().putLong(CircogPrefs.CONSENT_TIME_KEY, System.currentTimeMillis());
        PutDataRequest request = dataMap.asPutDataRequest();
        request.setUrgent();

        Task<DataItem> dataItemTask = Wearable.getDataClient(this).putDataItem(request);

        dataItemTask.addOnSuccessListener(
                new OnSuccessListener<DataItem>() {
                    @Override
                    public void onSuccess(DataItem dataItem) {
                        if (CircogPrefs.DEBUG_MODE) {
                            Log.i(TAG, "Sending consent result message was successful: " + dataItem);
                        }
                    }
                });
    }

    public void onDenyConsent(final View view) {
        if (CircogPrefs.DEBUG_MODE) {
            Log.i(TAG, "onDenyConsent()");
        }

        sendConsentResultToWearApp(false);
        finish();
    }

    @Override
    public void finish() {
        MainActivity.isConsentRunning = false;

        super.finish();
    }
}
