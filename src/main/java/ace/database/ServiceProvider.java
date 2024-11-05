package ace.database;

import ace.database.services.CustomerService;
import ace.database.services.ReadingService;

import java.io.IOException;
import java.sql.SQLException;

public class ServiceProvider
{
    private static DatabaseConnection _connection;
    private static CustomerService _customerService;
    private static ReadingService _readingService;

    static {

        _connection = new DatabaseConnection();
        try
        {
            _connection.openConnection();
        } catch (IOException | SQLException e)
        {
            throw new RuntimeException(e);
        }
        try
        {
            _connection.createAllTables();
        } catch (SQLException e)
        {
            throw new RuntimeException(e);
        }
        _customerService = new CustomerService(_connection);
        _readingService = new ReadingService(_connection);
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
