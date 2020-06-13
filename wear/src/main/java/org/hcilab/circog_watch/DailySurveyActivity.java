package org.hcilab.circog_watch;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.wearable.activity.WearableActivity;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import androidx.core.app.ActivityCompat;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInStatusCodes;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.fitness.Fitness;
import com.google.android.gms.fitness.FitnessActivities;
import com.google.android.gms.fitness.FitnessOptions;
import com.google.android.gms.fitness.SessionsClient;
import com.google.android.gms.fitness.data.DataPoint;
import com.google.android.gms.fitness.data.DataSet;
import com.google.android.gms.fitness.data.DataType;
import com.google.android.gms.fitness.data.Field;
import com.google.android.gms.fitness.data.Session;
import com.google.android.gms.fitness.request.DataReadRequest;
import com.google.android.gms.fitness.request.SessionReadRequest;
import com.google.android.gms.fitness.result.SessionReadResponse;
import com.google.android.gms.tasks.Task;
import com.kimjio.wear.datetimepicker.app.TimePickerDialog;
import com.kimjio.wear.datetimepicker.widget.TimePicker;

import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static android.Manifest.permission.ACTIVITY_RECOGNITION;

public class DailySurveyActivity extends WearableActivity {

    private static final String TAG = DailySurveyActivity.class.getSimpleName();

    private static final int DEFAULT_HOUR = 8;
    private static final int DEFAULT_MINUTE = 0;

    private static final int REQUEST_HOURS_SLEPT = 0;
    private static final int GOOGLE_FIT_PERMISSIONS_REQUEST_CODE = 1;
    private static final int REQUEST_SLEEP_QUALITY = 2;

    private FitnessOptions fitnessOptions;
    private int hoursSlept = 0;
    private int sleepQuality = -1;

    private TextView errorMessage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_daily_survey);

        // Enables Always-on
        setAmbientEnabled();

        errorMessage = findViewById(R.id.tv_error_message);

        Util.putBool(getApplicationContext(), CircogPrefs.LAST_WAKEUP_SET, false);

