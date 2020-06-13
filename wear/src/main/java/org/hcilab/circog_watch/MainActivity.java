package org.hcilab.circog_watch;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.wearable.activity.WearableActivity;
import android.util.Log;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.WorkerThread;
import androidx.core.app.ActivityCompat;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.Wearable;
import com.google.android.gms.wearable.WearableListenerService;

import org.hcilab.log.Logger;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.ExecutionException;

public class MainActivity extends WearableActivity implements ActivityCompat.OnRequestPermissionsResultCallback {

    private static final String TAG = MainActivity.class.getSimpleName();

    /* Id to identify local permission request for body sensors. */
    private static final int PERMISSION_REQUEST_WRITE_EXTERNAL_STORAGE = 1;

    public static final int[] COMPLETE_TASKLIST = {PvtActivity.TASK_ID, GoNoGoActivity.TASK_ID};

    private static boolean launchedFromNotif = false;
    private boolean mWearExternalStoragePermissionApproved;
    private static boolean isAppRunning = false;
    private boolean performingTest = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Enables Always-on
        setAmbientEnabled();

        if (CircogPrefs.DEBUG_MODE) {
            Log.i(TAG, "+ onCreate()");
        }

        isAppRunning = true;
        TaskList.initTaskList(getApplicationContext());
    }

    /*
     * Callback received when a permissions request has been completed.
     */
    @Override
    public void onRequestPermissionsResult(
            int requestCode, String[] permissions, int[] grantResults) {

        String permissionResult = "Request code: " + requestCode + ", Permissions: " + permissions
                + ", Results: " + grantResults;
        if (CircogPrefs.DEBUG_MODE) {
            Log.d(TAG, "onRequestPermissionsResult(): " + permissionResult);
        }


        if (requestCode == PERMISSION_REQUEST_WRITE_EXTERNAL_STORAGE) {
            mWearExternalStoragePermissionApproved = (grantResults.length == 1)
                    && (grantResults[0] == PackageManager.PERMISSION_GRANTED);

            if (mWearExternalStoragePermissionApproved)
                onResume();
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        isAppRunning = true;

        if (CircogPrefs.DEBUG_MODE) {
            Log.i(TAG, "+ onResume()");
        }

        mWearExternalStoragePermissionApproved =
                ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                        == PackageManager.PERMISSION_GRANTED;

        if (!mWearExternalStoragePermissionApproved) {
            ActivityCompat.requestPermissions(
                    this,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    PERMISSION_REQUEST_WRITE_EXTERNAL_STORAGE);
        } else {

            Logger.startLogger(getApplicationContext());

            // Log app launch method
            LogManager.logAppLaunch(launchedFromNotif);

            //check whether demographics have been recorded
            boolean provided = Util.getBool(getApplicationContext(), CircogPrefs.DEMOGRAPHICS_PROVIDED, false);

            //check whether consent has been given
            if (!Util.getBool(this, CircogPrefs.PREF_CONSENT_GIVEN, false)) {
                final Intent intent = new Intent(this, WaitingForConsentActivity.class);
                startActivity(intent);

                finish();
            } else if (performingTest) {

                launchNextTask();

                /*if (!provided) {
                    //launch demographics activity
                    final Intent intent = new Intent(this, RequestDemographicActivity.class);
                    startActivity(intent);

                    finish();
                }*/
            }
        }
    }

    @Override
    protected void onPause() {
        super.onPause();

        isAppRunning = false;
    }

    private void launchNextTask() {
        //launch task according to tasklist
        int task = TaskList.getCurrentTask(getApplicationContext());
        if (task == PvtActivity.TASK_ID) {
            launchPVT(findViewById(android.R.id.content));
        } else if (task == GoNoGoActivity.TASK_ID) {
            launchGoNoGo(findViewById(android.R.id.content));
        }

        finish();
    }

    public void launchPVT(final View view) {
        if (CircogPrefs.DEBUG_MODE) {
            Log.i(TAG, "launchPVT()");
        }
        Util.putString(getApplicationContext(), CircogPrefs.CURRENT_TASK, LogManager.KEY_PVT);
        final Intent intent = new Intent(this, PvtActivity.class);
        startActivity(intent);
//        finish();
    }

    public void launchGoNoGo(final View view) {
        if (CircogPrefs.DEBUG_MODE) {
            Log.i(TAG, "launchGoNoGo()");
        }
        Util.putString(getApplicationContext(), CircogPrefs.CURRENT_TASK, LogManager.KEY_GNG);
        final Intent intent = new Intent(this, GoNoGoActivity.class);
        startActivity(intent);
    }

    // region Button click listeners

    public void onLogCoffeeClick(final View view) {
        performingTest = false;

        final Intent intent = new Intent(this, LogCoffeeActivity.class);
        startActivity(intent);
    }

    public void onStartTestClick(final View view) {
        performingTest = true;

        onResume();
    }

    // endregion

    static class StartActivityOnHandheld extends AsyncTask<Void, Void, Void> {

        private HandheldActivity handheldActivity;
        private Context context;

        public StartActivityOnHandheld(Context context, HandheldActivity handheldActivity) {
            this.handheldActivity = handheldActivity;
            this.context = context;
        }

        @Override
        protected Void doInBackground(Void... args) {
            Collection<String> nodes = getNodes(context);
            for (String node : nodes) {
                sendMessageToLaunchConsentActivity(context, node, handheldActivity);
            }
            return null;
        }
    }

    @WorkerThread
    private static Collection<String> getNodes(Context context) {
        HashSet<String> results = new HashSet<>();

        Task<List<Node>> nodeListTask =
                Wearable.getNodeClient(context).getConnectedNodes();

        try {
            // Block on a task and get the result synchronously (because this is on a background
            // thread).
            List<Node> nodes = Tasks.await(nodeListTask);

            for (Node node : nodes) {
                results.add(node.getId());
            }

        } catch (ExecutionException exception) {
            if (CircogPrefs.DEBUG_MODE) {
                Log.e(TAG, "Task failed: " + exception);
            }
        } catch (InterruptedException exception) {
            if (CircogPrefs.DEBUG_MODE) {
                Log.e(TAG, "Interrupt occurred: " + exception);
            }
        }

        return results;
    }

    private static void sendMessageToLaunchConsentActivity(Context context, String nodeId, HandheldActivity handheldActivity) {
        if (nodeId != null) {
            Task<Integer> sendTask =
                    Wearable.getMessageClient(context)
                            .sendMessage(nodeId, handheldActivity.getMessagePath(), new byte[0]);

            sendTask.addOnSuccessListener(new OnSuccessListener<Integer>() {
                @Override
                public void onSuccess(Integer integer) {
                    if (CircogPrefs.DEBUG_MODE) {
                        Log.i(TAG, "OnSuccess sendTask");
                    }
                }
            });
            sendTask.addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    if (CircogPrefs.DEBUG_MODE) {
                        Log.e(TAG, "onFailure sendTask");
                    }
                }
            });
        } else {
            // Unable to retrieve node with transcription capability
            if (CircogPrefs.DEBUG_MODE) {
                Log.e(TAG, "Unable to retrieve node with transcription capability");
            }
        }
    }

    public static final String START_CONSENT_ACTIVITY_MESSAGE_PATH = "/start_consent_activity";
    public static final String START_DEMOGRAPHIC_ACTIVITY_MESSAGE_PATH = "/start_demographic_activity";
    public static final String START_DRINK_FAVORITE_SELECTION_ACTIVITY_MESSAGE_PATH = "/start_drinks_favorite_selection_activity";

    enum HandheldActivity {
        ConsentActivity,
        DemographicActivity,
        DrinksFavoriteActivity;

        public String getMessagePath() {
            switch (this) {
                case ConsentActivity:
                    return START_CONSENT_ACTIVITY_MESSAGE_PATH;
                case DemographicActivity:
                    return START_DEMOGRAPHIC_ACTIVITY_MESSAGE_PATH;
                case DrinksFavoriteActivity:
                    return START_DRINK_FAVORITE_SELECTION_ACTIVITY_MESSAGE_PATH;
            }
            return null;
        }
    }


    private static final String START_APP_CAPABILITY_NAME = "/start_wearable_app";
    public static final String SEND_DEMOGRAPHIC_RESULT = "/demographic_provided";

    public static class ListenerServiceFromHandheld extends WearableListenerService {

        @Override
        public void onMessageReceived(MessageEvent messageEvent) {
            if (CircogPrefs.DEBUG_MODE) {
                Log.d(TAG, "onMessageReceived: " + messageEvent);
            }

            if (messageEvent.getPath().equals(START_APP_CAPABILITY_NAME)) {
                launchedFromNotif = true;

                if (isAppRunning)
                    return;

                Intent startIntent = new Intent(this, MainActivity.class);
                startIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(startIntent);
            } else if(messageEvent.getPath().equals(SEND_DEMOGRAPHIC_RESULT)){
                Util.putBool(getApplicationContext(), CircogPrefs.DEMOGRAPHICS_PROVIDED, messageEvent.getData()[0] == (byte) 1);
            }
        }
    }
}
