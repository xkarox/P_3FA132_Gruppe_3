package ace.database;

import ace.ErrorMessages;
import ace.Utils;
import ace.database.intefaces.IDatabaseConnection;
import ace.model.decorator.FieldInfo;
import ace.model.interfaces.IDbItem;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.sql.*;
import java.util.*;

public class DatabaseConnection implements IDatabaseConnection
{
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

    public DatabaseConnection()
    {
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
        Utils.checkValueEquals(expectedLinesAffected, result, ErrorMessages.SqlUpdate);
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

    public PreparedStatement newPrepareStatement(String statement) throws SQLException
    {
        return this.getConnection().prepareStatement(statement);
    }

    private <T extends IDbItem> List<? extends IDbItem> getObjectsFromDbTable(T object, String sqlWhereClause) throws SQLException, ReflectiveOperationException
    {
        var tClass = object.getClass();
        List<FieldInfo> fieldInfos = FieldInfo.getFieldInformationFromClass(tClass);
        String queryCommand = String.format("SELECT * FROM %s %s;", object.getSerializedTableName(), sqlWhereClause);

        List<IDbItem> results = new ArrayList<>();

        try (ResultSet result = this.executeSqlQueryCommand(queryCommand))
        {
            while (result.next())
            {
                List<Object> args = new ArrayList<>();

                for (FieldInfo fieldInfo : fieldInfos) {
                    String fieldName = fieldInfo.FieldName;
                    Class<?> fieldType = fieldInfo.FieldType;

                    Object value = switch (fieldType.getSimpleName()){
                        case "String", "UUID", "LocalDate" -> result.getString(fieldName);
                        case "int" -> result.getInt(fieldName);
                        case "Boolean" -> result.getBoolean(fieldName);
                        case "Double" -> result.getDouble(fieldName);
                        default -> null;
                    };
                    if (value == null)
                        throw new IllegalArgumentException("Field type not supported");
                    args.add(value);
                }

                Constructor<? extends IDbItem> constructor = tClass.getConstructor();
                IDbItem newObject = constructor.newInstance();
                newObject.dbObjectFactory(args.toArray());
                results.add(newObject);
            }
        }

        return results;
    }

    public <T extends IDbItem> List<? extends IDbItem> getAllObjectsFromDbTable(T object) throws ReflectiveOperationException, SQLException
    {
        return getObjectsFromDbTable(object, "");
    }

    /**
     * Prints the information of a person.
     *
     * @param sqlWhereClause Sql where statement, starts with: Where ...
     */
    public <T extends IDbItem> List<? extends IDbItem> getAllObjectsFromDbTableWithFilter(T object, String sqlWhereClause) throws ReflectiveOperationException, SQLException
    {
        return getObjectsFromDbTable(object, sqlWhereClause);

    }

}
