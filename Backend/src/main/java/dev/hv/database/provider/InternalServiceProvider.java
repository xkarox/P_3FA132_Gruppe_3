package dev.hv.database.provider;

import dev.hv.ResponseMessages;
import dev.hv.database.DatabaseConnection;
import dev.hv.database.services.CustomerService;
import dev.hv.database.services.ReadingService;

import java.io.IOException;
import java.sql.SQLException;
import java.util.*;

import static dev.hv.ResponseMessages.ServicesNotAvailable;

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

    private synchronized <T> T getService(Class<T> serviceClass) throws IOException, SQLException
    {
        while (this._usedDbConnections.size() >= _maxDbConnections ||
               (serviceClass == CustomerService.class && this._usedCustomerConnections.size() >= _maxCustomerConnections) ||
               (serviceClass == ReadingService.class && this._usedReadingConnections.size() >= _maxReadingConnections)) {
            try {
                if (!_useMultiThreading)
                    throw new IllegalArgumentException(String.valueOf(ServicesNotAvailable));
                wait();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return null;
            }
        }

        return serviceFactory(serviceClass);
    }

    private synchronized <T> T serviceFactory(Class<T> serviceClass) throws SQLException, IOException
    {
        int connectionKey;
        DatabaseConnection dbConnection;
        if (serviceClass == CustomerService.class) {
            dbConnection = getDatabaseConnection();
            connectionKey = searchFreeCustomerService(dbConnection);
            this._usedCustomerConnections.add(connectionKey);
            return serviceClass.cast(this._possibleCustomerServices.get(connectionKey));
        } else if (serviceClass == ReadingService.class) {
            dbConnection = getDatabaseConnection();
            connectionKey = searchFreeReadingService(dbConnection);
            this._usedReadingConnections.add(connectionKey);
            return serviceClass.cast(this._possibleReadingServices.get(connectionKey));
        } else {
            connectionKey = searchFreeDbConnection();
            this._usedDbConnections.add(connectionKey);
            dbConnection = this._possibleDbConnections.get(connectionKey);
            if (this._properties == null)
                dbConnection.openConnection();
            else
                dbConnection.openConnection(_properties);
            return serviceClass.cast(dbConnection);
        }
    }

    private synchronized <T> Integer searchFreeService(Map<Integer, T> possibleServices, List<Integer> usedConnections, int maxConnections, T newService)
    {
        for (int key : possibleServices.keySet())
        {
            boolean keyFound = !usedConnections.contains(key);
            if (keyFound)
            {
                return key;
            }
        }
        int objectId = getObjectId(newService);
        possibleServices.put(objectId, newService);
        return objectId;
    }

    private synchronized Integer searchFreeDbConnection()
    {
        return searchFreeService(_possibleDbConnections, _usedDbConnections, _maxDbConnections, new DatabaseConnection(this));
    }

    private synchronized Integer searchFreeCustomerService(DatabaseConnection dbConnection)
    {
        return searchFreeService(_possibleCustomerServices, _usedCustomerConnections, _maxCustomerConnections, new CustomerService(dbConnection, this));
    }

    private synchronized Integer searchFreeReadingService(DatabaseConnection dbConnection)
    {
        return searchFreeService(_possibleReadingServices, _usedReadingConnections, _maxReadingConnections, new ReadingService(dbConnection, this));
    }

    public synchronized DatabaseConnection getDatabaseConnection() throws IOException, SQLException
    {
        return getService(DatabaseConnection.class);
    }

    public synchronized CustomerService getCustomerService() throws IOException, SQLException
    {
        return getService(CustomerService.class);
    }

    public synchronized ReadingService getReadingService() throws IOException, SQLException
    {
        return getService(ReadingService.class);
    }

    private synchronized <T> void releaseService(T service, Map<Integer, T> possibleServices, List<Integer> usedConnections) throws SQLException
    {
        int id = getObjectId(service);
        if (service instanceof DatabaseConnection) {
            ((DatabaseConnection) service).closeConnection();
        }
        if (usedConnections.contains(id)) {
            possibleServices.remove(id, service);
            usedConnections.remove(Integer.valueOf(id));
            notifyAll();
        } else {
            throw new IllegalArgumentException(ResponseMessages.DbConnectionNotRegistered.toString());
        }
    }

    public void releaseDbConnection(DatabaseConnection connection) throws SQLException
    {
        releaseService(connection, _possibleDbConnections, _usedDbConnections);
    }

    public void releaseCustomerService(CustomerService connection) throws SQLException
    {
        releaseService(connection, _possibleCustomerServices, _usedCustomerConnections);
    }

    public void releaseReadingService(ReadingService connection) throws SQLException
    {
        releaseService(connection, _possibleReadingServices, _usedReadingConnections);
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
