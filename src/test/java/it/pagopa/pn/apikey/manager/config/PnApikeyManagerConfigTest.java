package it.pagopa.pn.apikey.manager.config;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.junit.jupiter.api.Assertions.*;

@ContextConfiguration(classes = PnApikeyManagerConfig.class)
@ExtendWith(SpringExtension.class)
class PnApikeyManagerConfigTest {

    @Autowired
    private PnApikeyManagerConfig pnApikeyManagerConfig;

    /**
     * Method under test: {@link PnApikeyManagerConfig#canEqual(Object)}
     */
    @Test
    void testCanEqual() {
        assertFalse(pnApikeyManagerConfig.canEqual("Other"));
        assertTrue(pnApikeyManagerConfig.canEqual(pnApikeyManagerConfig));
    }

    /**
     * Methods under test:
     *
     * <ul>
     *   <li>{@link PnApikeyManagerConfig#equals(Object)}
     *   <li>{@link PnApikeyManagerConfig#hashCode()}
     * </ul>
     */
    @Test
    void testEquals2() {
        PnApikeyManagerConfig pnApikeyManagerConfig = new PnApikeyManagerConfig();
        assertEquals(pnApikeyManagerConfig, pnApikeyManagerConfig);
        int expectedHashCodeResult = pnApikeyManagerConfig.hashCode();
        assertEquals(expectedHashCodeResult, pnApikeyManagerConfig.hashCode());
    }

    /**
     * Methods under test:
     *
     * <ul>
     *   <li>{@link PnApikeyManagerConfig#equals(Object)}
     *   <li>{@link PnApikeyManagerConfig#hashCode()}
     * </ul>
     */
    @Test
    void testEquals3() {
        PnApikeyManagerConfig pnApikeyManagerConfig = new PnApikeyManagerConfig();
        PnApikeyManagerConfig pnApikeyManagerConfig1 = new PnApikeyManagerConfig();
        assertEquals(pnApikeyManagerConfig, pnApikeyManagerConfig1);
        int expectedHashCodeResult = pnApikeyManagerConfig.hashCode();
        assertEquals(expectedHashCodeResult, pnApikeyManagerConfig1.hashCode());
    }
}

