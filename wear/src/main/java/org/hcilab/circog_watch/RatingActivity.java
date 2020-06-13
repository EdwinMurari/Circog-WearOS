package org.hcilab.circog_watch;

import android.content.Intent;
import android.os.Bundle;
import android.support.wearable.activity.WearableActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.RadioGroup;
import android.widget.TextView;

public class RatingActivity extends WearableActivity {

    public final static int REQUEST_RATE_ALERTNESS = 0;
    public final static int REQUEST_RATE_SLEEP = 1;

    public final static String RESULT_RATE_TYPE = "resultTypeRate";
    public static final String RESULT_RADIO_GROUP_INDEX = "resultRadioGroupIndex";

    private static int _selectedAlertnessIndex = 0;
    private static int _selectedSleepQualityIndex = 0;
    private static int type;
    private RadioGroup _radioGroup;
    private Button _btnConfirm;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rating);

        // Enables Always-on
        setAmbientEnabled();

        TextView radioHelpText = findViewById(R.id.tv_radio_header);
        _radioGroup = findViewById(R.id.alertness_radiogroup);
        _btnConfirm = findViewById(R.id.btnConfirm);

        try {
            type = getIntent().getIntExtra(RESULT_RATE_TYPE, 0);
            if (type == REQUEST_RATE_ALERTNESS) {
                radioHelpText.setText(R.string.task_survey_sleepiness);
                _radioGroup.check(_selectedAlertnessIndex);
                _btnConfirm.setVisibility(_selectedAlertnessIndex == 0 ? View.GONE : View.VISIBLE);
            } else if (type == REQUEST_RATE_SLEEP) {
                radioHelpText.setText(R.string.daily_survey_sleep_quality);
                _radioGroup.check(_selectedSleepQualityIndex);
                _btnConfirm.setVisibility(_selectedSleepQualityIndex == 0 ? View.GONE : View.VISIBLE);
            }
        } catch (Exception e) {
            Log.e(NumberPickerActivity.class.getSimpleName(), "Error: " + e.getMessage());
        }

        _radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int i) {

                if (type == REQUEST_RATE_ALERTNESS)
                    _selectedAlertnessIndex = i;
                else if (type == REQUEST_RATE_SLEEP)
                    _selectedSleepQualityIndex = i;

                if (_btnConfirm.getVisibility() == View.GONE)
                    _btnConfirm.setVisibility(View.VISIBLE);
            }
        });
    }

    public void onConfirmSelectionClick(View view) {
        Intent intent = getIntent();
        intent.putExtra(RESULT_RADIO_GROUP_INDEX, Util.getRating(_radioGroup));
        setResult(RESULT_OK, intent);

        finish();
    }
}
