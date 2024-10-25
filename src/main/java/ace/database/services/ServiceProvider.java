package ace.database.services;

import ace.database.DatabaseConnection;

public class ServiceProvider
{
    private static DatabaseConnection _dbConnection;
    private static CustomerService _customerService;
    private static ReadingService _readingService;

    static
    {
        DatabaseConnection dbConnection = new DatabaseConnection();
        setDbConnection(dbConnection);
    }

    public static void setDbConnection(DatabaseConnection dbConnection)
    {
        _dbConnection = dbConnection;

        _dbConnection.openConnection();
        _customerService = new CustomerService(dbConnection);
        _readingService = new ReadingService(dbConnection);
    }

    public static DatabaseConnection GetDatabaseConnection()
    {
        return _dbConnection;
    }

    public static CustomerService GetCustomerService()
    {
        return _customerService;
    }

    public static ReadingService GetReadingService()
    {
        return _readingService;
    }
}
