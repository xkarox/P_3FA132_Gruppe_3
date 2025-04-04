package dev.hv.model.enums;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class UserPermissionsTest
{
    @Test
    void translateHttpToUserPermission()
    {
        assertEquals(UserPermissions.WRITE, UserPermissions.translateHttpToUserPermission("POST"));
        assertEquals(UserPermissions.READ, UserPermissions.translateHttpToUserPermission("GET"));
        assertEquals(UserPermissions.UPDATE, UserPermissions.translateHttpToUserPermission("PUT"));
        assertEquals(UserPermissions.DELETE, UserPermissions.translateHttpToUserPermission("DELETE"));
        assertNull(UserPermissions.translateHttpToUserPermission(null));
        assertNull(UserPermissions.translateHttpToUserPermission("PATCH"));
    }
}