package it.pagopa.pn.apikey.manager.constant;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ProcessStatus {

    public static final String PROCESS_NAME_API_KEY_CHANGE_STATUS_API_KEY = "[API KEY] change status api key";
    public static final String CHECKING_NAME_API_KEY_NEW_API_KEY = "[API KEY] checking groups to add";

    public static final String PROCESS_SERVICE_AGGREGATION_GET_ALL_PA = "[AGGREGATION] retrieving all pa";
    public static final String PROCESS_SERVICE_API_KEY_GET_PA_BY_ID = "[API KEY] retrieving pa";
    public static final String PROCESS_SERVICE_API_KEY_GET_PA_GROUPS_BY_ID = "[API KEY] retrieving pa groups";
    public static final String PROCESS_SERVICE_API_KEY_GET_PG_USER_DETAILS = "[API KEY] retrieving pg user details";



}
