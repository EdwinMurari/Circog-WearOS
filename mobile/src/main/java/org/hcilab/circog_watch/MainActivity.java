package org.hcilab.circog_watch;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.WorkerThread;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.android.gms.wearable.DataEvent;
import com.google.android.gms.wearable.DataEventBuffer;
import com.google.android.gms.wearable.DataMapItem;
import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.Wearable;
import com.google.android.gms.wearable.WearableListenerService;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.ExecutionException;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = MainActivity.class.getSimpleName();
    private static final String START_CONSENT_CAPABILITY_NAME = "/start_consent_activity";
    public static final String START_DEMOGRAPHIC_CAPABILITY_NAME = "/start_demographic_activity";

    public static final String START_WEARABLE_APP_MESSAGE_PATH = "/start_wearable_app";
    public static final String SEND_DEMOGRAPHIC_RESULT = "/demographic_provided";

    public static boolean isConsentRunning = false;
    public static boolean isDemographicRunning = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    @Override
    public void onResume() {
        super.onResume();

        //track whether app has been opened through a notification or explicit app launch
        boolean notifTriggered = Util.getBool(getApplicationContext(), CircogPrefs.NOTIF_CLICKED, false);
        Util.putBool(getApplicationContext(), CircogPrefs.NOTIF_CLICKED, false);

        if (notifTriggered) {
            launchWearableApp();
        }

        //make sure NotificationTriggerService is running
        startService(new Intent(this, NotificationTriggerService.class));
    }

    @Override
    protected void onStart() {
        super.onStart();

        NotificationTriggerService.removeNotification(getApplicationContext());
    }

    public void launchWearableApp(View view) {
        launchWearableApp();
    }


    public static class ListenerServiceFromWear extends WearableListenerService {

        @Override
        public void onMessageReceived(MessageEvent messageEvent) {
            if (CircogPrefs.DEBUG_MODE) {
                Log.d(TAG, "onMessageReceived: " + messageEvent);
            }

            if (messageEvent.getPath().equals(START_CONSENT_CAPABILITY_NAME)) {

                if (isConsentRunning)
                    return;

                isConsentRunning = true;
                Intent startIntent = new Intent(this, ConsentActivity.class);
                startIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(startIntent);

            } else if (messageEvent.getPath().equals(START_DEMOGRAPHIC_CAPABILITY_NAME)) {

                if (isDemographicRunning)
                    return;

                isDemographicRunning = true;
                Intent startIntent = new Intent(this, DemographicActivity.class);
                startIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(startIntent);
            }
        }

        @Override
        public void onDataChanged(DataEventBuffer dataEventBuffer) {
            Log.d(TAG, "onDataChanged(): " + dataEventBuffer);

            for (DataEvent event : dataEventBuffer) {
                if (event.getType() == DataEvent.TYPE_CHANGED) {
                    String path = event.getDataItem().getUri().getPath();
                    if (path != null && path.equals(CircogPrefs.TASK_DETAILS_PATH)) {
                        DataMapItem dataMapItem = DataMapItem.fromDataItem(event.getDataItem());

                        int taskCount = dataMapItem.getDataMap().getInt(CircogPrefs.TASK_COUNT_KEY);
                        long lastTaskCompletedTime = dataMapItem.getDataMap().getLong(CircogPrefs.LAST_TASK_TIME_KEY);

                        if (CircogPrefs.DEBUG_MODE) {
                            Log.d(TAG, "Updating task count on handheld: " + taskCount
                                    + "\n Updating last task time on handheld: " + lastTaskCompletedTime);
                        }

                        Util.putInt(this, CircogPrefs.DAILY_TASK_COUNT, taskCount);
                        Util.putLong(this, CircogPrefs.DATE_LAST_TASK_COMPLETED, lastTaskCompletedTime);

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


    private void launchWearableApp() {
        new StartWearAppTask().execute();
    }

    private class StartWearAppTask extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... args) {
            Collection<String> nodes = getNodes();
            for (String node : nodes) {
                sendMessageToLaunchWearableApp(node);
            }
            return null;
        }
    }

    @WorkerThread
    private Collection<String> getNodes() {
        HashSet<String> results = new HashSet<>();

        Task<List<Node>> nodeListTask =
                Wearable.getNodeClient(getApplicationContext()).getConnectedNodes();

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

    private void sendMessageToLaunchWearableApp(String nodeId) {
        if (nodeId != null) {
            Task<Integer> sendTask =
                    Wearable.getMessageClient(getApplicationContext())
                            .sendMessage(nodeId, START_WEARABLE_APP_MESSAGE_PATH, new byte[0]);

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


    public void sendDemographicResult(boolean isProvided) {
        new SendDemographicResultAppTask(isProvided).execute();
    }

    private class SendDemographicResultAppTask extends AsyncTask<Void, Void, Void> {
        boolean _isProvided;

        SendDemographicResultAppTask(boolean isProvided) {
            _isProvided = isProvided;
        }

        @Override
        protected Void doInBackground(Void... args) {
            Collection<String> nodes = getNodes();
            for (String node : nodes) {
                sendDemographicResultToLaunchWearableApp(node, _isProvided);
            }
            return null;
        }
    }

    private void sendDemographicResultToLaunchWearableApp(String nodeId, boolean isDemographicProvided) {
        if (nodeId != null) {
            Task<Integer> sendTask =
                    Wearable.getMessageClient(getApplicationContext())
                            .sendMessage(nodeId, SEND_DEMOGRAPHIC_RESULT, new byte[]{
                                    (isDemographicProvided ? (byte) 1 : (byte) 0)
                            });

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
}
