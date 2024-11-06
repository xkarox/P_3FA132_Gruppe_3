package ace.database.provider;

import ace.ErrorMessages;
import ace.database.DatabaseConnection;
import ace.database.DbHelperService;
import ace.database.DbTestHelper;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.*;

class ServiceProviderTest
{
    @BeforeAll
    static void OneTimeSetup() throws IOException
    {
        ServiceProvider.Services.dbConnectionPropertiesOverwrite(DbHelperService.loadProperties(DbTestHelper.loadTestDbProperties()));
    }

    @BeforeEach
    void beforeEach()
    {
        ServiceProvider.Services = new InternalServiceProvider(100, 10, 10);
    }

    @AfterAll
    static void afterAll()
    {
        ServiceProvider.Services = new InternalServiceProvider(100, 10, 10);
    }

    @Test
    void BasicTest() throws SQLException, IOException
    {
        assertOpenConnections(0, 0, 0);
        try(var dbCon = ServiceProvider.Services.getDatabaseConnection())
        {
            assertNotNull(dbCon);
            assertFalse(dbCon.getConnection().isClosed());
            assertOpenConnections(1, 0, 0);
        }
        try(var customerService = ServiceProvider.Services.getCustomerService())
        {
            assertNotNull(customerService);
            assertOpenConnections(1, 1, 0);
        }
        try(var readingService = ServiceProvider.Services.getReadingService())
        {
            assertNotNull(readingService);
            assertOpenConnections(1, 0, 1);
        }
        assertOpenConnections(0, 0, 0);
    }

    @Test
    void OverloadTest() throws SQLException, IOException
    {
        ServiceProvider.Services.configureMaxConnections(1, 0, 0);

        DatabaseConnection dbCon = ServiceProvider.Services.getDatabaseConnection();
        boolean exceptionTriggert = false;
        try
        {
            ServiceProvider.Services.getCustomerService();
        } catch (IllegalArgumentException e)
        {
            exceptionTriggert = true;
            assertTrue(e.getMessage().contains(String.valueOf(ErrorMessages.ServicesNotAvailable)));
        }
        assertTrue(exceptionTriggert, "Because the simFailedException should have been triggert");

        exceptionTriggert = false;
        try
        {
            ServiceProvider.Services.getReadingService();
        } catch (IllegalArgumentException e)
        {
            exceptionTriggert = true;
            assertTrue(e.getMessage().contains(String.valueOf(ErrorMessages.ServicesNotAvailable)));
        }
        assertTrue(exceptionTriggert, "Because the simFailedException should have been triggert");

        // Trigger other part of the if statement
        ServiceProvider.Services.configureMaxConnections(2, 0, 0);
        exceptionTriggert = false;
        try
        {
            ServiceProvider.Services.getCustomerService();
        } catch (IllegalArgumentException e)
        {
            exceptionTriggert = true;
            assertTrue(e.getMessage().contains(String.valueOf(ErrorMessages.ServicesNotAvailable)));
        }
        assertTrue(exceptionTriggert, "Because the simFailedException should have been triggert");

        exceptionTriggert = false;
        try
        {
            ServiceProvider.Services.getReadingService();
        } catch (IllegalArgumentException e)
        {
            exceptionTriggert = true;
            assertTrue(e.getMessage().contains(String.valueOf(ErrorMessages.ServicesNotAvailable)));
        }
        assertTrue(exceptionTriggert, "Because the simFailedException should have been triggert");
    }

    @Test
    void GetDatabaseConnectionTest()
    {
    }

    @Test
    void GetCustomerServiceTest()
    {
    }

    @Test
    void GetReadingServiceTest()
    {
    }

    @Test
    void DbConnectionPropertiesOverwriteTest()
    {
    }

    private void assertOpenConnections(int dbCon, int custCon, int readCon)
    {
        assertEquals(dbCon, ServiceProvider.Services.getOpenDbConnectionsCount());
        assertEquals(custCon, ServiceProvider.Services.getOpenCustomerServicesCount());
        assertEquals(readCon, ServiceProvider.Services.getOpenReadingServicesCount());
    }
}