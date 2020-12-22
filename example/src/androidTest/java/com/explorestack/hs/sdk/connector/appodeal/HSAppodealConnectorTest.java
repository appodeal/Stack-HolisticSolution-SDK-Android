package com.explorestack.hs.sdk.connector.appodeal;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

@RunWith(AndroidJUnit4.class)
public class HSAppodealConnectorTest {

    private static final double DELTA = 1e-15;
    private final HSAppodealConnector connector = new HSAppodealConnector();

    @Test
    public void parsePrice() {
        assertEquals(61.22, connector.parsePrice("$61.22", "USD"), DELTA);
        assertEquals(2440.52, connector.parsePrice("$2,440.52", "USD"), DELTA);
        assertEquals(4490.52, connector.parsePrice("4 490,52 ₽", "RUB"), DELTA);
        assertEquals(3490000.89, connector.parsePrice("$3,490,000.89", "USD"), DELTA);
        assertEquals(3490000.89, connector.parsePrice("3 490 000,89 €", "EUR"), DELTA);
        assertEquals(3490000.89, connector.parsePrice("3.490.000,89 €", "EUR"), DELTA);

        assertEquals(61.22, connector.parsePrice("61.22", "USD"), DELTA);
        assertEquals(4490.52, connector.parsePrice("4 490,52", "RUB"), DELTA);

        assertEquals(61.22, connector.parsePrice("61.22", null), DELTA);
        assertEquals(4490.52, connector.parsePrice("4490.52", null), DELTA);

        assertEquals(61.22, connector.parsePrice("61.22", "-"), DELTA);
        assertEquals(4490.52, connector.parsePrice("4490.52", "-"), DELTA);

        assertNull(connector.parsePrice("$61.22", null));
        assertNull(connector.parsePrice("4 490,00 ₽", null));

        assertNull(connector.parsePrice("$61.22", "-"));
        assertNull(connector.parsePrice("#4 490,00", "RUB"));
    }
}
