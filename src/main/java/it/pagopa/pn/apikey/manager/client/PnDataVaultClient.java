package it.pagopa.pn.apikey.manager.client;


import it.pagopa.pn.apikey.manager.generated.openapi.msclient.pndatavault.v1.api.RecipientsApi;
import it.pagopa.pn.apikey.manager.generated.openapi.msclient.pndatavault.v1.dto.BaseRecipientDtoDto;
import it.pagopa.pn.apikey.manager.generated.openapi.msclient.pndatavault.v1.dto.RecipientTypeDto;
import it.pagopa.pn.commons.log.PnLogger;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

/**
 * Classe wrapper di pn-data-vault, con gestione del backoff
 */
@Component
@lombok.CustomLog
public class PnDataVaultClient {

    private final RecipientsApi recipientsApi;

    public PnDataVaultClient(RecipientsApi recipientsApi) {
        this.recipientsApi = recipientsApi;
    }


    /**
     * Ritorna una lista di nominativi in base alla lista di iuid passati
     *
     * @param internalIds lista di iuid
     * @return lista di nominativi
     */
    public Flux<BaseRecipientDtoDto> getRecipientDenominationByInternalId(List<String> internalIds) {
        log.logInvokingExternalService(PnLogger.EXTERNAL_SERVICES.PN_DATA_VAULT, "Opaque Ids Resolution");
        return recipientsApi.getRecipientDenominationByInternalId(internalIds);

    }

    /**
     * Genera (o recupera) un internaluserId in base al CF/PIVA
     *
     * @param isPerson   true per PF, false per PG
     * @param fiscalCode CF o PIVA
     * @return iuid
     */
    public Mono<String> ensureRecipientByExternalId(boolean isPerson, String fiscalCode) {
        log.logInvokingExternalService(PnLogger.EXTERNAL_SERVICES.PN_DATA_VAULT, "Opaque Id Creation");
        return recipientsApi.ensureRecipientByExternalId(isPerson ? RecipientTypeDto.PF : RecipientTypeDto.PG, fiscalCode);

    }

}
