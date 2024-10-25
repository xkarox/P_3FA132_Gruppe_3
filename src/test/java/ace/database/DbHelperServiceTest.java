package ace.database;

import ace.database.mocks.MockTableObject;
import ace.model.interfaces.IDbItem;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class DbHelperServiceTest
{
    private final MockTableObject mockData = new MockTableObject();
    private final String _expectedSchema = String.format("CREATE TABLE %s (%s);",
            this.mockData.getSerializedTableName(), this.mockData.getSerializedStructure());

    @Test
    void testCreateSqlSchema()
    {
        DbHelperService dbHelperService = new DbHelperService(new ArrayList<IDbItem>()
        {{
            add(mockData);
        }});

        List<String> tableSchemas = dbHelperService.createSqlTableSchemaCommands();
        assertEquals(1, tableSchemas.size(), "The list should contain one schema");
        assertEquals(this._expectedSchema, tableSchemas.getFirst(), "Should be the expected schema");
    }

    @Test
    void testCreateMultipleSchemas()
    {
        String secondTableName = "Testing1";
        String secondSqlSchema = "id INT PRIMARY KEY, name VARCHAR(100), age DOUBLE";
        String secondExpectedSchema = "CREATE TABLE " + secondTableName + " (" + secondSqlSchema + ");";

        MockTableObject mockTable = new MockTableObject();
        MockTableObject secondMockTable = new MockTableObject();
        secondMockTable.testSetSchema(secondSqlSchema);
        secondMockTable.testSetTableName(secondTableName);

        DbHelperService dbHelperService = new DbHelperService(new ArrayList<IDbItem>()
        {{
            add(mockTable);
            add(secondMockTable);
        }});

        List<String> tableSchemas = dbHelperService.createSqlTableSchemaCommands();

        assertEquals(2, tableSchemas.size(), "The list should contain two schemas");
        assertEquals(this._expectedSchema, tableSchemas.getFirst(), "Should be the expected schema");
        assertEquals(secondExpectedSchema, tableSchemas.getLast(), "Should be the expected schema");
    }

    @Test
    void getDefaultPropertiesTest() throws IOException
    {
        String localUserName = System.getProperty("user.name").toLowerCase();
        String url = "localhost:3306/homeautomation_test";
        String user = "test";
        String pw = "1243#+09?";

        String content = String.format("%s.db.url = %s\n%s.db.user = %s\n%s.db.pw = %s",
                localUserName, url, localUserName, user, localUserName, pw);

        InputStream inputStream = new ByteArrayInputStream(content.getBytes());
        Properties properties = DbHelperService.loadProperties(inputStream);

        String loadedUrl = properties.getProperty(localUserName + ".db.url");
        String loadedUser = properties.getProperty(localUserName + ".db.user");
        String loadedPw = properties.getProperty(localUserName + ".db.pw");

        assertEquals(loadedUrl, url, "Value doesn't match the expected value for url");
        assertEquals(loadedUser, user, "Value doesn't match the expected value for user");
        assertEquals(loadedPw, pw, "Value doesn't match the expected value for password");
    }

    @Test
    void loadPropertiesTest() throws IOException
    {
        String localUserName = System.getProperty("user.name").toLowerCase();
        Properties properties = DbHelperService.loadProperties();

        // Depends on values in the properties.config file ... but test coverage ...
        assertNotNull(properties, "Because they should have been loaded");
        assertNotNull(properties.getProperty(localUserName + ".db.url"));
        assertNotNull(properties.getProperty(localUserName + ".db.user"));
        assertNotNull(properties.getProperty(localUserName + ".db.pw"));
    }
}

