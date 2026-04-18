package org.openmrs.module.legacyui;

import org.junit.Test;
import static org.junit.Assert.*;


public class GeneralUtilsTest {

    @Test
    public void isValidUuidFormat_shouldReturnTrueForValidUuid() {
        String uuid = "a3e12268-74bf-11df-9768-17cfc9833272";
        assertTrue(GeneralUtils.isValidUuidFormat(uuid));
    }

    @Test
    public void isValidUuidFormat_shouldReturnFalseForShortUuid() {
        String uuid = "12345";
        assertFalse(GeneralUtils.isValidUuidFormat(uuid));
    }

    @Test
    public void isValidUuidFormat_shouldReturnFalseForUuidWithSpaces() {
        String uuid = "a3e12268 74bf 11df 9768 17cfc9833272";
        assertFalse(GeneralUtils.isValidUuidFormat(uuid));
    }
}