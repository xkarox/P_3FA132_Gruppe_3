package ace.database;

import ace.database.mocks.DbItemMock;
import ace.model.interfaces.IDbItem;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class DbHelperServiceTest
{
    private final String _tableName = "Testing";
    private final String _sqlSchema = "id INT PRIMARY KEY," + "name VARCHAR(50)," + "age INT";
    private final String _expectedSchema = "CREATE TABLE " + this._tableName + " (" + this._sqlSchema + ");";

    @Test
    void testCreateSqlSchema()
    {
        IDbItem mockTable = new DbItemMock(this._sqlSchema, this._tableName);
        DbHelperService dbHelperService = new DbHelperService(new ArrayList<IDbItem>(){{add(mockTable);}});

        List<String> tableSchemas = dbHelperService.createSqlTableSchemaCommands();
        assertEquals(1, tableSchemas.size(), "The list should contain one schema");
        assertEquals(this._expectedSchema, tableSchemas.getFirst(), "Should be the expected schema");
    }

    @Test
    void testCreateMultipleSchemas()
    {
        String secondTableName = "Testing1";
        String secondSqlSchema = "id INT PRIMARY KEY," + "name VARCHAR(100)," + "age DOUBLE";
        String secondExpectedSchema = "CREATE TABLE " + secondTableName + " (" + secondSqlSchema + ");";

        IDbItem mockTable = new DbItemMock(this._sqlSchema, this._tableName);
        IDbItem secondMockTable = new DbItemMock(secondSqlSchema, secondTableName);

        DbHelperService dbHelperService = new DbHelperService(new ArrayList<IDbItem>(){{
            add(mockTable);
            add(secondMockTable);
        }});

        List<String> tableSchemas = dbHelperService.createSqlTableSchemaCommands();

        assertEquals(2, tableSchemas.size(), "The list should contain two schemas");
        assertEquals(this._expectedSchema, tableSchemas.getFirst(), "Should be the expected schema");
        assertEquals(secondExpectedSchema, tableSchemas.getLast(), "Should be the expected schema");
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
        Properties properties = DbHelperService.loadProperties(inputStream);

        String loadedUrl = properties.getProperty(localUserName + ".db.url");
        String loadedUser = properties.getProperty(localUserName + ".db.user");
        String loadedPw = properties.getProperty(localUserName + ".db.pw");

        assertEquals(loadedUrl, url, "Value doesn't match the expected value for url");
        assertEquals(loadedUser, user, "Value doesn't match the expected value for user");
        assertEquals(loadedPw, pw, "Value doesn't match the expected value for password");
    }
}

