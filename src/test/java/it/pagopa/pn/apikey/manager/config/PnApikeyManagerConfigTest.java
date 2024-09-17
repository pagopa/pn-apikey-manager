package it.pagopa.pn.apikey.manager.config;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(SpringExtension.class)
@EnableConfigurationProperties(value = PnApikeyManagerConfig.class)
@TestPropertySource("classpath:application-test.properties")
class PnApikeyManagerConfigTest {

    @Autowired
    private PnApikeyManagerConfig pnApikeyManagerConfig;

    @Test
    void testJwksCacheMaxDurationSec() {
        assertNotNull(pnApikeyManagerConfig);
        assertEquals(3600, pnApikeyManagerConfig.getJwksCacheMaxDurationSec());
    }

    @Test
    void testJwksCacheRenewSec() {
        assertNotNull(pnApikeyManagerConfig);
        assertEquals(300, pnApikeyManagerConfig.getJwksCacheRenewSec());
    }

    @Test
    void testSqsConfig() {
        assertNotNull(pnApikeyManagerConfig);
        assertNotNull(pnApikeyManagerConfig.getSqs());
        assertEquals("pn-apikey_manager_internal_queue", pnApikeyManagerConfig.getSqs().getInternalQueueName());
    }

    @Test
    void testDaoConfig() {
        assertNotNull(pnApikeyManagerConfig);
        assertNotNull(pnApikeyManagerConfig.getDao());
        assertEquals("pn-publicKey", pnApikeyManagerConfig.getDao().getPublicKeyTableName());
    }

    @Test
    void testAttributeResolversConfig() {
        List<PnApikeyManagerConfig.AttributeResolver> attributeResolvers = pnApikeyManagerConfig.retrieveAttributeResolvers();
        assertNotNull(attributeResolvers);
        assertEquals(2, attributeResolvers.size());

    }
}