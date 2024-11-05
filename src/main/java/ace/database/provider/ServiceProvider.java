package ace.database.provider;

import ace.database.DatabaseConnection;
import ace.database.services.CustomerService;
import ace.database.services.ReadingService;

import java.io.IOException;

public class ServiceProvider
{
    private static final InternalServiceProvider _services;

    static {
        _services = new InternalServiceProvider(100, 10, 10);
    }

    public static DatabaseConnection GetDatabaseConnection() throws IOException
    {
        return _services.getDatabaseConnection();
    }

    public static CustomerService GetCustomerService() throws IOException
    {
        return _services.getCustomerService();
    }

    public static ReadingService GetReadingService() throws IOException
    {
        return _services.getReadingService();
    }
}
