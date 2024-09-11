package it.pagopa.pn.apikey.manager.config;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = PnApikeyManagerConfig.class)
class PnApikeyManagerConfigTest {

    @Autowired
    private PnApikeyManagerConfig pnApikeyManagerConfig;

    @BeforeEach
    void setUp() {
        PnApikeyManagerConfig.Dao dao = new PnApikeyManagerConfig.Dao();
        dao.setPublicKeyTableName("publicKeyTable");
        pnApikeyManagerConfig.setDao(dao);
        PnApikeyManagerConfig.Sqs sqs = new PnApikeyManagerConfig.Sqs();
        sqs.setInternalQueueName("internalQueue");
        pnApikeyManagerConfig.setSqs(sqs);

        PnApikeyManagerConfig.AttributeResolver attributeResolver = new PnApikeyManagerConfig.AttributeResolver();
        attributeResolver.setName("resolverName");
        attributeResolver.setCfg(Map.of("purposes", List.of("purpose1", "purpose2")));
        pnApikeyManagerConfig.setAttributeResolversCfgs(List.of(attributeResolver));

        pnApikeyManagerConfig.setJwksCacheMaxDurationSec(3600);
        pnApikeyManagerConfig.setJwksCacheRenewSec(300);
    }
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
        assertEquals("internalQueue", pnApikeyManagerConfig.getSqs().getInternalQueueName());
    }

    @Test
    void testDaoConfig() {
        assertNotNull(pnApikeyManagerConfig);
        assertNotNull(pnApikeyManagerConfig.getDao());
        assertEquals("publicKeyTable", pnApikeyManagerConfig.getDao().getPublicKeyTableName());
    }

    @Test
    void testAttributeResolversConfig() {
        assertNotNull(pnApikeyManagerConfig);
        assertNotNull(pnApikeyManagerConfig.getAttributeResolversCfgs());
        assertFalse(pnApikeyManagerConfig.getAttributeResolversCfgs().isEmpty());

        PnApikeyManagerConfig.AttributeResolver resolver = pnApikeyManagerConfig.getAttributeResolversCfgs().get(0);
        assertNotNull(resolver);
        assertEquals("resolverName", resolver.getName());
        assertNotNull(resolver.getCfg());
        assertEquals(List.of("purpose1", "purpose2"), resolver.getCfg().get("purposes"));
    }
}