//        signIn();
//        initGoogleFitAccount();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        /*if (requestCode == REQUEST_CODE_SIGN_IN) {
            // The Task returned from this call is always completed, no need to attach
            // a listener.
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            handleSignInResult(task);
            return;
        }*/

        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == REQUEST_HOURS_SLEPT) {
                hoursSlept = data.getIntExtra(NumberPickerActivity.RESULT_PICKED_NUMBER, 0);
            } else if (requestCode == REQUEST_SLEEP_QUALITY) {
                sleepQuality = data.getIntExtra(RatingActivity.RESULT_RADIO_GROUP_INDEX, 0);
            }
        }
    }

    public void launchHoursSleptPicker(View view) {
        final Intent intent = new Intent(this, NumberPickerActivity.class);
        intent.putExtra(NumberPickerActivity.RESULT_TYPE_PICK, NumberPickerActivity.REQUEST_PICK_HOURS);
        startActivityForResult(intent, REQUEST_HOURS_SLEPT);
    }

    public void launchWakeupTimePicker(View view) {
        int initHour = Util.getInt(getApplicationContext(), CircogPrefs.LAST_WAKEUP_HOUR, DEFAULT_HOUR);
        int initMinute = Util.getInt(getApplicationContext(), CircogPrefs.LAST_WAKEUP_MINUTE, DEFAULT_MINUTE);

        TimePickerDialog timePickerDialog = new TimePickerDialog(this, true);
        timePickerDialog.updateTime(initHour, initMinute);
        timePickerDialog.setOnTimeSetListener(new TimePickerDialog.OnTimeSetListener() {
            @Override
            public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                Util.putInt(getApplicationContext(), CircogPrefs.LAST_WAKEUP_HOUR, hourOfDay);
                Util.putInt(getApplicationContext(), CircogPrefs.LAST_WAKEUP_MINUTE, minute);
                Util.putBool(getApplicationContext(), CircogPrefs.LAST_WAKEUP_SET, true);
            }
        });
        timePickerDialog.show();
    }

    public void launchSleepQualityPicker(View view) {
        final Intent intent = new Intent(this, RatingActivity.class);
        intent.putExtra(RatingActivity.RESULT_RATE_TYPE, RatingActivity.REQUEST_RATE_SLEEP);
        startActivityForResult(intent, REQUEST_SLEEP_QUALITY);
    }

    public void onSubmitDailySurveyClick(View view) {

        //force users to put in a time or take a default
        boolean wakeupTimeSet = Util.getBool(getApplicationContext(), CircogPrefs.LAST_WAKEUP_SET, false);
        if (!wakeupTimeSet) {
            errorMessage.setText(R.string.daily_survey_error_wakeup_time);
            errorMessage.setVisibility(View.VISIBLE);
            return;
        }

        //force users to indicate their hours slept
        if (hoursSlept < 1) {
            errorMessage.setText(R.string.daily_survey_error_hours_slept);
            errorMessage.setVisibility(View.VISIBLE);
            return;
        }

        //force users to rate their sleep
        if (sleepQuality == -1) {
            errorMessage.setText(R.string.daily_survey_error_sleep_quality);
            errorMessage.setVisibility(View.VISIBLE);
            return;
        }

        int wakeupHour = Util.getInt(getApplicationContext(), CircogPrefs.LAST_WAKEUP_HOUR, -1);
        int wakeupMinute = Util.getInt(getApplicationContext(), CircogPrefs.LAST_WAKEUP_MINUTE, -1);

        if (CircogPrefs.DEBUG_MODE) {
            Log.i(TAG, "sleep quality rating: " + sleepQuality);
            Log.i(TAG, "woke up at: " + wakeupHour + ":" + wakeupMinute);
            Log.i(TAG, "slept for hours: " + hoursSlept);
        }

        //update last survey date
        Util.putLong(getApplicationContext(), CircogPrefs.DATE_LAST_DAILY_SURVEY_MS, System.currentTimeMillis());

        //update last hours slept selection
        Util.putInt(getApplicationContext(), CircogPrefs.LAST_HOURS_SLEPT, hoursSlept);

        //log daily survey: wakeupHour, wakeupMinute
        LogManager.logDailySurveyFilledIn(wakeupHour, wakeupMinute, hoursSlept, sleepQuality);

        finish();
    }


    // region Google fit API

    /**
     * Google fit API for sleep tracking
     **/
    private void initGoogleFitAccount() {
        fitnessOptions = FitnessOptions.builder()
                .addDataType(DataType.TYPE_ACTIVITY_SEGMENT, FitnessOptions.ACCESS_READ)
                .build();

        GoogleSignInAccount account = GoogleSignIn.getAccountForExtension(this, fitnessOptions);

        if (!GoogleSignIn.hasPermissions(account, fitnessOptions)) {
            if (CircogPrefs.DEBUG_MODE) {
                Log.i(TAG, "Requesting google fit permission");
            }
            GoogleSignIn.requestPermissions(
                    this,
                    GOOGLE_FIT_PERMISSIONS_REQUEST_CODE,
                    account,
                    fitnessOptions);
        } else {
            if (CircogPrefs.DEBUG_MODE) {
                Log.i(TAG, "Accessing google fit history");
            }

            accessGoogleFitHistory();
        }
    }


    private void getSleepFromSession() {
        GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(this);

        if (account == null) {
            if (CircogPrefs.DEBUG_MODE) {
                Log.e(TAG, "Google account not signed in");
            }
            return;
        }

        SessionsClient sessionClient = Fitness.getSessionsClient(this, account);

        // Note: The android.permission.ACTIVITY_RECOGNITION permission is
        // required to read DataType.TYPE_ACTIVITY_SEGMENT
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.HOUR_OF_DAY, 12);
        int endTime = (int) calendar.getTimeInMillis();

        Log.e("TEST", "Current time: " + System.currentTimeMillis());
        Log.e("TEST", "End time: " + Calendar.getInstance().getTimeInMillis());

        SessionReadRequest request = new SessionReadRequest.Builder()
                .readSessionsFromAllApps()
                // Activity segment data is required for details of the fine-
                // granularity sleep, if it is present.
                .read(DataType.TYPE_ACTIVITY_SEGMENT)
                .setTimeInterval(System.currentTimeMillis(), endTime, TimeUnit.SECONDS)
                .enableServerQueries()
                .build();

        Task<SessionReadResponse> task = sessionClient.readSession(request);

        task.addOnSuccessListener(response -> {
            Log.e("TEST", "TASK ONSUCCESS");


            // Filter the resulting list of sessions to just those that are sleep.
            List<Session> sleepSessions = response.getSessions().stream()
                    .filter(s -> s.getActivity().equals(FitnessActivities.SLEEP))
                    .collect(Collectors.toList());

            LogManager.logSleepSessionDataRaw(sleepSessions.toString());

            for (Session session : sleepSessions) {
                Log.d("AppName", String.format("Sleep between %d and %d",
                        session.getStartTime(TimeUnit.SECONDS),
                        session.getEndTime(TimeUnit.SECONDS)));

                // If the sleep session has finer granularity sub-components, extract them:
                List<DataSet> dataSets = response.getDataSet(session);
                for (DataSet dataSet : dataSets) {
                    for (DataPoint point : dataSet.getDataPoints()) {
                        // The Activity defines whether this segment is light, deep, REM or awake.
                        String sleepStage = point.getValue(Field.FIELD_ACTIVITY).asActivity();
                        long start = point.getStartTime(TimeUnit.SECONDS);
                        long end = point.getEndTime(TimeUnit.SECONDS);
                        Log.d(TAG,
                                String.format("\t* %s between %d and %d", sleepStage, start, end));
                        LogManager.logSleepSessionDataDetected(sleepStage, start, end);
                    }
                }
            }
        });
    }

    private void accessGoogleFitHistory() {

        Calendar cal = Calendar.getInstance();
        cal.setTime(new Date());
        long endTime = cal.getTimeInMillis();
        cal.add(Calendar.DAY_OF_WEEK, -1);
        long startTime = cal.getTimeInMillis();

        DataReadRequest readRequest = new DataReadRequest.Builder()
                .read(DataType.TYPE_ACTIVITY_SEGMENT)
                .enableServerQueries()
                .setTimeRange(startTime, endTime, TimeUnit.MILLISECONDS)
                .bucketByTime(1, TimeUnit.DAYS)
                .build();

        GoogleSignInAccount account = GoogleSignIn
                .getAccountForExtension(this, fitnessOptions);

        Fitness.getHistoryClient(this, account)
                .readData(readRequest)
                .addOnSuccessListener(response -> {

                    if (CircogPrefs.DEBUG_MODE) {
                        Log.i(TAG, "Successfully fetched google fit history!");
                    }

                    List<DataSet> dataSets = response.getDataSets();

                    // TODO :: Remove later
                    LogManager.logSleepDataTemp(dataSets.toString());

                    for (DataSet dataSet : dataSets) {
                        for (DataPoint point : dataSet.getDataPoints()) {
                            // The Activity defines whether this segment is light, deep, REM or awake.
                            String sleepStage = point.getValue(Field.FIELD_ACTIVITY).asActivity();
                            long start = point.getStartTime(TimeUnit.SECONDS);
                            long end = point.getEndTime(TimeUnit.SECONDS);

                            if (CircogPrefs.DEBUG_MODE) {
                                Log.i(TAG, String.format("\t* %s between %d and %d", sleepStage, start, end));
                            }

                            //log sleep data obtained from google fit
                            LogManager.logGoogleFitDetectedSleep(sleepStage, start, end);
                        }
                    }
                })
                .addOnFailureListener(e -> {
                    if (CircogPrefs.DEBUG_MODE) {
                        Log.e(TAG, "OnFailure()", e);
                    }
                });
    }

    private void getSleepData() {
        initGoogleFitAccount();
//        getSleepFromSession();
    }

    // endregion

    //region Google Sign-in
    public static final int REQUEST_CODE_SIGN_IN = 8001;

    private GoogleSignInClient mGoogleSignInClient;

    /**
     * Starts Google sign in activity, response handled in onActivityResult.
     */
    private void signIn() {
        if (GoogleSignIn.getLastSignedInAccount(this) != null) {
            getSleepData();

            return;
        }

        setupGoogleSignInClient();

        if (mGoogleSignInClient == null) {
            Log.e(TAG, "Google Sign In API client not initialized.");
            return;
        }
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, REQUEST_CODE_SIGN_IN);
    }

    /**
     * Configures the GoogleApiClient used for sign in. Requests scopes profile and email.
     */
    protected void setupGoogleSignInClient() {
        GoogleSignInOptions gso = new GoogleSignInOptions
                .Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestProfile()
                .requestEmail()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);
    }

    protected void handleSignInResult(Task<GoogleSignInAccount> completedTask) {
        try {
            GoogleSignInAccount account = completedTask.getResult(ApiException.class);

            // Signed in successfully, show authenticated UI.
            if (CircogPrefs.DEBUG_MODE) {
                Log.i(TAG, "Signed in to google");
            }

            getSleepData();

        } catch (ApiException e) {
            // The ApiException status code indicates the detailed failure reason.
            // Please refer to the GoogleSignInStatusCodes class reference for more information.
            if (CircogPrefs.DEBUG_MODE) {
                Log.w(TAG,
                        "signInResult:failed code=" + e.getStatusCode() + ". Msg=" + GoogleSignInStatusCodes.getStatusCodeString(e.getStatusCode()));
            }
        }
    }

    //endregion
}
