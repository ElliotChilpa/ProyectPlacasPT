package com.placaspt.logic;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class AuthManagerTest {

    @Test
    void testValidCredentials() {
        assertTrue(AuthManager.validate("admin", "1234"));
    }

    @Test
    void testInvalidCredentials() {
        assertFalse(AuthManager.validate("usuario", "contraseña"));
    }
}
