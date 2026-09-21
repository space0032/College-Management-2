package com.college.utils;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class WardenValidationTest {

    @Test
    void acceptsWellFormedEmails() {
        assertTrue(WardenValidation.isValidEmail("james@college.edu"));
        assertTrue(WardenValidation.isValidEmail("a.b+c@sub.example.org"));
    }

    @Test
    void rejectsMalformedOrMissingEmails() {
        assertFalse(WardenValidation.isValidEmail(""));
        assertFalse(WardenValidation.isValidEmail(null));
        assertFalse(WardenValidation.isValidEmail("not-an-email"));
        assertFalse(WardenValidation.isValidEmail("user@nowhere"));
    }

    @Test
    void phoneMayBeBlankButMustBePlausibleOtherwise() {
        assertTrue(WardenValidation.isValidPhone(""));
        assertTrue(WardenValidation.isValidPhone(null));
        assertTrue(WardenValidation.isValidPhone("9876550001"));
        assertTrue(WardenValidation.isValidPhone("+91 98765 50001"));
        assertFalse(WardenValidation.isValidPhone("abc"));
        assertFalse(WardenValidation.isValidPhone("1"));
    }

    @Test
    void trimToNullCollapsesBlankValues() {
        assertNull(WardenValidation.trimToNull("   "));
        assertNull(WardenValidation.trimToNull(null));
        assertEquals("james@college.edu", WardenValidation.trimToNull("  james@college.edu  "));
    }
}