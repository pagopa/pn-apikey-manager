package it.pagopa.pn.apikey.manager.constant;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.NONE)
public final class ApiKeyConstant {
    public static final String PK = "id";
    public static final String LAST_UPDATE = "lastUpdate";
    public static final String GROUPS = "groups";
    public static final String PA_ID = "x-pagopa-pn-cx-id";
    public static final String GSI_VK = "virtualKey-id-index";
    public static final String GSI_PA = "paId-lastUpdate-index";
}
