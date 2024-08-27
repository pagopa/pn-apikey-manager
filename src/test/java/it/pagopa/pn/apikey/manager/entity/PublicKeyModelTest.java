package it.pagopa.pn.apikey.manager.entity;

import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;

class PublicKeyModelTest {

    @Test
    void publicKeyModelConstructorCopiesFieldsCorrectly() {
        PublicKeyModel original = new PublicKeyModel();
        original.setKid("kid");
        original.setName("name");
        original.setCorrelationId("correlationId");
        original.setPublicKey("publicKey");
        original.setExpireAt(Instant.now());
        original.setCreatedAt(Instant.now());
        original.setStatus("status");
        original.setCxId("cxId");
        original.setIssuer("issuer");
        original.setTtl(Instant.now());

        PublicKeyModel copy = new PublicKeyModel(original);

        assertEquals(original.getKid(), copy.getKid());
        assertEquals(original.getName(), copy.getName());
        assertEquals(original.getCorrelationId(), copy.getCorrelationId());
        assertEquals(original.getPublicKey(), copy.getPublicKey());
        assertEquals(original.getExpireAt(), copy.getExpireAt());
        assertEquals(original.getCreatedAt(), copy.getCreatedAt());
        assertEquals(original.getStatus(), copy.getStatus());
        assertEquals(original.getCxId(), copy.getCxId());
        assertEquals(original.getIssuer(), copy.getIssuer());
        assertEquals(original.getTtl(), copy.getTtl());
        assertEquals(original.getStatusHistory(), copy.getStatusHistory());
    }

    @Test
    void statusHistoryItemConstructorSetsFieldsCorrectly() {
        PublicKeyModel.StatusHistoryItem item = new PublicKeyModel.StatusHistoryItem();
        item.setChangeByDenomination("denomination");
        item.setDate(Instant.now());
        item.setStatus("status");

        assertEquals("denomination", item.getChangeByDenomination());
        assertNotNull(item.getDate());
        assertEquals("status", item.getStatus());
    }

    @Test
    void statusHistoryItemHandlesNullFields() {
        PublicKeyModel.StatusHistoryItem item = new PublicKeyModel.StatusHistoryItem();
        item.setChangeByDenomination(null);
        item.setDate(null);
        item.setStatus(null);

        assertNull(item.getChangeByDenomination());
        assertNull(item.getDate());
        assertNull(item.getStatus());
    }
}