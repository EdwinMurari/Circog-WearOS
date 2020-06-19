package org.hcilab.circog_watch;

public interface CircogPrefs {

    // MAIN
    boolean DEBUG_MODE  = false;
    boolean PROVIDE_FEEDBACK = false;
    int MAX_DAILY_TASKS = 6;
    int MIN_STUDY_DAYS = 8;
    boolean RESTART_SERVICE_AFTER_REBOOT = true;
    String  PREFERENCES_NAME = "CircogPreferences";

    //STRINGS
    String PREF_UID				                = "uid";
    String CURRENT_VERSION_CODE                 = "CurrentVersionCode";
    String PREF_CONSENT_GIVEN                   = "Consent given";
    String DEMOGRAPHICS_PROVIDED                = "Demographics provided";
    String PREF_REGISTRATION_TIMESTAMP          = "registered_timestamp";
    String CURRENT_TASK                         = "current_task";
    String TASK_SEQUENCE                        = "tasks_sequence";
    String TASKLIST_DELIMITER                   = ", ";

    String DAILY_TASK_COUNT                     = "daily_task_count";
    String DATE_LAST_TASK_COMPLETED             = "day_last_task_completed";

    // Email and demographics
    String	PREF_EMAIL							= "email";
    String	PREF_AGE							= "age";
    String	PREF_GENDER_POS						= "gender_pos";
    String	PREF_GENDER 						= "gender";
    String	PREF_PROFESSION						= "profession";

    //DAILY Survey
    String LAST_WAKEUP_HOUR                     = "last_wakeup_hour";
    String LAST_WAKEUP_MINUTE                   = "last_wakeup_minute";
    String LAST_WAKEUP_SET                      = "last_wakeup_set";
    String DATE_LAST_DAILY_SURVEY_MS            = "last_daily_survey_ms";
    String LAST_HOURS_SLEPT                     = "last_hours_slept";

    //TASK Survey
    String LEVEL_ALERTNESS                      = "alertness";
    String CAFFEINATED                          = "caffeinated";
    String CAFFEINATED_DRINK_INDEX              = "caffeinated_drink_index";
    String CAFFEINATED_DRINK_QUANTITY           = "caffeinated_drink_quantity";
    String CAFFEINATED_AMOUNT_IN_MG             = "caffeinated_drink_in_mg";

    //FORMATTING
    String	CRLF						= "\r\n";

    //WEAR DATA PATH
    String CONSENT_RESULT_PATH = "/consent_result";
    String CONSENT_TIME_KEY = "time";
    String CONSENT_SUCCESS_BOOL_KEY = "consent_isSuccess";

    String DEMOGRAPHIC_RESULT_PATH = "/demographic_result";
    String DEMOGRAPHIC_PROVIDED_KEY = "demographic_provided";
    String DEMOGRAPHIC_AGE_KEY = "age";
    String DEMOGRAPHIC_GENDER_KEY = "gender";
    String DEMOGRAPHIC_GENDER_POS_KEY = "gender_pos";
    String DEMOGRAPHIC_PROFESSION_KEY = "profession";
    String DEMOGRAPHIC_EMAIL_KEY = "email";

    String TASK_DETAILS_PATH ="/task_details_path";
    String TASK_COUNT_KEY = "task_count";
    String LAST_TASK_TIME_KEY = "last_task_time";
}