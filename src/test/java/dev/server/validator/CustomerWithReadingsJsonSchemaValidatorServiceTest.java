package dev.server.validator;

import com.networknt.schema.JsonSchema;
import com.networknt.schema.JsonSchemaFactory;
import com.networknt.schema.SpecVersion;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class CustomerWithReadingsJsonSchemaValidatorServiceTest
{
    static JsonSchema customerWithReadingsSchema;

    @BeforeAll
    static void setUp()

    {
        String schema = "{\n" +
                "  \"title\": \"JSON-Schema Customer with readings\",\n" +
                "  \"type\": \"object\",\n" +
                "  \"required\": [\n" +
                "    \"customer\",\n" +
                "    \"readings\"\n" +
                "  ],\n" +
                "  \"properties\": {\n" +
                "    \"customer\": {\n" +
                "      \"type\": \"object\",\n" +
                "      \"required\": [\n" +
                "        \"id\",\n" +
                "        \"firstName\",\n" +
                "        \"lastName\",\n" +
                "        \"gender\"\n" +
                "      ],\n" +
                "      \"properties\": {\n" +
                "        \"id\": {\n" +
                "          \"type\": \"string\"\n" +
                "        },\n" +
                "        \"firstName\": {\n" +
                "          \"type\": \"string\"\n" +
                "        },\n" +
                "        \"lastName\": {\n" +
                "          \"type\": \"string\"\n" +
                "        },\n" +
                "        \"birthDate\": {\n" +
                "          \"type\": \"string\",\n" +
                "          \"format\": \"date\"\n" +
                "        },\n" +
                "        \"gender\": {\n" +
                "          \"type\": \"string\",\n" +
                "          \"enum\": [\n" +
                "            \"D\",\n" +
                "            \"M\",\n" +
                "            \"U\",\n" +
                "            \"W\"\n" +
                "          ]\n" +
                "        }\n" +
                "      }\n" +
                "    }\n" +
                "  },\n" +
                "  \"readings\": {\n" +
                "    \"type\": \"array\",\n" +
                "    \"items\": {\n" +
                "      \"type\": \"object\",\n" +
                "      \"required\": [\n" +
                "        \"id\",\n" +
                "        \"customer\",\n" +
                "        \"dateOfReading\",\n" +
                "        \"meterId\",\n" +
                "        \"substitute\",\n" +
                "        \"metercount\",\n" +
                "        \"kindOfMeter\"\n" +
                "      ],\n" +
                "      \"properties\": {\n" +
                "        \"id\": {\n" +
                "          \"type\": \"string\"\n" +
                "        },\n" +
                "        \"customer\": {\n" +
                "          \"type\": \"null\"\n" +
                "        },\n" +
                "        \"dateOfReading\": {\n" +
                "          \"type\": \"string\",\n" +
                "          \"format\": \"date\"\n" +
                "        },\n" +
                "        \"comment\": {\n" +
                "          \"type\": [\n" +
                "            \"string\",\n" +
                "            \"null\"\n" +
                "          ]\n" +
                "        },\n" +
                "        \"meterId\": {\n" +
                "          \"type\": \"string\"\n" +
                "        },\n" +
                "        \"substitute\": {\n" +
                "          \"type\": \"boolean\"\n" +
                "        },\n" +
                "        \"metercount\": {\n" +
                "          \"type\": \"number\"\n" +
                "        },\n" +
                "        \"kindOfMeter\": {\n" +
                "          \"type\": \"string\",\n" +
                "          \"enum\": [\n" +
                "            \"HEIZUNG\",\n" +
                "            \"STROM\",\n" +
                "            \"WASSER\",\n" +
                "            \"UNBEKANNT\"\n" +
                "          ]\n" +
                "        }\n" +
                "      }\n" +
                "    }\n" +
                "  }\n" +
                "}\n";
        JsonSchemaFactory factory = JsonSchemaFactory.getInstance(SpecVersion.VersionFlag.V202012);
        customerWithReadingsSchema = factory.getSchema(schema);
    }

    @Test
    void jsonSchemaPathSet()
    {
        String expectedJsonSchemaPath = "schemas/customerWithReadings.json";
        String jsonSchemaPath = CustomerWithReadingsJsonSchemaValidatorService.getInstance().getJsonSchemaPath();

        assertEquals(expectedJsonSchemaPath, jsonSchemaPath, "Path should be set to 'schemas/customer.json'");
    }

    @Test
    void loadSchema()
    {
        JsonSchema loadedSchema = CustomerWithReadingsJsonSchemaValidatorService.getInstance().getJsonSchema();
        assertEquals(customerWithReadingsSchema.toString(), loadedSchema.toString(), "Should be the same schema");
    }

    @Test
    void loadSchemaWrongPath()
    {
        CustomerWithReadingsJsonSchemaValidatorService.getInstance().setJsonSchemaPath("some/random/path/lol");
        assertThrows(IllegalArgumentException.class, () ->
                CustomerWithReadingsJsonSchemaValidatorService.getInstance().loadSchema(CustomerWithReadingsJsonSchemaValidatorService.class));
    }
}
