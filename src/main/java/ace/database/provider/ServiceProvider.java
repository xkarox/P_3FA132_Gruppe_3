package ace.database.provider;

import ace.database.DatabaseConnection;
import ace.database.services.CustomerService;
import ace.database.services.ReadingService;

import java.io.IOException;
import java.io.InputStream;
import java.sql.SQLException;
import java.util.Properties;

public class ServiceProvider
{
    private static final InternalServiceProvider _services;

    static {
        _services = new InternalServiceProvider(100, 10, 10);
    }

    public static DatabaseConnection GetDatabaseConnection() throws IOException, SQLException
    {
        return _services.getDatabaseConnection();
    }

    public static CustomerService GetCustomerService() throws IOException, SQLException
    {
        return _services.getCustomerService();
    }

    public static ReadingService GetReadingService() throws IOException, SQLException
    {
        return _services.getReadingService();
    }

    public static void DbConnectionPropertiesOverwrite(Properties properties)
    {
        _services.dbConnectionPropertiesOverwrite(properties);
    }

    public static void ConfigureMaxConnections(int maxDbConnections, int maxCustomerConnections, int maxReadingConnections)
    {
        _services.configureMaxConnections(maxDbConnections, maxCustomerConnections, maxReadingConnections);
    }
}
