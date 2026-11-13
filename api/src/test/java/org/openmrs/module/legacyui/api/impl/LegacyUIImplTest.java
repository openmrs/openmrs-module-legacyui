package org.openmrs.module.legacyui.api.impl;

import org.junit.jupiter.api.Test;
import org.openmrs.User;

import static org.junit.jupiter.api.Assertions.assertThrows;

public class LegacyUIImplTest {

    @Test
    void shouldThrowExceptionIfUserIsNull() {

        LegacyUIImpl service = new LegacyUIImpl();

        assertThrows(IllegalArgumentException.class, () -> {
            service.getProviderForUser(null);
        });
    }

    @Test
    void shouldThrowExceptionIfProviderNotFound() {

        LegacyUIImpl service = new LegacyUIImpl();

        User user = new User();

        assertThrows(IllegalStateException.class, () -> {
            service.getProviderForUser(user);
        });
    }
}