package org.hcilab.circog_watch;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.wearable.activity.WearableActivity;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Date;
import java.util.Random;

public class PvtActivity extends WearableActivity {

    public static final int TASK_ID = 1;
    private static final String TAG = PvtActivity.class.getSimpleName();

    //SETTINGS
    private static final int MIN_DELAY = 2000; //ms
    private static final int MAX_DELAY = 6000; //ms
    private static final int MIN_TIME_PASSED = 100; //ms
    private static final int MAX_TIME_PASSED = 3000; //ms
    private static final int TOAST_DELAY = 1000; //ms
    private static final int MIN_TASKS = 8;
    private static final int MAX_TASKS = 12;

    private View instructionLayout;
    private View testLayout;

    private TextView ticker;
    private TextView warningText;

    private boolean running;
    private long startTime;
    private int taskCount;
    private ArrayList<Long> measurements;
    private int numberOfTaps;
    private boolean allTasksCompleted;
    private long startTasksTime;
    private long endTasksTime;
    private int total_tasks;
    private boolean clicksPenalized;

    //handels the timer shown
    private Handler timerHandler = new Handler();
    private Runnable timerRunnable = new Runnable() {

        @Override
        public void run() {
            long millis = System.currentTimeMillis() - startTime;
            ticker.setText(String.format("%d", millis));
            timerHandler.postDelayed(this, 0);
        }
    };

    //manages the appearance of the timer
    private Handler taskHandler = new Handler();
    private Runnable taskRunnable = new Runnable() {

        @Override
        public void run() {
            Log.i(TAG, "+ starting timer");
            startTime = System.currentTimeMillis();
            timerHandler.postDelayed(timerRunnable, 0);
            running = true;
            clicksPenalized = false;
        }
    };

    //manages the cancel timer after 3 seconds
    private Handler cancelHandler = new Handler();
    private Runnable cancelRunnable = new Runnable() {
        @Override
        public void run() {
            Log.i(TAG, "- cancel task timer fired");
            timerHandler.removeCallbacks(timerRunnable);
            taskHandler.removeCallbacks(taskRunnable);
            ticker.setText("");
            running = false;
            scheduleNextTask();
        }
    };

    //manages the toast message for premature taps
    private Handler premTabHandler = new Handler();
    private Runnable premTabRunnable = new Runnable() {
        @Override
        public void run() {
            clicksPenalized = true;
        }
    };

    private Handler hideWarningHandler = new Handler();
    private Runnable hideWarningRunnable = new Runnable() {
        @Override
        public void run() {
            warningText.setVisibility(View.GONE);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pvt);

        running = false;
        startTime = 0;
        taskCount = 0;
        measurements = new ArrayList<Long>();
        numberOfTaps = 0;
        allTasksCompleted = false;
        clicksPenalized = false;

        //random number of targets
        if (CircogPrefs.DEBUG_MODE) {
            total_tasks = 1;
        } else {
            Random rand = new Random();
            total_tasks = MIN_TASKS + rand.nextInt((MAX_TASKS - MIN_TASKS) + 1);
        }

        if (CircogPrefs.DEBUG_MODE) {
            Log.i(TAG, "number of total tasks generated: " + total_tasks);
        }

        instructionLayout = findViewById(R.id.pvt_lyt_exp);
        testLayout = findViewById(R.id.pvt_lyt_test);

        ticker = findViewById(R.id.pvt_ticker);
        warningText = findViewById(R.id.pvt_warning);

        testLayout.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (running) {
                    Log.i(TAG, "- stopping timer");
                    timerHandler.removeCallbacks(timerRunnable);
                    cancelHandler.removeCallbacks(cancelRunnable);
                    running = false;

                    //TODO: use event time
                    long timePassed = System.currentTimeMillis() - startTime;

                    if (timePassed > MIN_TIME_PASSED) {
                        measurements.add(timePassed);
                        taskCount++;
                    }
                    scheduleNextTask();
                }

                return false;
            }
        });

        /**
         * collects numberOfTaps, i.e. premature taps
         */
        testLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (!running) {
                    numberOfTaps++;
                }

                if (clicksPenalized) {
                    warningText.setVisibility(View.VISIBLE);
                    hideWarningHandler.postDelayed(hideWarningRunnable, TOAST_DELAY);
                }

            }
        });

        //check whether daily survey has been answered yet
        Date lastDailySurveyTaken = new Date(Util.getLong(getApplicationContext(), CircogPrefs.DATE_LAST_DAILY_SURVEY_MS, 0));
        Date now = new Date(System.currentTimeMillis());
        boolean dailySurveyAnswered = Util.isSameDay(lastDailySurveyTaken, now);
