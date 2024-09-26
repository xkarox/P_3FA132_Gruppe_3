package ace.model;

import ace.model.interfaces.IDatabaseConnection;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class DatabaseConnection implements IDatabaseConnection
{
    private Connection _connection;

    public Connection getConnection()
    {
        if (this._connection == null)
            throw new RuntimeException("Connection not initialised");
        return _connection;
    }

    @Override
    public IDatabaseConnection openConnection(Properties properties)
    {
        String localUserName = System.getProperty("user.name");

        String url = properties.getProperty(localUserName + "db.url");
        String user = properties.getProperty(localUserName + "db.user");
        String password = properties.getProperty(localUserName + "db.password");

        try {
            _connection = DriverManager.getConnection(url, user, password);
            if (_connection == null)
            {
                throw new RuntimeException("Could not initialise connection");
            }
        } catch (SQLException e) // Could not connect to db
        {
            throw new RuntimeException(e);
        }

        return this;
    }

    @Override
    public void createAllTables()
    {
        throw new RuntimeException("Not implemented");
    }

    @Override
    public void truncateAllTables()
    {
        List<String> tableNames = getAllTableNames();
        for (String tableName : tableNames)
        {
            String sql = "TRUNCATE TABLE " + tableName;

            try (Statement stmt = this._connection.createStatement())
            {
                stmt.executeUpdate(sql);
            } catch (SQLException e)
            {
                throw new RuntimeException(e);
            }
        }
    }

    @Override
    public void removeAllTables()
    {
        List<String> tableNames = getAllTableNames();
        for (String tableName : tableNames)
        {
            String sql = "DROP TABLE IF EXISTS " + tableName;

            try (Statement stmt = this._connection.createStatement())
            {
                stmt.executeUpdate(sql); // Remove table
            } catch (SQLException e)
            {
                throw new RuntimeException(e);
            }
        }
    }

    private List<String> getAllTableNames()
    {
        List<String> tableNames = new ArrayList<>();

        try
        {
            DatabaseMetaData metaData = getConnection().getMetaData();
            ResultSet tables = metaData.getTables(null, null, "%", new String[]{"TABLE"});

            while (tables.next())
            {
                String tableName = tables.getString("TABLE_NAME");
                tableNames.add(tableName);
            }
        } catch (SQLException e)
        {
            throw new RuntimeException(e);
        }

        return tableNames;
    }

    @Override
    public void closeConnection()
    {
        try
        {
            getConnection().close();
        } catch (SQLException e)
        {
            throw new RuntimeException(e);
        }
    }
}
