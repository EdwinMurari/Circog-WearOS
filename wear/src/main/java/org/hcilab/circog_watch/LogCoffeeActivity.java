package org.hcilab.circog_watch;

import android.content.Intent;
import android.os.Bundle;
import android.support.wearable.activity.WearableActivity;
import android.view.View;
import android.widget.TextView;

import java.util.Objects;

public class LogCoffeeActivity extends WearableActivity {

    private static final int REQUEST_ALERTNESS_LEVEL = 0;
    private static final int REQUEST_DRINK = 1;
    private static final int REQUEST_DRINK_QUANTITY = 2;

    private int alertnessLevel = -1;
    private int selectedDrinkIndex = 0;
    private int drinkQuantity = 0;

    private TextView tv_chooseDrinkQuantity;
    private TextView tv_errorMessage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_log_coffee);

        // Enables Always-on
        setAmbientEnabled();

        tv_chooseDrinkQuantity = findViewById(R.id.tv_choose_drink_quantity);
        tv_errorMessage = findViewById(R.id.tv_error_message);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        try {
            super.onActivityResult(requestCode, resultCode, data);

            if (requestCode == REQUEST_ALERTNESS_LEVEL && resultCode == RESULT_OK) {

                alertnessLevel = data.getIntExtra(RatingActivity.RESULT_RADIO_GROUP_INDEX, 0);

            } else if (requestCode == REQUEST_DRINK && resultCode == RESULT_OK) {

                selectedDrinkIndex = data.getIntExtra(DrinkSelectionActivity.RESULT_SELECTED_DRINK, 0);
                tv_chooseDrinkQuantity.setVisibility(selectedDrinkIndex == 0 ? View.GONE : View.VISIBLE);
                drinkQuantity = selectedDrinkIndex == 0 ? 0 : -1;

            } else if (requestCode == REQUEST_DRINK_QUANTITY && resultCode == RESULT_OK) {

                drinkQuantity = data.getIntExtra(NumberPickerActivity.RESULT_PICKED_NUMBER, 0);

            }
        } catch (Exception ignored) {
        }
    }

    public void launchAlertnessLevelActivity(View view) {
        final Intent intent = new Intent(this, RatingActivity.class);
        intent.putExtra(RatingActivity.RESULT_RATE_TYPE, RatingActivity.REQUEST_RATE_ALERTNESS);
        startActivityForResult(intent, REQUEST_ALERTNESS_LEVEL);
    }

    public void launchDrinkSelection(View view) {
        final Intent intent = new Intent(this, DrinkSelectionActivity.class);
        startActivityForResult(intent, REQUEST_DRINK);
    }

    public void launchDrinkQuantitySelection(View view) {
        final Intent intent = new Intent(this, NumberPickerActivity.class);
        intent.putExtra(NumberPickerActivity.RESULT_TYPE_PICK, NumberPickerActivity.REQUEST_PICK_DRINK_QUANTITY);
        startActivityForResult(intent, REQUEST_DRINK_QUANTITY);
    }

    public void onSubmitSurveyClick(View view) {
        double caffeineInMg = 0;
        if (selectedDrinkIndex > 0 && CoffeeDataManager.getInstance().getCoffeeData(selectedDrinkIndex) != null)
            caffeineInMg = Util.getCaffeineContentFromDrink(Objects.requireNonNull(CoffeeDataManager.getInstance().getCoffeeData(selectedDrinkIndex)), drinkQuantity);

        if (alertnessLevel == -1) {

            tv_errorMessage.setText(R.string.task_survey_error_alert);
            tv_errorMessage.setVisibility(View.VISIBLE);

        } else if (selectedDrinkIndex == -1) {

            tv_errorMessage.setText(R.string.task_survey_error_drink_index);
            tv_errorMessage.setVisibility(View.VISIBLE);

        } else if (drinkQuantity == -1) {

            tv_errorMessage.setText(R.string.task_survey_error_drink_quantity);
            tv_errorMessage.setVisibility(View.VISIBLE);

        } else {
            LogManager.logCoffeeIntake(alertnessLevel, selectedDrinkIndex, drinkQuantity, caffeineInMg);
            finish();
        }
    }
}
