package server.validator;

import com.networknt.schema.JsonSchema;
import com.networknt.schema.JsonSchemaFactory;
import com.networknt.schema.SpecVersion;
import org.checkerframework.checker.units.qual.C;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class CustomerJsonSchemaValidatorServiceTest
{
    static JsonSchema customerSchema;
    CustomerJsonSchemaValidatorService customerJsonSchemaValidatorService;

    @BeforeAll
    static void setUp()
    {
        String schema = "{\n" +
                "  \"title\": \"Customer-JSON-Schema\",\n" +
                "  \"type\": \"object\",\n" +
                "  \"required\": [\n" +
                "    \"customer\"\n" +
                "  ],\n" +
                "  \"properties\": {\n" +
                "    \"customer\": {\n" +
                "      \"type\": \"object\",\n" +
                "      \"required\": [\n" +
                "        \"firstName\",\n" +
                "        \"lastName\",\n" +
                "        \"gender\"\n" +
                "      ],\n" +
                "      \"properties\": {\n" +
                "        \"id\": {\n" +
                "          \"type\" : [\"string\", \"null\"]\n" +
                "        },\n" +
                "        \"firstName\": {\n" +
                "          \"type\": \"string\"\n" +
                "        },\n" +
                "        \"lastName\": {\n" +
                "          \"type\": \"string\"\n" +
                "        },\n" +
                "        \"birthDate\": {\n" +
                "          \"type\": [\"string\", \"null\"],\n" +
                "          \"format\": \"date\"\n" +
                "        },\n" +
                "        \"gender\": {\n" +
                "          \"type\": \"string\",\n" +
                "          \"enum\": [\"D\", \"M\", \"U\", \"W\"]\n" +
                "        }\n" +
                "      }\n" +
                "    }\n" +
                "  }\n" +
                "}";
        JsonSchemaFactory factory = JsonSchemaFactory.getInstance(SpecVersion.VersionFlag.V202012);
        customerSchema = factory.getSchema(schema);
    }

    @BeforeEach
    void setUpBeforeEach()
    {
        customerJsonSchemaValidatorService = new CustomerJsonSchemaValidatorService();
    }

    @Test
    void jsonSchemaPathSet()
    {
        String expectedJsonSchemaPath = "schemas/customer.json";
        String jsonSchemaPath = customerJsonSchemaValidatorService.getJsonSchemaPath();

        assertEquals(expectedJsonSchemaPath, jsonSchemaPath, "Path should be set to 'schemas/customer.json'");
    }

    @Test
    void loadSchema()
    {
        JsonSchema loadedSchema = customerJsonSchemaValidatorService.getJsonSchema();
        assertEquals(customerSchema.toString(), loadedSchema.toString(), "Should be the same schema");
    }

    @Test
    void loadSchemaWrongPath()
    {
        customerJsonSchemaValidatorService.setJsonSchemaPath("some/random/path/lol");
        assertThrows(IllegalArgumentException.class, () -> customerJsonSchemaValidatorService.loadSchema());
    }
}
