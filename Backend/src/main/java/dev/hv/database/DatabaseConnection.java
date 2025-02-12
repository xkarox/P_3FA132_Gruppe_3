package dev.hv.database;

import dev.hv.ResponseMessages;
import dev.hv.Utils;
import dev.hv.database.intefaces.IDatabaseConnection;
import dev.hv.database.provider.InternalServiceProvider;
import dev.hv.model.interfaces.IId;
import dev.hv.model.decorator.FieldInfo;
import dev.hv.model.interfaces.IDbItem;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.sql.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

// Req. Nr.: 13
public class DatabaseConnection implements IDatabaseConnection, AutoCloseable
{
    private final InternalServiceProvider _provider;


    private Connection _connection;
    private DbHelperService _helperService;
    private String _databaseName;

    public Connection getConnection()
    {
        return _connection;
    }

    public void setHelperService(DbHelperService helperService)
    {
        this._helperService = helperService;
    }

    public DatabaseConnection(InternalServiceProvider provider)
    {
        this._provider = provider;
        setHelperService(new DbHelperService());
    }

    public DatabaseConnection()
    {
        this._provider = null;
        setHelperService(new DbHelperService());
    }

    @Override
    /* Always call through getConnection for null handling */
    public IDatabaseConnection openConnection(Properties properties) throws SQLException
    {
        String localUserName = System.getProperty("user.name").toLowerCase();

        String url = "jdbc:mariadb://" + properties.getProperty(localUserName + ".db.url");
        String user = properties.getProperty(localUserName + ".db.user");
        String password = properties.getProperty(localUserName + ".db.pw");

        this._databaseName = Arrays.asList(properties.getProperty(localUserName + ".db.url").split("/")).getLast();
        _connection = DriverManager.getConnection(url, user, password);

        return this;
    }

    public IDatabaseConnection openConnection() throws IOException, SQLException
    {
        return this.openConnection(DbHelperService.loadProperties());
    }


    @Override
    public void createAllTables() throws SQLException
    {
        List<String> tablesCommands = this._helperService.createSqlTableSchemaCommands();
        for (String createTableCommand : tablesCommands)
        {
            executeSqlUpdateCommand(createTableCommand, 0);
        }
    }

    @Override
    public void truncateAllTables() throws SQLException
    {
        List<String> tableNames = getAllTableNames();
        for (String tableName : tableNames)
        {
            String sql = "TRUNCATE TABLE " + tableName;
            executeSqlUpdateCommand(sql);
        }
    }

    @Override
    public void removeAllTables() throws SQLException
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

    public List<String> getAllTableNames() throws SQLException
    {
        List<String> tableNames = new ArrayList<>();

        DatabaseMetaData metaData = getConnection().getMetaData();
        ResultSet tables = metaData.getTables(this._databaseName, null, "%", new String[]{"TABLE"});

        while (tables.next())
        {
            String tableName = tables.getString("TABLE_NAME");
            tableNames.add(tableName);
        }

        return tableNames;
    }

    @Override
    public void closeConnection() throws SQLException
    {
        this._connection.close();
    }

    public int executeSqlUpdateCommand(String sql) throws SQLException
    {
        var connection = this.getConnection();
        Statement stmt = connection.createStatement();
        return stmt.executeUpdate(sql);
    }

    public int executeSqlUpdateCommand(String sql, int expectedLinesAffected) throws SQLException
    {
        var connection = this.getConnection();
        Statement stmt = connection.createStatement();
        int result = stmt.executeUpdate(sql);
        Utils.checkValueEquals(expectedLinesAffected, result, ResponseMessages.SqlUpdate);
        return result;
    }

    public ResultSet executeSqlQueryCommand(String sql) throws SQLException
    {
        var connection = this.getConnection();
        Statement stmt = connection.createStatement();
        return stmt.executeQuery(sql);
    }

    public void executePreparedStatementCommand(PreparedStatement preparedStatement) throws SQLException
    {
        preparedStatement.executeUpdate();
    }

    public int executePreparedStatementCommand(PreparedStatement preparedStatement, int expectedLinesAffected) throws SQLException
    {
        int result = preparedStatement.executeUpdate();
        Utils.checkValueEquals(expectedLinesAffected, result, ResponseMessages.SqlUpdate);
        return result;
    }

    public PreparedStatement newPrepareStatement(String statement) throws SQLException
    {
        return this.getConnection().prepareStatement(statement);
    }

    private <T extends IDbItem & IId> List<T> getObjectsFromDbTable(Class<T> classInfo, String sqlWhereClause) throws SQLException, ReflectiveOperationException, IOException
    {
        IDbItem object = classInfo.getConstructor().newInstance();

        var tClass = object.getClass();
        List<FieldInfo> fieldInfos = FieldInfo.getFieldInformationFromClass(tClass);
        String queryCommand = String.format("SELECT * FROM %s %s;", object.getSerializedTableName(), sqlWhereClause);

        List<T> results = new ArrayList<>();

        try (ResultSet result = this.executeSqlQueryCommand(queryCommand))
        {
            while (result.next())
            {
                List<Object> args = new ArrayList<>();

                for (FieldInfo fieldInfo : fieldInfos)
                {
                    String fieldName = fieldInfo.FieldName;
                    Class<?> fieldType = fieldInfo.FieldType;

                    Object value = switch (fieldType.getSimpleName())
                    {
                        case "String", "UUID", "LocalDate" -> result.getString(fieldName);
                        case "int" -> result.getInt(fieldName);
                        case "Boolean" -> result.getBoolean(fieldName);
                        case "Double" -> result.getDouble(fieldName);
                        default -> throw new IllegalArgumentException(ResponseMessages.DbFieldTypeNotSupported.toString());
                    };
                    args.add(value);
                }

                Constructor<T> constructor = classInfo.getConstructor();
                T newObject = constructor.newInstance();
                newObject.dbObjectFactory(args.toArray());
                results.add(newObject);
            }
        }

        return results;
    }

    public <T extends IDbItem & IId> List<T> getAllObjectsFromDbTable(Class<T> classInfo) throws ReflectiveOperationException, SQLException, IOException
    {
        return getObjectsFromDbTable(classInfo, "");
    }

    /**
     * Prints the information of a person.
     *
     * @param sqlWhereClause Sql where statement, starts with: Where ...
     */
    public <T extends IDbItem & IId> List<T> getAllObjectsFromDbTableWithFilter(Class<T> classInfo, String sqlWhereClause) throws ReflectiveOperationException, SQLException, IOException
    {
        return getObjectsFromDbTable(classInfo, sqlWhereClause);
    }

    @Override
    public void close() throws SQLException
    {
        if (this._provider != null)
            this._provider.releaseDbConnection(this);
    }
}
