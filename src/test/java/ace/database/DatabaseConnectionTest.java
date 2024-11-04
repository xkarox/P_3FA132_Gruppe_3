package ace.database;

import ace.database.mocks.MockTableObject;
import ace.model.interfaces.IDbItem;
import org.junit.jupiter.api.*;

import java.io.IOException;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class DatabaseConnectionTest
{
    private final MockTableObject mockData = new MockTableObject();
    private DatabaseConnection _dbConnection;

    @BeforeEach
    void setUp(TestInfo testInfo) throws IOException, SQLException
    {
        if (testInfo.getTags().contains("excludeSetup"))
        {
            return;
        }

        DatabaseConnection dbConnection = new DatabaseConnection();
        dbConnection.openConnection(DbHelperService.loadProperties(DbTestHelper.loadTestDbProperties()));
        dbConnection.removeAllTables();

        if (testInfo.getTags().contains("createMockHelperService"))
        {
            DbHelperService dbHelperService = new DbHelperService(new ArrayList<IDbItem>()
            {{
                add(mockData);
            }});
            dbConnection.setHelperService(dbHelperService);
        }

        this._dbConnection = dbConnection;
    }

    @Test
    @Order(1)
    @Tag("excludeSetup")
    void openConnectionTest() throws IOException, SQLException
    {
        DatabaseConnection dbConnection = new DatabaseConnection();
        dbConnection.openConnection(DbHelperService.loadProperties(DbTestHelper.loadTestDbProperties()));
        try
        {
            assertFalse(dbConnection.getConnection().isClosed(), "A connection should be established");
        } catch (SQLException e)
        {
            throw new RuntimeException(e);
        }
    }

    @Test
    @Order(2)
    @Tag("createMockHelperService")
    void createAllTablesTest() throws SQLException
    {
        this._dbConnection.createAllTables();

        DatabaseMetaData metaData;
        try
        {
            metaData = this._dbConnection.getConnection().getMetaData();
        } catch (SQLException e)
        {
            throw new RuntimeException(e);
        }

        try (ResultSet tables = metaData.getTables(mockData.getSerializedTableName(), null, "%", new String[]{"TABLE"}))
        {
            while (tables.next())
            {
                String tableName = tables.getString("TABLE_NAME");
                assertEquals(this.mockData.getSerializedTableName(), tableName);

                try (ResultSet columns = metaData.getColumns(null, null, tableName, "%"))
                {
                    int column = 0;
                    List<String> mockColumns = List.of(this.mockData.getSerializedStructure().split(","));
                    while (columns.next())
                    {
                        String columnName = columns.getString("COLUMN_NAME");
                        String columnType = columns.getString("TYPE_NAME");
                        int columnSize = columns.getInt("COLUMN_SIZE");

                        List<String> mockColumn = List.of(mockColumns.get(column).split(" "));
                        String mockColumnName = mockColumn.getFirst().strip();
                        String mockColumnType = mockColumn.get(1).strip();
                        String mockColumnSize;

                        if (mockColumnType.equals("INT"))
                            mockColumnSize = "(10)";
                        else
                        {
                            mockColumnSize = mockColumn.get(2).strip();
                        }

                        assertEquals(mockColumnName, columnName, "Column name should be the same");
                        assertEquals(mockColumnType, columnType, "Column type should be the same");
                        assertEquals(mockColumnSize, "(" + columnSize + ")", "Column size should be the same");

                        column++;
                    }
                }
            }
        } catch (SQLException e)
        {
            throw new RuntimeException(e);
        }

    }

    @Test
    @Order(3)
    @Tag("createMockHelperService")
    void truncateAllTablesTest() throws SQLException
    {
        this._dbConnection.createAllTables();
        assertEquals(0, getTableData(this._dbConnection, this.mockData.getSerializedTableName()).length, "Because there should be no data in the mock table");
        createTestData(this._dbConnection);
        this._dbConnection.truncateAllTables();
        assertEquals(0, getTableData(this._dbConnection, this.mockData.getSerializedTableName()).length, "Because there should be no data in the mock table");
    }

    @Test
    @Order(4)
    @Tag("createMockHelperService")
    void getAllTableNamesTest() throws SQLException
    {
        List<String> tableNames = this._dbConnection.getAllTableNames();
        assertEquals(0, tableNames.size(), "Because there should be no tables");
        this._dbConnection.createAllTables();
        tableNames = this._dbConnection.getAllTableNames();
        assertEquals(1, tableNames.size(), "Because there should be one tables");
        assertEquals(this.mockData.getSerializedTableName(), tableNames.getFirst(), "Because the name should be the same");
    }

    @Test
    @Order(5)
    @Tag("createMockHelperService")
    void removeAllTablesTest() throws SQLException
    {
        List<String> tableNames = this._dbConnection.getAllTableNames();
        assertEquals(0, tableNames.size(), "Because there should be no tables");
        this._dbConnection.createAllTables();
        tableNames = this._dbConnection.getAllTableNames();
        assertEquals(1, tableNames.size(), "Because there should be one tables");
        this._dbConnection.removeAllTables();
        tableNames = this._dbConnection.getAllTableNames();
        assertEquals(0, tableNames.size(), "Because there should be no tables");
    }

    @Test
    @Order(6)
    void closeConnectionTest() throws SQLException
    {
        assertNotNull(this._dbConnection.getConnection(), "Because a connection should be established");
        this._dbConnection.closeConnection();
        try
        {
            assertTrue(this._dbConnection.getConnection().isClosed(), "Because the connection was closed");
        } catch (SQLException e)
        {
            throw new RuntimeException(e);
        }
    }

    @Test
    @Order(7)
    @Tag("createMockHelperService")
    void executeSqlUpdateCommandTest() throws SQLException
    {
        this._dbConnection.createAllTables();

        String createMockCommand = String.format("INSERT INTO %s VALUES (%d, '%s', %d)", this.mockData.getSerializedTableName(), 1, "t1", 111);
        int result = this._dbConnection.executeSqlUpdateCommand(createMockCommand, 1);
        assertEquals(1, result, "Because one line should be affected");

        createMockCommand = String.format("INSERT INTO %s VALUES (%d, '%s', %d), ('%d', '%s', '%d')", this.mockData.getSerializedTableName(),
                2, "t2", 222,
                3, "t3", 333);
        result = this._dbConnection.executeSqlUpdateCommand(createMockCommand);
        assertEquals(2, result, "Because two line should be affected");
    }

    @Test
    @Order(8)
    @Tag("createMockHelperService")
    void executePreparedStatementCommandTest() throws ReflectiveOperationException, SQLException
    {
        this._dbConnection.createAllTables();

        String sqlStatement = "INSERT INTO " + this.mockData.getSerializedTableName() +
                " (id, name, age) VALUES (?, ?, ?);";

        try (PreparedStatement statement = this._dbConnection.newPrepareStatement(sqlStatement))
        {
            statement.setObject(1, 2);
            statement.setObject(2, "t2");
            statement.setObject(3, 222);
            this._dbConnection.executePreparedStatementCommand(statement);
        }

        var result = this._dbConnection.getAllObjectsFromDbTable(new MockTableObject());
        assertEquals(1, result.size());
    }

    @Test
    @Order(9)
    @Tag("createMockHelperService")
    public void getAllObjectsFromDbTableTest() throws ReflectiveOperationException, SQLException
    {
        MockTableObject mockInstance = new MockTableObject("Peter", 99);
        MockTableObject mockInstance2 = new MockTableObject("hans", 66);


        this._dbConnection.createAllTables();
        String createMockCommand = String.format("INSERT INTO %s VALUES (%d, '%s', %d)", this.mockData.getSerializedTableName(), 1, mockInstance.name, mockInstance.age);
        this._dbConnection.executeSqlUpdateCommand(createMockCommand);

        var result = this._dbConnection.getAllObjectsFromDbTable(this.mockData);
        assertEquals(1, result.size(), "Because there should be just one item in the table");
        assertEquals(mockInstance, result.getFirst(), "Because the objects should be equal");

        String createMockCommand2 = String.format("INSERT INTO %s VALUES (%d, '%s', %d)", this.mockData.getSerializedTableName(), 2, mockInstance2.name, mockInstance2.age);
        this._dbConnection.executeSqlUpdateCommand(createMockCommand2);

        var result2 = this._dbConnection.getAllObjectsFromDbTable(this.mockData);
        assertEquals(2, result2.size(), "Because there should be just one item in the table");
        assertEquals(mockInstance, result2.getFirst(), "Because the objects should be equal");
        assertEquals(mockInstance2, result2.getLast(), "Because the objects should be equal");
    }

    @Test
    @Order(10)
    @Tag("excludeSetup")
    void openConnectionNull()
    {
        DatabaseConnection dbConnection = new DatabaseConnection();
        boolean exceptionTriggert = false;
        try
        {
            dbConnection.getConnection();
        } catch (RuntimeException e)
        {
            exceptionTriggert = true;
            assertEquals(e.getMessage(), "Connection not initialised");
        }
        assertTrue(exceptionTriggert, "Because the exception should have been triggert");
    }

    private void createTestData(DatabaseConnection dbConnection) throws SQLException
    {
        int mockId = 666;
        String mockName = "John Doe";
        int mockAge = 99;
        String createMockCommand = String.format("INSERT INTO %s VALUES (%d, '%s', %d)", this.mockData.getSerializedTableName(), mockId, mockName, mockAge);
        dbConnection.executeSqlUpdateCommand(createMockCommand, 1);

        String[][] tableData = getTableData(dbConnection, this.mockData.getSerializedTableName());
        assertEquals(1, tableData.length, "Because there should be one object in the mock table");
        assertEquals(String.valueOf(mockId), tableData[0][0], "Because id should be the same");
        assertEquals(mockName, tableData[0][1], "Because name should be the same");
        assertEquals(String.valueOf(mockAge), tableData[0][2], "Because age should be the same");
    }

    private String[][] getTableData(DatabaseConnection dbConnection, String tableName)
    {
        String[][] result;
        try (PreparedStatement preparedStatement = dbConnection.getConnection().prepareStatement("SELECT * FROM " + tableName);
             ResultSet dataResultSet = preparedStatement.executeQuery())
        {

            ResultSetMetaData metaData = dataResultSet.getMetaData();
            int columnCount = metaData.getColumnCount();

            int rowCount = 0;
            while (dataResultSet.next())
            {
                rowCount++;
            }

            dataResultSet.beforeFirst();

            result = new String[rowCount][columnCount];

            int rowCounter = 0;
            while (dataResultSet.next())
            {
                for (int j = 1; j <= columnCount; j++)
                {
                    result[rowCounter][j - 1] = dataResultSet.getString(j);
                }
                rowCounter++;
            }
        } catch (SQLException e)
        {
            throw new RuntimeException(e);
        }

        return result;
    }
}
