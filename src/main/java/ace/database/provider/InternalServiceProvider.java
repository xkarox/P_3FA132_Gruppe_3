package ace.database.provider;

import ace.ErrorMessages;
import ace.database.DatabaseConnection;
import ace.database.services.CustomerService;
import ace.database.services.ReadingService;

import java.io.IOException;
import java.sql.SQLException;
import java.util.*;

import static ace.ErrorMessages.ServicesNotAvailable;

public class InternalServiceProvider
{
    private Properties _properties = null;
    private boolean _useMultiThreading = false;

    private int _maxDbConnections;
    private final Map<Integer, DatabaseConnection> _possibleDbConnections = new HashMap<>();
    private final List<Integer> _usedDbConnections = new ArrayList<>();

    private int _maxCustomerConnections;
    private final Map<Integer, CustomerService> _possibleCustomerServices = new HashMap<>();
    private final List<Integer> _usedCustomerConnections = new ArrayList<>();

    private int _maxReadingConnections;
    private final Map<Integer, ReadingService> _possibleReadingServices = new HashMap<>();
    private final List<Integer> _usedReadingConnections = new ArrayList<>();


    public InternalServiceProvider(int maxDbConnections, int maxCustomerConnections, int maxReadingConnections)
    {
        this._maxDbConnections = maxDbConnections;
        this._maxCustomerConnections = maxCustomerConnections;
        this._maxReadingConnections = maxReadingConnections;
    }

    public synchronized DatabaseConnection getDatabaseConnection() throws IOException, SQLException
    {
        while (this._usedDbConnections.size() >= _maxDbConnections) {
            try {
                if (!_useMultiThreading)
                    throw  new IllegalArgumentException(String.valueOf(ServicesNotAvailable));
                wait();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return null;
            }
        }
        int connectionKey = searchFreeDbConnection();
        this._usedDbConnections.add(connectionKey);
        DatabaseConnection connection = this._possibleDbConnections.get(connectionKey);
        if (this._properties == null)
            connection.openConnection();
        else
            connection.openConnection(_properties);

        return connection;
    }

    public synchronized CustomerService getCustomerService() throws IOException, SQLException
    {
        while (this._usedDbConnections.size() >= _maxDbConnections || this._usedCustomerConnections.size() >= _maxCustomerConnections) {
            try {
                if (!_useMultiThreading)
                    throw  new IllegalArgumentException(String.valueOf(ServicesNotAvailable));
                wait();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return null;
            }
        }
        DatabaseConnection dbConnection = getDatabaseConnection();
        int connectionKey = searchFreeCustomerService(dbConnection);
        this._usedCustomerConnections.add(connectionKey);
        return this._possibleCustomerServices.get(connectionKey);
    }

    public synchronized ReadingService getReadingService() throws IOException, SQLException
    {
        while (this._usedDbConnections.size() >= _maxDbConnections || this._usedReadingConnections.size() >= _maxReadingConnections) {
            try {
                if (!_useMultiThreading)
                    throw  new IllegalArgumentException(String.valueOf(ServicesNotAvailable));
                wait();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return null;
            }
        }
        DatabaseConnection dbConnection = getDatabaseConnection();
        int connectionKey = searchFreeReadingService(dbConnection);
        this._usedReadingConnections.add(connectionKey);
        return this._possibleReadingServices.get(connectionKey);
    }

    public synchronized Integer searchFreeDbConnection()
    {
        for (int key : _possibleDbConnections.keySet())
        {
            if (!_usedDbConnections.contains(key))
            {
                return key;
            }
        }
        if (this._possibleDbConnections.size() > this._maxDbConnections)
            return -1;
        else
        {
            DatabaseConnection newConnection = new DatabaseConnection(this);
            int objectId = System.identityHashCode(newConnection);
            this._possibleDbConnections.put(objectId, newConnection);
            return objectId;
        }
    }

    public synchronized Integer searchFreeCustomerService(DatabaseConnection dbConnection)
    {
        for (int key : _possibleCustomerServices.keySet())
        {
            if (!_usedCustomerConnections.contains(key))
            {
                return key;
            }
        }
        if (this._possibleCustomerServices.size() > this._maxCustomerConnections)
            return -1;
        else
        {
            CustomerService newService = new CustomerService(dbConnection, this);
            int objectId = System.identityHashCode(newService);
            this._possibleCustomerServices.put(objectId, newService);
            return objectId;
        }
    }

    public synchronized Integer searchFreeReadingService(DatabaseConnection dbConnection)
    {
        for (int key : _possibleReadingServices.keySet())
        {
            if (!_usedReadingConnections.contains(key))
            {
                return key;
            }
        }
        if (this._possibleReadingServices.size() > this._maxReadingConnections)
            return -1;
        else
        {

            ReadingService newService = new ReadingService(dbConnection, this);
            int objectId = System.identityHashCode(newService);
            this._possibleReadingServices.put(objectId, newService);
            return objectId;
        }
    }

    public synchronized <T> void releaseService(T service, Map<Integer, T> possibleServices, List<Integer> usedConnections) throws SQLException
    {
        int id = getObjectId(service);
        if (service instanceof DatabaseConnection) {
            ((DatabaseConnection) service).closeConnection();
        }
        if (usedConnections.contains(id)) {
            possibleServices.remove(id, service);
            usedConnections.remove(Integer.valueOf(id));
            notifyAll();
        }
    }

    public void releaseDbConnection(DatabaseConnection connection) throws SQLException
    {
        releaseService(connection, _possibleDbConnections, _usedDbConnections);
    }

    public void releaseCustomerService(CustomerService connection)
    {
        try {
            releaseService(connection, _possibleCustomerServices, _usedCustomerConnections);
        } catch (SQLException e) {
            // Handle exception
        }
    }

    public void releaseReadingService(ReadingService connection)
    {
        try {
            releaseService(connection, _possibleReadingServices, _usedReadingConnections);
        } catch (SQLException e) {
            // Handle exception
        }
    }

    private int getObjectId(Object object)
    {
        return System.identityHashCode(object);

    }

    public void dbConnectionPropertiesOverwrite(Properties properties)
    {
        this._properties = properties;
    }

    public void configureMaxConnections(int maxDbConnections, int maxCustomerConnections, int maxReadingConnections)
    {
        this._maxDbConnections = maxDbConnections;
        this._maxCustomerConnections = maxCustomerConnections;
        this._maxReadingConnections = maxReadingConnections;
    }

    public int getOpenDbConnectionsCount()
    {
        return _usedDbConnections.size();
    }

    public int getOpenCustomerServicesCount()
    {
        return _usedCustomerConnections.size();
    }

    public int getOpenReadingServicesCount()
    {
        return _usedReadingConnections.size();
    }

    public void setMultithreading(boolean state)
    {
        this._useMultiThreading = state;
    }
}
