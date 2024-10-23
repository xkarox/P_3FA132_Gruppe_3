package ace.database;

import ace.ErrorMessages;
import ace.database.intefaces.IDatabaseConnection;
import ace.model.decorator.FieldInfo;
import ace.model.interfaces.IDbItem;
import ace.utils;

import java.lang.reflect.Constructor;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

public class DatabaseConnection implements IDatabaseConnection
{
    private Connection _connection;
    private DbHelperService _helperService;
    private String _databaseName;

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

        this._databaseName = Arrays.asList(properties.getProperty(localUserName + ".db.url").split("/")).getLast();

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
            executeSqlUpdateCommand(sql);
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
            ResultSet tables = metaData.getTables(this._databaseName, null, "%", new String[]{"TABLE"});

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

    private <T extends IDbItem> List<? extends IDbItem> getObjectsFromDbTable(T object, String sqlWhereClause)
    {
        var tClass = object.getClass();
        List<FieldInfo> fieldInfos = FieldInfo.getFieldInformationFromClass(tClass);
        String queryCommand = String.format("SELECT * FROM %s %s;", object.getSerializedTableName(), sqlWhereClause);

        List<IDbItem> results = new ArrayList<>();

        try (ResultSet result = this.executeSqlQueryCommand(queryCommand))
        {
            while (result.next()) {
                List<Object> args = new ArrayList<>();
                for (FieldInfo fieldInfo : fieldInfos)
                {
                    if (fieldInfo.FieldType == String.class){
                        args.add(result.getString(fieldInfo.FieldName));
                    }
                    if (fieldInfo.FieldType == int.class){
                        args.add(result.getInt(fieldInfo.FieldName));
                    }
                }

                Constructor<? extends IDbItem> constructor = tClass.getConstructor();
                IDbItem newObject = constructor.newInstance();
                newObject.dbObjectFactory(args.toArray());
                results.add(newObject);
            }
        } catch (SQLException | ReflectiveOperationException e)
        {
            throw new RuntimeException(e);
        }

        return results;
    }

    public <T extends IDbItem> List<? extends IDbItem> getAllObjectsFromDbTable(T object){
        return getObjectsFromDbTable(object, "");
    }

    /**
     * Prints the information of a person.
     *
     * @param sqlWhereClause    Sql where statement, starts with: Where ...
     */
    public <T extends IDbItem> List<? extends IDbItem> getAllObjectsFromDbTableWithFilter(T object, String sqlWhereClause){
        return getObjectsFromDbTable(object, sqlWhereClause);

    }

}
