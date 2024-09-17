package it.pagopa.pn.apikey.manager.constant;

import it.pagopa.pn.apikey.manager.generated.openapi.server.v1.dto.CxTypeAuthFleetDto;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.Set;

@NoArgsConstructor(access = AccessLevel.NONE)
public final class ApiKeyConstant {
    public static final String PK = "id";
    public static final String LAST_UPDATE = "lastUpdate";
    public static final String GROUPS = "groups";
    public static final String PA_ID = "x-pagopa-pn-cx-id";
    public static final String VIRTUAL_KEY = "virtualKey";
    public static final String UID = "x-pagopa-pn-uid";
    public static final String GSI_VK = "virtualKey-id-index";
    public static final String GSI_PA = "paId-lastUpdate-index";
    public static final String GSI_UID_CXID = "uid-cxId-index";

    public static final Set<CxTypeAuthFleetDto> ALLOWED_CX_TYPE = Set.of(CxTypeAuthFleetDto.PA);
    public static final Set<CxTypeAuthFleetDto> ALLOWED_CX_TYPE_VIRTUALKEY = Set.of(CxTypeAuthFleetDto.PG);
}
