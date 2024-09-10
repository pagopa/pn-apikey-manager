package it.pagopa.pn.apikey.manager.middleware.queue.consumer.event;

import it.pagopa.pn.api.dto.events.GenericEvent;
import it.pagopa.pn.api.dto.events.StandardEventHeader;
import lombok.*;


import javax.validation.constraints.NotEmpty;

@Getter
@Builder
@ToString
@EqualsAndHashCode
@NoArgsConstructor
@AllArgsConstructor
public class PublicKeyEvent implements GenericEvent<StandardEventHeader, PublicKeyEvent.Payload> {

    private StandardEventHeader header;

    private Payload payload;

    @Getter
    @Builder
    @ToString
    @EqualsAndHashCode
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Payload {

        @NotEmpty
        private String cxId;

        @NotEmpty
        private String kid;

        @NotEmpty
        private String action;
    }

}
