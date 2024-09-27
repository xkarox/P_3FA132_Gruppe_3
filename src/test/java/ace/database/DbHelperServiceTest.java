package ace.database;

import ace.model.classes.mocks.DbItemMock;
import ace.model.interfaces.IDbItem;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class DbHelperServiceTest
{
    private String _tableName;
    private String _sqlSchema;
    private String _expectedSchema;

    @BeforeEach
    void setUp()
    {
        this._tableName = "Testing";
        this._sqlSchema = "id INT PRIMARY KEY," + "name VARCHAR(50)," + "age INT";
        this._expectedSchema = "CREATE TABLE " + this._tableName + " (" + this._sqlSchema + ");";
    }

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
}

