package dev.hv.database.services;

import dev.hv.database.DatabaseConnection;
import dev.hv.database.DbTestHelper;
import dev.hv.model.classes.Authentification.AuthUserPermissions;
import dev.hv.model.enums.UserPermissions;
import dev.provider.ServiceProvider;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.mockito.Mockito.*;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class UserPermissionServiceTest
{

    private DatabaseConnection _mockConnection;
    private AuthUserPermissions _permission;


    @BeforeAll
    static void OneTimeSetup() throws IOException
    {
        DbTestHelper.LoadTestServiceProvider();
    }

    @AfterAll
    static void OneTimeTearDown() throws SQLException, IOException
    {
        DbTestHelper.UnloadTestServiceProvider();
    }

    @BeforeEach
    void SetUp() throws IOException, SQLException
    {
        _mockConnection = mock(DatabaseConnection.class);
        _permission = new AuthUserPermissions(UUID.randomUUID(), UserPermissions.READ);

        try (DatabaseConnection dbCon = ServiceProvider.Services.getDatabaseConnection())
        {
            dbCon.removeAllTables();
            dbCon.createAllTablesWithAuth();
        }
    }

    @Test
    void add() throws SQLException, ReflectiveOperationException, IOException
    {
        try (UserPermissionService ups = ServiceProvider.getUserPermissionService())
        {
            assertThrows(IllegalArgumentException.class, () -> ups.add(null));
            var res = ups.add(_permission);
            assertEquals(_permission, res);
        }
    }

    @Test
    void getAllById() throws SQLException, IOException, ReflectiveOperationException
    {
        var permissions = new AuthUserPermissions(_permission.getId(), UserPermissions.READ);

        try (UserPermissionService ups = ServiceProvider.getUserPermissionService())
        {
            assertNull(ups.getAllById(_permission.getId()));
            var res = ups.add(_permission);
            var queryRes = ups.getAllById(_permission.getId());
            assertEquals(1, queryRes.size());
            assertEquals(res, queryRes.getFirst());

            ups.add(permissions);
            var queryRes2 = ups.getAllById(_permission.getId());
            assertEquals(2, queryRes2.size());
            assertEquals(permissions, queryRes2.get(1));

            ups.add(new AuthUserPermissions(UUID.randomUUID(), UserPermissions.UPDATE));
            assertEquals(2, ups.getAllById(_permission.getId()).size());
        }
    }

    @Test
    void update() throws SQLException, IOException, ReflectiveOperationException
    {
        try (UserPermissionService ups = ServiceProvider.getUserPermissionService())
        {
            ups.add(_permission);
            _permission.setPermission(UserPermissions.UPDATE);

            assertThrows(IllegalArgumentException.class, () -> ups.update(null));

            ups.update(_permission);
            assertEquals(_permission, ups.getById(_permission.getId()));
        }
    }

    @Test
    void closeConnection() throws SQLException, IOException
    {
        UserPermissionService ups = ServiceProvider.getUserPermissionService();
        ups.close();
        assertTrue(ups._dbConnection.getConnection().isClosed());
    }

    @Test
    void constructorTest() throws SQLException, IOException
    {
        var mockDbCon = mock(Connection.class);

        when(_mockConnection.getConnection()).thenReturn(mockDbCon);
        when(mockDbCon.isClosed()).thenReturn(false);

        UserPermissionService ups = new UserPermissionService(_mockConnection);

        when(_mockConnection.openConnection()).thenReturn(null);
        when(mockDbCon.isClosed()).thenReturn(true);
        ups = new UserPermissionService(_mockConnection);
    }
}