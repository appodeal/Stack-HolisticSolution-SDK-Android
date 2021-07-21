package com.explorestack.hs.sdk;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

@RunWith(AndroidJUnit4.class)
public class HSUtilsTest {

    private static final double DELTA = 1e-15;

    @Test
    public void parsePrice() {
        assertEquals(61.22, HSUtils.parsePrice("$61.22", "USD"), DELTA);
        assertEquals(2440.52, HSUtils.parsePrice("$2,440.52", "USD"), DELTA);
        assertEquals(4490.52, HSUtils.parsePrice("4 490,52 ₽", "RUB"), DELTA);
        assertEquals(3490000.89, HSUtils.parsePrice("$3,490,000.89", "USD"), DELTA);
        assertEquals(3490000.89, HSUtils.parsePrice("3 490 000,89 €", "EUR"), DELTA);
        assertEquals(3490000.89, HSUtils.parsePrice("3.490.000,89 €", "EUR"), DELTA);
        assertEquals(12345.67, HSUtils.parsePrice("₹12,345.67", "INR"), DELTA);
        assertEquals(12345.67, HSUtils.parsePrice("12,345.67 ₹", "INR"), DELTA);
        assertEquals(12345.67, HSUtils.parsePrice("12345.67₹", "INR"), DELTA);
        assertEquals(12345.67, HSUtils.parsePrice("12345.67 ₹", "INR"), DELTA);

        assertEquals(61, HSUtils.parsePrice("61", null), DELTA);
        assertEquals(4490, HSUtils.parsePrice("4490", null), DELTA);

        assertEquals(61.22, HSUtils.parsePrice("61.22", "USD"), DELTA);
        assertEquals(4490.52, HSUtils.parsePrice("4 490,52", "RUB"), DELTA);

        assertEquals(61.22, HSUtils.parsePrice("61.22", null), DELTA);
        assertEquals(4490.52, HSUtils.parsePrice("4490.52", null), DELTA);

        assertEquals(61.22, HSUtils.parsePrice("61.22", "-"), DELTA);
        assertEquals(4490.52, HSUtils.parsePrice("4490.52", "-"), DELTA);

        assertNull(HSUtils.parsePrice("$61.22", null));
        assertNull(HSUtils.parsePrice("4 490,00 ₽", null));

        assertNull(HSUtils.parsePrice("$61.22", "-"));
        assertNull(HSUtils.parsePrice("#4 490,00", "RUB"));
    }
}
