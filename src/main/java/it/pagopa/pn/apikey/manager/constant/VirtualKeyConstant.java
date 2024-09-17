package it.pagopa.pn.apikey.manager.constant;

import it.pagopa.pn.apikey.manager.generated.openapi.server.v1.dto.CxTypeAuthFleetDto;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.Set;

@NoArgsConstructor(access = AccessLevel.NONE)
public class VirtualKeyConstant {

    public static final Set<CxTypeAuthFleetDto> ALLOWED_CX_TYPE_VIRTUAL_KEY = Set.of(CxTypeAuthFleetDto.PG);
}
