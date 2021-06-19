package com.msa.onewaycoroutines

import com.msa.onewaycoroutines.utilities.isData
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class MutabilityHelperTest {

    @Test
    fun isData() {
        assertTrue(TestDataClass::class.java.isData)
        assertFalse(String::class.java.isData)
    }

    data class TestDataClass(
        internal val foo: Int,
    )
}

