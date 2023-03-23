package it.pagopa.pn.apikey.manager.utils;

import static org.mockito.Mockito.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import it.pagopa.pn.apikey.manager.exception.ApiKeyManagerException;
import it.pagopa.pn.commons.log.PnAuditLogEvent;
import it.pagopa.pn.commons.log.PnAuditLogEventType;

import java.util.HashMap;



import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

class CheckExceptionUtilsTest {


    /**
     * Method under test: {@link CheckExceptionUtils#logAuditOnErrorOrWarnLevel(Throwable, PnAuditLogEvent)}
     */
    @Test
    void testLogAuditOnErrorOrWarnLevel() {
        Throwable throwable = new Throwable();
        PnAuditLogEvent pnAuditLogEvent = mock(PnAuditLogEvent.class);
        when(pnAuditLogEvent.generateFailure(any(),  any())).thenReturn(new PnAuditLogEvent(
                PnAuditLogEventType.AUD_ACC_LOGIN, new HashMap<>(), "Not all who wander are lost", "Arguments"));
        CheckExceptionUtils.logAuditOnErrorOrWarnLevel(throwable, pnAuditLogEvent);
        verify(pnAuditLogEvent).generateFailure(any(),  any());
    }

    /**
     * Method under test: {@link CheckExceptionUtils#logAuditOnErrorOrWarnLevel(Throwable, PnAuditLogEvent)}
     */
    @Test
    void testLogAuditOnErrorOrWarnLevel2() {
        ApiKeyManagerException throwable = new ApiKeyManagerException("An error occurred", HttpStatus.CONTINUE);
        PnAuditLogEvent pnAuditLogEvent = mock(PnAuditLogEvent.class);
        when(pnAuditLogEvent.generateWarning(any(),  any())).thenReturn(new PnAuditLogEvent(
                PnAuditLogEventType.AUD_ACC_LOGIN, new HashMap<>(), "Not all who wander are lost", "Arguments"));
        CheckExceptionUtils.logAuditOnErrorOrWarnLevel(throwable, pnAuditLogEvent);
        verify(pnAuditLogEvent).generateWarning(any(),  any());
    }


    /**
     * Method under test: {@link CheckExceptionUtils#logAuditOnErrorOrWarnLevel(Throwable, PnAuditLogEvent)}
     */
    @Test
    void testLogAuditOnErrorOrWarnLevel3() {
        Throwable throwable = new Throwable();
        PnAuditLogEvent pnAuditLogEvent = mock(PnAuditLogEvent.class);
        when(pnAuditLogEvent.log()).thenReturn(new PnAuditLogEvent(PnAuditLogEventType.AUD_ACC_LOGIN, new HashMap<>(),
                "Not all who wander are lost", "Arguments"));
        PnAuditLogEvent pnAuditLogEvent1 = mock(PnAuditLogEvent.class);
        when(pnAuditLogEvent1.generateFailure(any(),  any())).thenReturn(pnAuditLogEvent);
        CheckExceptionUtils.logAuditOnErrorOrWarnLevel(throwable, pnAuditLogEvent1);
        verify(pnAuditLogEvent1).generateFailure(any(),  any());
        verify(pnAuditLogEvent).log();
    }

    @Test
    void testLogAuditOnErrorOrWarnLevel4() {
        ApiKeyManagerException throwable = new ApiKeyManagerException("An error occurred", HttpStatus.CONTINUE);
        PnAuditLogEvent pnAuditLogEvent = mock(PnAuditLogEvent.class);
        when(pnAuditLogEvent.log()).thenReturn(new PnAuditLogEvent(PnAuditLogEventType.AUD_ACC_LOGIN, new HashMap<>(),
                "Not all who wander are lost", "Arguments"));
        PnAuditLogEvent pnAuditLogEvent1 = mock(PnAuditLogEvent.class);
        when(pnAuditLogEvent1.generateWarning(any(),  any())).thenReturn(pnAuditLogEvent);
        CheckExceptionUtils.logAuditOnErrorOrWarnLevel(throwable, pnAuditLogEvent1);
        verify(pnAuditLogEvent1).generateWarning(any(),  any());
        verify(pnAuditLogEvent).log();
    }
}

