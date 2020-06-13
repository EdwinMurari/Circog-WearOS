package org.hcilab.circog_watch;

import android.content.Intent;
import android.os.Bundle;
import android.support.wearable.activity.WearableActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.NumberPicker;
import android.widget.TextView;

public class NumberPickerActivity extends WearableActivity {

    private static final String TAG = NumberPickerActivity.class.getSimpleName();

    public final static int REQUEST_PICK_DRINK_QUANTITY = 0;
    public final static int REQUEST_PICK_HOURS = 1;

    public final static String RESULT_PICKED_NUMBER = "resultPickedNumber";
    public final static String RESULT_TYPE_PICK = "resultTypePick";

    public static final String[] CUP_TYPES = {"Small (280ml)", "Medium (400ml)", "Large (500ml)"};

    NumberPicker numberPicker;
    NumberPicker cupPicker;

    private static int pickerType;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_drink_quantity_selection);

        // Enables Always-on
        setAmbientEnabled();

        TextView helpText = findViewById(R.id.number_picker_help_text);

        numberPicker = findViewById(R.id.numberPicker);
        numberPicker.setWrapSelectorWheel(false);

        cupPicker = findViewById(R.id.cupPicker);
        cupPicker.setMinValue(1);
        cupPicker.setMaxValue(3);
        cupPicker.setWrapSelectorWheel(false);
        cupPicker.setDisplayedValues(CUP_TYPES);

        int totalNumberCount = 1;
        try {
            pickerType = getIntent().getIntExtra(RESULT_TYPE_PICK, 0);
            if (pickerType == REQUEST_PICK_DRINK_QUANTITY) {
                helpText.setText(R.string.number_picker_drink_quantity_header);
                totalNumberCount = 10;
                cupPicker.setVisibility(View.VISIBLE);
            } else if (pickerType == REQUEST_PICK_HOURS) {
                helpText.setText(R.string.number_picker_hours_slept);
                totalNumberCount = 24;
                cupPicker.setVisibility(View.GONE);
            }
        } catch (Exception e) {
            Log.e(NumberPickerActivity.class.getSimpleName(), "Error: " + e.getMessage());
        }

        String[] nums = new String[totalNumberCount];
        for (int i = 0; i < nums.length; i++)
            nums[i] = Integer.toString(i+1);
        numberPicker.setMinValue(1);
        numberPicker.setMaxValue(totalNumberCount);
        numberPicker.setDisplayedValues(nums);
        numberPicker.setValue(1);
    }

    public void onNumberPickerConfirmClick(View view) {
        int numberToBeSent = 0;
        if (pickerType == REQUEST_PICK_DRINK_QUANTITY) {
            int perCupVolume = 0;
            switch (cupPicker.getValue()) {
                case 2:
                    perCupVolume = 400;
                    break;
                case 3:
                    perCupVolume = 500;
                    break;
                case 1:
                default:
                    perCupVolume = 280;
                    break;
            }
            numberToBeSent = perCupVolume * numberPicker.getValue();
        } else {
            numberToBeSent = numberPicker.getValue();
        }

        if (CircogPrefs.DEBUG_MODE) {
            Log.i(TAG, "Picked number: " + numberToBeSent);
        }

        Intent intent = getIntent();
        intent.putExtra(RESULT_PICKED_NUMBER, numberToBeSent);
        setResult(RESULT_OK, intent);
        finish();
    }
}
