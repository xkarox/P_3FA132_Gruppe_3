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
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;

import java.io.IOException;
import java.security.Permissions;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

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
}