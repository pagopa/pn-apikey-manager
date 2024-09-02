package it.pagopa.pn.apikey.manager.config;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.junit.jupiter.api.Assertions.*;

@ContextConfiguration(classes = PnApikeyManagerUsagePlanConfig.class)
@ExtendWith(SpringExtension.class)
class PnApikeyManagerUsagePlanConfigTest {

    @Autowired
    private PnApikeyManagerUsagePlanConfig pnApikeyManagerUsagePlanConfig;

    /**
     * Method under test: {@link PnApikeyManagerUsagePlanConfig#canEqual(Object)}
     */
    @Test
    void testCanEqual() {
        assertFalse(pnApikeyManagerUsagePlanConfig.canEqual("Other"));
        assertTrue(pnApikeyManagerUsagePlanConfig.canEqual(pnApikeyManagerUsagePlanConfig));
    }

    /**
     * Methods under test:
     *
     * <ul>
     *   <li>{@link PnApikeyManagerUsagePlanConfig#equals(Object)}
     *   <li>{@link PnApikeyManagerUsagePlanConfig#hashCode()}
     * </ul>
     */
    @Test
    void testEquals2() {
        PnApikeyManagerUsagePlanConfig pnApikeyManagerUsagePlanConfigTest = new PnApikeyManagerUsagePlanConfig();
        assertEquals(pnApikeyManagerUsagePlanConfig, pnApikeyManagerUsagePlanConfigTest);
        int expectedHashCodeResult = pnApikeyManagerUsagePlanConfig.hashCode();
        assertEquals(expectedHashCodeResult, pnApikeyManagerUsagePlanConfigTest.hashCode());
    }

    /**
     * Methods under test:
     *
     * <ul>
     *   <li>{@link PnApikeyManagerUsagePlanConfig#equals(Object)}
     *   <li>{@link PnApikeyManagerUsagePlanConfig#hashCode()}
     * </ul>
     */
    @Test
    void testEquals3() {
        PnApikeyManagerUsagePlanConfig pnApikeyManagerUsagePlanConfigTest = new PnApikeyManagerUsagePlanConfig();
        PnApikeyManagerUsagePlanConfig pnApikeyManagerUsagePlanConfigTest2 = new PnApikeyManagerUsagePlanConfig();
        assertEquals(pnApikeyManagerUsagePlanConfigTest, pnApikeyManagerUsagePlanConfigTest2);
        int expectedHashCodeResult = pnApikeyManagerUsagePlanConfigTest.hashCode();
        assertEquals(expectedHashCodeResult, pnApikeyManagerUsagePlanConfigTest2.hashCode());
    }
}

