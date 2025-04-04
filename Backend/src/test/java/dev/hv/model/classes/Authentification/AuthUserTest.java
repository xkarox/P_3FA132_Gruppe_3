package dev.hv.model.classes.Authentification;

import dev.hv.database.DatabaseConnection;
import dev.hv.database.DbTestHelper;
import dev.hv.model.enums.UserPermissions;
import dev.hv.model.enums.UserRoles;
import dev.provider.ServiceProvider;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class AuthUserTest
{
    @BeforeAll
    static void OneTimeSetup() throws IOException, SQLException
    {
        DbTestHelper.LoadTestServiceProvider();
        try (DatabaseConnection dbCon = ServiceProvider.Services.getDatabaseConnection())
        {
            dbCon.removeAllTables();
            dbCon.createAllTablesWithAuth();
        }
    }

    @AfterAll
    static void OneTimeTearDown() throws SQLException, IOException
    {
        DbTestHelper.UnloadTestServiceProvider();
    }

    static AuthUser _authUser;
    @BeforeAll
    static void setUp()
    {
        _authUser = new AuthUser();
        _authUser.setId(UUID.randomUUID());
        _authUser.setUsername("test");
        _authUser.setPassword("testPassword");
        _authUser.setRole(UserRoles.ADMIN);
        _authUser.setPermissions(new ArrayList<>(List.of(UserPermissions.READ, UserPermissions.WRITE)));
    }

    @Test
    void constructor()
    {
        var blankUser = new AuthUser();
        AuthUser authUser = new AuthUser();
        assertNotNull(authUser);

        authUser = new AuthUser(_authUser.getId());
        blankUser.setId(_authUser.getId());
        assertEquals(blankUser, authUser);

        blankUser.setUsername(_authUser.getUsername());
        blankUser.setPassword(_authUser.getPassword());

        authUser = new AuthUser(_authUser.getId(), _authUser.getUsername(), _authUser.getPassword());
        assertEquals(blankUser, authUser);

        blankUser.setRole(_authUser.getRole());
        blankUser.setPermissions(_authUser.getPermissions());

        AuthUserDto userDto = new AuthUserDto();
        userDto.setId(_authUser.getId());
        userDto.setUsername(_authUser.getUsername());
        userDto.setPassword(_authUser.getPassword());
        userDto.setRole(_authUser.getRole());
        userDto.setPermissions(_authUser.getPermissions());

        authUser = new AuthUser(userDto);
        assertEquals(blankUser, authUser);

        userDto.setPermissions(null);
        blankUser.setPermissions(new ArrayList<>());
        authUser = new AuthUser(userDto);
        assertEquals(blankUser, authUser);

    }

    @Test
    void dbObjectFactory_withValidArgs_shouldSetFieldsCorrectly() throws Exception {
        UUID id = UUID.randomUUID();
        String username = "testUser";
        String password = "testPassword";
        int roleIndex = UserRoles.ADMIN.ordinal();
        Object[] args = {id.toString(), username, password, roleIndex};

        AuthUser authUser = new AuthUser();
        authUser.dbObjectFactory(args);

        assertEquals(id, authUser.getId());
        assertEquals(username, authUser.getUsername());
        assertEquals(password, authUser.getPassword());
        assertEquals(UserRoles.ADMIN, authUser.getRole());
    }

    @Test
    void equals()
    {
        var userDto = new AuthUserDto(_authUser);
        assertNotEquals(_authUser, userDto);
        assertEquals(_authUser, _authUser);
        assertNotEquals(_authUser, null);
        assertNotEquals(_authUser, new AuthUser());

        var authUser = new AuthUser();
        authUser.setId(_authUser.getId());
        assertNotEquals(_authUser, authUser);
        authUser.setUsername(_authUser.getUsername());
        assertNotEquals(_authUser, authUser);
        authUser.setRole(_authUser.getRole());
        assertNotEquals(_authUser, authUser);
        authUser.setPermissions(_authUser.getPermissions());
        assertEquals(_authUser, authUser);
    }
}