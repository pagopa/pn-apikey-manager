package it.pagopa.pn.apikey.manager.constant;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ProcessStatus {

    public static final String PROCESS_NAME_AGGREGATION_GET_AGGREGATE = "[AGGREGATION] get aggregate";
    public static final String PROCESS_NAME_AGGREGATION_LIST_GET_AGGREGATE = "[AGGREGATION] get aggregate list";
    public static final String PROCESS_NAME_AGGREGATION_GET_PA_AGGREGATE = "[AGGREGATION] get pa aggregation";
    public static final String PROCESS_NAME_AGGREGATION_MOVE_PA = "[AGGREGATION] move pa";
    public static final String PROCESS_NAME_AGGREGATION_ADD_PA_LIST_TO_AGGREGATE = "[AGGREGATION] add pa list to aggregate";
    public static final String PROCESS_NAME_AGGREGATION_CREATE_AGGREGATE = "[AGGREGATION] create aggregate";
    public static final String PROCESS_NAME_AGGREGATION_DELETE_API_KEYS = "[AGGREGATION] delete api keys";
    public static final String PROCESS_NAME_AGGREGATION_GET_ASSOCIABLE_PA = "[AGGREGATION] get associable pa";
    public static final String PROCESS_NAME_AGGREGATION_UPDATE_AGGREGATE = "[AGGREGATION] get update aggregate";


    public static final String PROCESS_NAME_API_KEY_BO_GET_API_KEYS_BO = "[API KEY BO] get api keys bo";
    public static final String PROCESS_NAME_API_KEY_BO_INTEROP = "[API KEY BO] change value pdnd";


    public static final String PROCESS_NAME_API_KEY_CHANGE_STATUS_API_KEY = "[API KEY] change status api key";
    public static final String PROCESS_NAME_API_KEY_DELETE_API_KEY = "[API KEY] delete api key";
    public static final String PROCESS_NAME_API_KEY_GET_API_KEYS = "[API KEY] get api keys";
    public static final String PROCESS_NAME_API_KEY_NEW_API_KEY = "[API KEY] new api key";
    public static final String CHECKING_NAME_API_KEY_NEW_API_KEY = "[API KEY] checking groups to add";



    public static final String PROCESS_NAME_API_KEY_PRVT_CHANGE_VIRTUAL_KEY = "[API KEY PRVT] change virtual key";

    public static final String PROCESS_NAME_PA_GET_PA = "[PA] get pa";

    public static final String PROCESS_NAME_USAGE_PLAN_GET_USAGE_PLAN = "[USAGE PLAN] get usage plan";

    public static final String PROCESS_SERVICE_AGGREGATION_GET_ALL_PA = "[AGGREGATION] retrieving all pa";
    public static final String PROCESS_SERVICE_API_KEY_GET_PA_BY_ID = "[API KEY] retrieving pa";
    public static final String PROCESS_SERVICE_API_KEY_GET_PA_GROUPS_BY_ID = "[API KEY] retrieving pa groups";



}
