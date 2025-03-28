package dev.hv.database.services;

import dev.hv.database.DatabaseConnection;
import dev.hv.database.DbTestHelper;
import dev.hv.model.classes.Authentification.AuthUser;
import dev.hv.model.classes.Authentification.AuthUserPermissions;
import dev.hv.model.classes.Customer;
import dev.hv.model.enums.UserPermissions;
import dev.hv.model.enums.UserRoles;
import dev.hv.model.interfaces.ICustomer;
import dev.provider.ServiceProvider;
import jakarta.validation.constraints.AssertTrue;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.security.Permissions;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

class AuthUserServiceTest
{
    private DatabaseConnection _mockConnection;
    private Customer _customer;
    private AuthUser _user;


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
        _customer = new Customer(UUID.randomUUID(), "Peter", "Parker", LocalDate.now(), ICustomer.Gender.M);
        _user = new AuthUser();
        _user.setId(_customer.getId());
        _user.setPassword("SuperGeheim12!");
        _user.setRole(UserRoles.USER);
        _user.setUsername("PP");
        _user.setPermissions(new ArrayList<>(List.of(UserPermissions.READ, UserPermissions.WRITE)));

        try (DatabaseConnection dbCon = ServiceProvider.Services.getDatabaseConnection())
        {
            dbCon.removeAllTables();
            dbCon.createAllTablesWithAuth();
        }
    }

    @Test
    void add() throws SQLException, ReflectiveOperationException, IOException
    {
        try (AuthUserService aus = ServiceProvider.getAuthUserService())
        {
            assertThrows(IllegalArgumentException.class, () -> aus.add(null));
            var res = aus.add(_user);
            assertEquals(_user, res);

            try (UserPermissionService ups = ServiceProvider.getUserPermissionService())
            {
                var perRes = ups.getAllById(_user.getId());
                var userPermissions = _user.getPermissions().stream().map(x -> new AuthUserPermissions(_user.getId(), x)).toList();
                assertEquals(userPermissions, perRes);
            }
        }
    }

    @Test
    void update() throws SQLException, ReflectiveOperationException, IOException
    {
        var password = CryptoService.hashStringWithSalt(_user.getPassword());

        try (AuthUserService aus = ServiceProvider.getAuthUserService())
        {
            assertThrows(IllegalArgumentException.class, () -> aus.update(null));
            aus.add(_user);
            _user.setUsername("PeterParker");
            var res = aus.update(_user);
            _user.setPassword(password);
            assertEquals(_user, res);

            _user.setPassword(null);
            var res2 = aus.update(_user);
            assertEquals(_user, res2);
        }
    }

    @Test
    void remove() throws SQLException, ReflectiveOperationException, IOException
    {
        try (AuthUserService aus = ServiceProvider.getAuthUserService())
        {
            assertThrows(IllegalArgumentException.class, () -> aus.remove(null));
            aus.add(_user);
            aus.remove(_user);
            assertNull(aus.getById(_user.getId()));
        }
    }

    @Test
    void displayNameAvailable() throws ReflectiveOperationException, SQLException, IOException
    {
        try (AuthUserService aus = ServiceProvider.getAuthUserService())
        {
            aus.add(_user);
            assertFalse(aus.DisplayNameAvailable(_user.getUsername()));
            assertTrue(aus.DisplayNameAvailable("PeterParker"));
        }
    }

    @Test
    void getUserByName() throws SQLException, IOException, ReflectiveOperationException
    {
        try (AuthUserService aus = ServiceProvider.getAuthUserService())
        {
            aus.add(_user);
            var res = aus.getUserByName(_user.getUsername());
            assertEquals(_user, res);
        }
    }

    @Test
    void create() throws SQLException
    {
        var mockInternalConnection = mock(Connection.class);
        when(_mockConnection.getConnection()).thenReturn(mockInternalConnection);

        // Just for coverage
        when(mockInternalConnection.isClosed()).thenReturn(false);
        var aus = new AuthUserService(_mockConnection);

        when(mockInternalConnection.isClosed()).thenReturn(true);
        aus = new AuthUserService(_mockConnection);
        //

        when(mockInternalConnection.isClosed()).thenThrow(SQLException.class);
        assertThrows(RuntimeException.class, () -> new AuthUserService(_mockConnection));


    }

    @Test
    void createNewAuthInformation() throws SQLException, IOException, ReflectiveOperationException
    {
        var res = AuthUserService.CreateNewAuthInformation(_customer);
        assertNotNull(res.getUsername());
        assertTrue(res.getUsername().startsWith(_customer.getFirstName().toLowerCase() + "_"));
        assertTrue(res.getUsername().contains("_" + _customer.getLastName().toLowerCase() + "_"));
    }

    @Test
    void getByUserName() throws ReflectiveOperationException, SQLException, IOException
    {
        try (AuthUserService aus = ServiceProvider.getAuthUserService())
        {
            aus.add(_user);
            var res = aus.getByUserName(_user.getUsername());
            assertEquals(_user, res);
        }
    }


}