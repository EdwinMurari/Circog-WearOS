package org.hcilab.circog_watch;

import android.os.Bundle;
import android.support.wearable.activity.WearableActivity;

public class GenderSelectionActivity extends WearableActivity {

    public static final String RESULT_GENDER_SELECTION = "resultGenderSelection";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gender_selection);

        // Enables Always-on
        setAmbientEnabled();
    }
}
