package ace.database;

import ace.model.classes.mocks.DbItemMock;
import ace.model.interfaces.IDbItem;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.*;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import static org.junit.jupiter.api.Assertions.*;

public class DatabaseConnectionTest
{
    @BeforeEach
    void setUp()
    {
        DatabaseConnection dbConnection = new DatabaseConnection();
        dbConnection.openConnection(DatabaseConnection.loadProperties(loadTestDbProperties()));
        dbConnection.removeAllTables();
    }

    @Test
    void getDefaultPropertiesTest()
    {
        String localUserName = System.getProperty("user.name").toLowerCase();
        String url = "localhost:3306/homeautomation_test";
        String user = "test";
        String pw = "1243#+09?";

        String content = localUserName + ".db.url = " + url + "\n"
                + localUserName + ".db.user = " + user + "\n"
                + localUserName + ".db.pw = " + pw;

        InputStream inputStream = new ByteArrayInputStream(content.getBytes());
        Properties properties = DatabaseConnection.loadProperties(inputStream);

        String loadedUrl = properties.getProperty(localUserName + ".db.url");
        String loadedUser = properties.getProperty(localUserName + ".db.user");
        String loadedPw = properties.getProperty(localUserName + ".db.pw");

        assertEquals(loadedUrl, url, "Value doesn't match the expected value for url");
        assertEquals(loadedUser, user, "Value doesn't match the expected value for user");
        assertEquals(loadedPw, pw, "Value doesn't match the expected value for password");
    }

    @Test
    void openConnectionTest(){
        DatabaseConnection dbConnection = new DatabaseConnection();
        dbConnection.openConnection(DatabaseConnection.loadProperties(loadTestDbProperties()));

        assertNotNull(dbConnection.getConnection(), "A connection should be established");
    }

    @Test
    void createAllTablesTest(){

        String mockTableName = "testing";
        String mockSqlSchema = "id INT PRIMARY KEY," +
                            "name VARCHAR (50)," +
                            "age INT";

        String expectedSchema = "CREATE TABLE " + mockTableName + " (" + mockSqlSchema + ");";

        IDbItem mockTable = new DbItemMock(mockSqlSchema, mockTableName);
        DbHelperService dbHelperService = new DbHelperService(new ArrayList<IDbItem>(){{add(mockTable);}});

        DatabaseConnection dbConnection = loadTestDatabaseConnection();
        dbConnection.setHelperService(dbHelperService);
        dbConnection.createAllTables();


        DatabaseMetaData metaData = null;
        try
        {
            metaData = dbConnection.getConnection().getMetaData();
        } catch (SQLException e)
        {
            throw new RuntimeException(e);
        }

        try (ResultSet tables = metaData.getTables(null, null, "%", new String[] { "TABLE" }))
        {
            while (tables.next())
            {
                String tableName = tables.getString("TABLE_NAME");
                assertEquals(mockTableName, tableName);

                try (ResultSet columns = metaData.getColumns(null, null, tableName, "%"))
                {
                    int column = 0;
                    List<String> mockColumns = List.of(mockSqlSchema.split(","));
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
    void truncateAllTablesTest(){

    }

    @Test
    void removeAllTablesTest(){

    }

    @Test
    void getAllTableNamesTest(){

    }

    @Test
    void closeConnectionTest(){

    }

    private DatabaseConnection loadTestDatabaseConnection()
    {
        DatabaseConnection dbConnection = new DatabaseConnection();
        dbConnection.openConnection(DatabaseConnection.loadProperties(loadTestDbProperties()));
        return dbConnection;
    }

    private InputStream loadTestDbProperties ()
    {
        Path currentPath = Paths.get(System.getProperty("user.dir"));
        Path filePath = currentPath.resolve("config\\properties.config.test");

        try
        {
            return new FileInputStream(String.valueOf(filePath.toAbsolutePath()));
        }
        catch (IOException e)
        {
            throw new RuntimeException(e);
        }

    }
}
