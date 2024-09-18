package it.pagopa.pn.apikey.manager.constant;

import it.pagopa.pn.apikey.manager.generated.openapi.server.v1.dto.CxTypeAuthFleetDto;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.Set;

@NoArgsConstructor(access = AccessLevel.NONE)
public final class PublicKeyConstant {

    public static final Set<CxTypeAuthFleetDto> ALLOWED_CX_TYPE_PUBLIC_KEY = Set.of(CxTypeAuthFleetDto.PG);
    public static final String KID = "kid";
    public static final String CREATED_AT = "createdAt";
    public static final String CXID = "cxId";
}
