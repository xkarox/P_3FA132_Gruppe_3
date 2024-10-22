package ace.database;

import ace.ErrorMessages;
import ace.database.intefaces.IDatabaseConnection;
import ace.utils;
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
    private DbHelperService _helperService;

    public Connection getConnection()
    {
        if (this._connection == null){
            throw new RuntimeException("Connection not initialised");
        }
        return _connection;
    }

    public void setHelperService(DbHelperService helperService)
    {
        this._helperService = helperService;
    }

    public DatabaseConnection(){
        setHelperService(new DbHelperService());
    }

    @Override
    public IDatabaseConnection openConnection(Properties properties)
    {
        String localUserName = System.getProperty("user.name").toLowerCase();

        String url = "jdbc:mariadb://" + properties.getProperty(localUserName + ".db.url");
        String user = properties.getProperty(localUserName + ".db.user");
        String password = properties.getProperty(localUserName + ".db.pw");

        try {
            _connection = DriverManager.getConnection(url, user, password);
            if (_connection == null)
            {
                throw new RuntimeException("Could not initialise connection");
            }
        }
        catch (SQLException e) // Could not connect to db
        {
            throw new RuntimeException(e);
        }

        return this;
    }

    @Override
    public void createAllTables()
    {
        List<String> tablesCommands = this._helperService.createSqlTableSchemaCommands();
        for (String createTableCommand : tablesCommands)
        {
            executeSqlUpdateCommand(createTableCommand, 0);
        }
    }

    @Override
    public void truncateAllTables()
    {
        List<String> tableNames = getAllTableNames();
        for (String tableName : tableNames)
        {
            String sql = "TRUNCATE TABLE " + tableName;
            executeSqlUpdateCommand(sql, 0);
        }
    }

    @Override
    public void removeAllTables()
    {
        List<String> tableNames = getAllTableNames();

        // Deactivate foreign key checks for dropping
        executeSqlUpdateCommand("SET FOREIGN_KEY_CHECKS = 0", 0);
        for (String tableName : tableNames)
        {
            String sql = "DROP TABLE IF EXISTS " + tableName;
            executeSqlUpdateCommand(sql, 0);
        }

        // Activate foreign key checks
        executeSqlUpdateCommand("SET FOREIGN_KEY_CHECKS = 1", 0);
    }

    public List<String> getAllTableNames()
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
        }
        catch (SQLException e)
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

    public int executeSqlUpdateCommand(String sql)
    {
        try (Statement stmt = this.getConnection().createStatement())
        {
            return stmt.executeUpdate(sql);
        } catch (SQLException e)
        {
            throw new RuntimeException(e);
        }
    }

    public int executeSqlUpdateCommand(String sql, int expectedLinesAffected)
    {
        try (Statement stmt = this.getConnection().createStatement())
        {
            int result = stmt.executeUpdate(sql);
            utils.checkValueEquals(result, expectedLinesAffected, ErrorMessages.SqlUpdate);
            return result;
        } catch (SQLException e)
        {
            throw new RuntimeException(e);
        }
    }

    public ResultSet executeSqlQueryCommand(String sql)
    {
        try (Statement stmt = this.getConnection().createStatement())
        {
            return stmt.executeQuery(sql);
        } catch (SQLException e)
        {
            throw new RuntimeException(e);
        }
    }
}
