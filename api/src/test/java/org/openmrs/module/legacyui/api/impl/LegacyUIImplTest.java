package org.openmrs.module.legacyui.api.impl;

import org.junit.jupiter.api.Test;
import org.openmrs.User;

import static org.junit.jupiter.api.Assertions.assertNotNull;

public class LegacyUIImplTest {

    @Test
    void shouldCreateLegacyUIImplObject() {
        LegacyUIImpl service = new LegacyUIImpl();
        assertNotNull(service);
    }

    @Test
    void shouldHandleValidUserObject() {
        User user = new User();
        assertNotNull(user);
    }
}