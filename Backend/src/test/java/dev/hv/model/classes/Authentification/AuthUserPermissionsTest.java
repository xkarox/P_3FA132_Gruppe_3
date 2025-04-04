package dev.hv.model.classes.Authentification;

import dev.hv.model.enums.UserPermissions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class AuthUserPermissionsTest
{
    static AuthUserPermissions _authUserPermissions;
    @BeforeAll
    static void setUp()
    {
        _authUserPermissions = new AuthUserPermissions();
        _authUserPermissions.setId(UUID.randomUUID());
        _authUserPermissions.setPermission(UserPermissions.READ);
    }

    @Test
    void equals()
    {
        var userPermissions = new AuthUserPermissions();
        assertNotEquals(_authUserPermissions, null);
        assertNotEquals(_authUserPermissions, new Object());

        userPermissions.setId(UUID.randomUUID());
        assertNotEquals(_authUserPermissions, userPermissions);
        userPermissions.setId(_authUserPermissions.getId());
        userPermissions.setPermission(_authUserPermissions.getPermission());
        assertEquals(_authUserPermissions, userPermissions);
    }

    @Test
    void setId()
    {
        var userPermissions = new AuthUserPermissions();
        assertNull(userPermissions.getId());
        userPermissions.setId(UUID.randomUUID());
        assertNotNull(userPermissions.getId());
    }
}