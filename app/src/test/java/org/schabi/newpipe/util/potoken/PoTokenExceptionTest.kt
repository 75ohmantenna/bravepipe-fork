package org.schabi.newpipe.util.potoken

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class PoTokenExceptionTest {
    @Test
    fun `JavaScript syntax errors do not expose their input`() {
        val sensitiveInput = "SyntaxError: visitor_data=secret"

        val exception = buildExceptionForJsError(sensitiveInput)

        assertTrue(exception is BadWebViewException)
        assertEquals("JavaScript syntax error", exception.message)
        assertFalse(exception.message!!.contains(sensitiveInput))
    }

    @Test
    fun `JavaScript execution errors do not expose their input`() {
        val sensitiveInput = "token=secret"

        val exception = buildExceptionForJsError(sensitiveInput)

        assertTrue(exception is PoTokenException)
        assertEquals("JavaScript execution failed", exception.message)
        assertFalse(exception.message!!.contains(sensitiveInput))
    }
}
