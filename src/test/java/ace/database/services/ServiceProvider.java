package ace.database.services;

import ace.database.DatabaseConnection;

import java.io.IOException;

public class ServiceProvider
{
    private static DatabaseConnection _dbConnection;
    private static CustomerService _customerService;
    private static ReadingService _readingService;

    static
    {
        DatabaseConnection dbConnection = new DatabaseConnection();
        try
        {
            setDbConnection(dbConnection);
        } catch (IOException e)
        {
            throw new RuntimeException(e);
        }
    }

    public static void setDbConnection(DatabaseConnection dbConnection) throws IOException
    {
        _dbConnection = dbConnection;
        _dbConnection.openConnection();
        _customerService = new CustomerService(dbConnection);
        _readingService = new ReadingService(dbConnection);
    }

    public static DatabaseConnection getDatabaseConnection()
    {
        return _dbConnection;
    }

    public static CustomerService getCustomerService()
    {
        return _customerService;
    }

    public static ReadingService getReadingService()
    {
        return _readingService;
    }
}
