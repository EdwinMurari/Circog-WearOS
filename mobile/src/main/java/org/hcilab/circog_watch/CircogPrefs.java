package org.hcilab.circog_watch;

public interface CircogPrefs {

    // MAIN
    boolean DEBUG_MODE  = false;
    String  PREFERENCES_NAME = "CircogPreferences";
    int MAX_DAILY_TASKS = 6;
    int MIN_STUDY_DAYS = 8;
    boolean RESTART_SERVICE_AFTER_REBOOT = true;

    //STRINGS
    String PREF_CONSENT_GIVEN                   = "Consent given";

    String DAILY_TASK_COUNT                     = "daily_task_count";
    String DATE_LAST_TASK_COMPLETED             = "day_last_task_completed";

    //Notifications
    String	NOTIF_CLICKED     					= "notif_clicked";
    String	NOTIF_POSTED    					= "notif_posted";
    String	NOTIF_POSTED_MILLIS					= "notif_posted_millis";
    String  LAST_NOTIFICATION_POSTED_MS         = "last_notif_posted_ms";

    // WEAR DATA ITEMS
    String CONSENT_RESULT_PATH = "/consent_result";
    String CONSENT_TIME_KEY = "time";
    String CONSENT_SUCCESS_BOOL_KEY = "consent_isSuccess";

    String DEMOGRAPHIC_RESULT_PATH = "/demographic_result";
    String DEMOGRAPHIC_AGE_KEY = "age";
    String DEMOGRAPHIC_GENDER_KEY = "gender";
    String DEMOGRAPHIC_GENDER_POS_KEY = "gender_pos";
    String DEMOGRAPHIC_PROFESSION_KEY = "profession";
    String DEMOGRAPHIC_EMAIL_KEY = "email";

    String TASK_DETAILS_PATH ="/task_details_path";
    String TASK_COUNT_KEY = "task_count";
    String LAST_TASK_TIME_KEY = "last_task_time";
}