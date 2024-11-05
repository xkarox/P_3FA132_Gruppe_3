package ace.database.provider;

import ace.database.DatabaseConnection;
import ace.database.services.CustomerService;
import ace.database.services.ReadingService;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class InternalServiceProvider
{
    private final int _maxDbConnections;
    private final Map<Integer, DatabaseConnection> _possibleDbConnections = new HashMap<>();
    private final List<Integer> _usedDbConnections = new ArrayList<>();

    private final int _maxCustomerConnections;
    private final Map<Integer, CustomerService> _possibleCustomerServices = new HashMap<>();
    private final List<Integer> _usedCustomerConnections = new ArrayList<>();

    private final int _maxReadingConnections;
    private final Map<Integer, ReadingService> _possibleReadingServices = new HashMap<>();
    private final List<Integer> _usedReadingConnections = new ArrayList<>();


    public InternalServiceProvider(int maxDbConnections, int maxCustomerConnections, int maxReadingConnections)
    {
        this._maxDbConnections = maxDbConnections;
        this._maxCustomerConnections = maxCustomerConnections;
        this._maxReadingConnections = maxReadingConnections;
    }

    public synchronized DatabaseConnection getDatabaseConnection() throws IOException
    {
        while (this._usedDbConnections.size() >= _maxDbConnections) {
            try {
                wait();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return null;
            }
        }
        int connectionKey = searchFreeDbConnection();
        this._usedDbConnections.add(connectionKey);
        DatabaseConnection connection = this._possibleDbConnections.get(connectionKey);
        connection.openConnection();
        return connection;
    }

    public synchronized CustomerService getCustomerService() throws IOException
    {
        while (this._usedCustomerConnections.size() >= _maxCustomerConnections) {
            try {
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

    public synchronized ReadingService getReadingService() throws IOException
    {
        while (this._usedReadingConnections.size() >= _maxReadingConnections) {
            try {
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

    public synchronized void releaseDbConnection(DatabaseConnection connection)
    {
        int id = getObjectId(connection);
        connection.closeConnection();
        if (this._usedDbConnections.contains(id)) {
            this._possibleDbConnections.remove(id, connection);
            this._usedDbConnections.remove(Integer.valueOf(id));
            notifyAll();
        }
    }

    public synchronized void releaseCustomerService(CustomerService connection)
    {
        int id = getObjectId(connection);
        if (this._usedCustomerConnections.contains(id)) {
            this._possibleCustomerServices.remove(id, connection);
            this._usedCustomerConnections.remove(Integer.valueOf(id));
            notifyAll();
        }
    }

    public synchronized void releaseReadingService(ReadingService connection)
    {
        int id = getObjectId(connection);
        if (this._usedReadingConnections.contains(id)) {
            this._possibleReadingServices.remove(id, connection);
            this._usedReadingConnections.remove(Integer.valueOf(id));
            notifyAll();
        }
    }

    private int getObjectId(Object object)
    {
        return System.identityHashCode(object);

    }
}