//        if(CircogPrefs.DEBUG_MODE || !dailySurveyAnswered) {
        if (!dailySurveyAnswered) {
            startActivity(new Intent(getApplicationContext(), DailySurveyActivity.class));
        }

        //show alert assessments
        ArrayList<Integer> tasklist = TaskList.getTaskList(getApplicationContext());
        if (tasklist.size() == MainActivity.COMPLETE_TASKLIST.length) {
//            Util.showTaskSurveyDialog(this);
            final Intent intent = new Intent(this, AlertnessSurveyActivity.class);
            startActivity(intent);
        }
    }

    private void scheduleNextTask() {
        if (taskCount < total_tasks) {
            Random rand = new Random();
            long interval = MIN_DELAY + rand.nextInt((MAX_DELAY - MIN_DELAY) + 1);
            long now = System.currentTimeMillis();

            taskHandler.postAtTime(taskRunnable, now + interval);
            taskHandler.postDelayed(taskRunnable, interval);

            cancelHandler.postAtTime(cancelRunnable, now + interval);
            cancelHandler.postDelayed(cancelRunnable, interval + MAX_TIME_PASSED);

            ticker.setText("");
            Log.i(TAG, "+ task scheduled in " + interval + " ms");

            premTabHandler.postAtTime(premTabRunnable, now + TOAST_DELAY);
            premTabHandler.postDelayed(premTabRunnable, TOAST_DELAY);

        } else {
            Log.i(TAG, "- tasks finished: " + measurements.size() + " measurements.");
            Log.i(TAG, measurements.toString());
            allTasksCompleted = true;
            endTasksTime = System.currentTimeMillis();
            showFinishScreen();
        }
    }

    private void showFinishScreen() {

        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        String avg = String.format("%.1f", Util.calculateAverage(measurements));

        alertDialogBuilder.setMessage(TaskList.getNextTask(getApplicationContext()));

        alertDialogBuilder.setPositiveButton(getString(R.string.pvt_dialog_ok), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                launchNextTask();
            }
        });
        alertDialogBuilder.show();

        Log.i(TAG, "# of measurements: " + measurements.size() + ", avg: " + avg);
        Log.i(TAG, "false positives (touches): " + numberOfTaps);

        //logging results
        int alertness = Util.getInt(this, CircogPrefs.LEVEL_ALERTNESS, -1);
        boolean caffeinated = Util.getBool(this, CircogPrefs.CAFFEINATED, false);
        boolean taskCompleted = measurements.size() == total_tasks;
        LogManager.logPVT(measurements, numberOfTaps, startTasksTime, endTasksTime, taskCompleted, alertness, caffeinated);
        allTasksCompleted = true;
    }

    /**
     * launches next task in tasklist and finishes current activity
     */
    private void launchNextTask() {
        int task = TaskList.getCurrentTask(getApplicationContext());
        if (task == PvtActivity.TASK_ID) {
            if (CircogPrefs.DEBUG_MODE) {
                Log.i(TAG, "launchPVT()");
            }
            Util.putString(getApplicationContext(), CircogPrefs.CURRENT_TASK, LogManager.KEY_PVT);
            final Intent intent = new Intent(this, PvtActivity.class);
            startActivity(intent);
        } else if (task == GoNoGoActivity.TASK_ID) {
            if (CircogPrefs.DEBUG_MODE) {
                Log.i(TAG, "launchGoNoGo()");
            }
            Util.putString(getApplicationContext(), CircogPrefs.CURRENT_TASK, LogManager.KEY_GNG);
            final Intent intent = new Intent(this, GoNoGoActivity.class);
            startActivity(intent);
        }

        //check whether this is the last task
        ArrayList<Integer> tasklist = TaskList.getTaskList(getApplicationContext());
        if (tasklist.size() == 0) {

            //increment DAILY_TASK_COUNT
            TaskList.incrementDailyTaskCount(getApplicationContext());

            //bye bye message
            Toast.makeText(PvtActivity.this, R.string.task_toast_finish, Toast.LENGTH_LONG).show();
        }
        finish();
    }

    public void startPvt(View view) {
        instructionLayout.setVisibility(View.GONE);
        testLayout.setVisibility(View.VISIBLE);

        Log.i(TAG, "+ start PVT task");
        startTasksTime = System.currentTimeMillis();
        scheduleNextTask();
    }

    @Override
    protected void onResume() {
        super.onResume();

        //reset all values on resume
        running = false;
        startTime = 0;
        taskCount = 0;
        numberOfTaps = 0;
        allTasksCompleted = false;
        measurements = new ArrayList<Long>();
    }

    @Override
    public void onPause() {
        super.onPause();
        cancelTimers();

        if (!allTasksCompleted) {
            endTasksTime = System.currentTimeMillis();
            int alertness = Util.getInt(this, CircogPrefs.LEVEL_ALERTNESS, -1);
            boolean caffeinated = Util.getBool(this, CircogPrefs.CAFFEINATED, false);
            LogManager.logPVT(measurements, numberOfTaps, startTasksTime, endTasksTime, allTasksCompleted, alertness, caffeinated);
        }
    }

    private void cancelTimers() {
        timerHandler.removeCallbacks(timerRunnable);
        taskHandler.removeCallbacks(taskRunnable);
        cancelHandler.removeCallbacks(cancelRunnable);
    }
}
