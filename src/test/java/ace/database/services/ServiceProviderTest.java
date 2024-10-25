package ace.database.services;

import ace.database.DatabaseConnection;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class ServiceProviderTest
{
    @Test
    void setDbConnection() throws IOException
    {
        DatabaseConnection dbConnection = new DatabaseConnection();
        ServiceProvider.setDbConnection(dbConnection);
        assertEquals(dbConnection, ServiceProvider.getDatabaseConnection());
    }

    @Test
    void getDatabaseConnection()
    {
        assertNotNull(ServiceProvider.getDatabaseConnection());
    }

    @Test
    void getCustomerService()
    {
        assertNotNull(ServiceProvider.getCustomerService());
    }

    @Test
    void getReadingService()
    {
        assertNotNull(ServiceProvider.getReadingService());
    }
}