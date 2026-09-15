package com.example.woldeokmoneyverse.util

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class MoneyFormatTest {
    @Test fun groupsWholeWldAmountsWithoutPrecisionLoss() {
        assertEquals("0", formatMoneyAmount("0"))
        assertEquals("10,773", formatMoneyAmount("10773"))
        assertEquals("1,000,000", formatMoneyAmount("1000000"))
        assertEquals("100,000,000,000,000,000,000", formatMoneyAmount("100000000000000000000"))
        assertEquals("-12,345", formatMoneyAmount("-12345"))
    }

    @Test fun keepsOptionalFractionGrouped() {
        assertEquals("1,234.5", formatMoneyAmount("1234.500"))
    }

    @Test fun canonicalInputIsNotLimitedByLong() {
        assertEquals("100000000000000000000", canonicalPositiveMoneyInput("100,000,000,000,000,000,000"))
        assertNull(canonicalPositiveMoneyInput("0"))
        assertNull(canonicalPositiveMoneyInput("1.5"))
    }
}
