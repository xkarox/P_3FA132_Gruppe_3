package dev.server.validator;

import com.networknt.schema.JsonSchema;
import com.networknt.schema.JsonSchemaFactory;
import com.networknt.schema.SpecVersion;
import dev.server.validator.ReadingJsonSchemaValidationService;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class ReadingJsonSchemaValidationServiceTest
{
    static JsonSchema customerSchema;
    ReadingJsonSchemaValidationService readingJsonSchemaValidatorService;

    @BeforeAll
    static void setUp()
    {
        String schema = "{\n" +
                "  \"title\": \"JSON-Schema Reading\",\n" +
                "  \"type\": \"object\",\n" +
                "  \"required\": [\n" +
                "    \"reading\"\n" +
                "  ],\n" +
                "  \"properties\": {\n" +
                "    \"reading\": {\n" +
                "      \"type\": \"object\",\n" +
                "      \"required\": [\n" +
                "        \"customer\",\n" +
                "        \"dateOfReading\",\n" +
                "        \"meterId\",\n" +
                "        \"substitute\",\n" +
                "        \"meterCount\",\n" +
                "        \"kindOfMeter\"\n" +
                "      ],\n" +
                "      \"properties\": {\n" +
                "        \"id\": {\n" +
                "          \"type\": [\"string\", \"null\"]\n" +
                "        },\n" +
                "        \"customer\": {\n" +
                "          \"anyOf\": [\n" +
                "            {\n" +
                "              \"type\": \"object\",\n" +
                "              \"required\": [\n" +
                "                \"firstName\",\n" +
                "                \"lastName\",\n" +
                "                \"gender\"\n" +
                "              ],\n" +
                "              \"properties\": {\n" +
                "                \"uuid\": {\n" +
                "                  \"type\": [\"string\", \"null\"]\n" +
                "                },\n" +
                "                \"firstName\": {\n" +
                "                  \"type\": \"string\"\n" +
                "                },\n" +
                "                \"lastName\": {\n" +
                "                  \"type\": \"string\"\n" +
                "                },\n" +
                "                \"birthDate\": {\n" +
                "                  \"type\": [\"string\", \"null\"],\n" +
                "                  \"format\": \"date\"\n" +
                "                },\n" +
                "                \"gender\": {\n" +
                "                  \"type\": \"string\",\n" +
                "                  \"enum\": [\"D\", \"M\", \"U\", \"W\"]\n" +
                "                }\n" +
                "              }\n" +
                "            },\n" +
                "            {\n" +
                "              \"type\": \"null\"\n" +
                "            }\n" +
                "          ]\n" +
                "        },\n" +
                "        \"dateOfReading\": {\n" +
                "          \"type\": \"string\",\n" +
                "          \"format\": \"date\"\n" +
                "        },\n" +
                "        \"comment\": {\n" +
                "          \"type\": [\"string\", \"null\"]\n" +
                "        },\n" +
                "        \"meterId\": {\n" +
                "          \"type\": \"string\"\n" +
                "        },\n" +
                "        \"substitute\": {\n" +
                "          \"type\": \"boolean\"\n" +
                "        },\n" +
                "        \"meterCount\": {\n" +
                "          \"type\": \"number\"\n" +
                "        },\n" +
                "        \"kindOfMeter\": {\n" +
                "          \"type\": \"string\",\n" +
                "          \"enum\": [\"HEIZUNG\", \"UNBEKANNT\", \"STROM\", \"WASSER\"]\n" +
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
        readingJsonSchemaValidatorService = new ReadingJsonSchemaValidationService();
    }

    @Test
    void jsonSchemaPathSet()
    {
        String expectedJsonSchemaPath = "schemas/reading.json";
        String jsonSchemaPath = readingJsonSchemaValidatorService.getJsonSchemaPath();

        assertEquals(expectedJsonSchemaPath, jsonSchemaPath, "Path should be set to 'schemas/customer.json'");
    }

    @Test
    void loadSchema()
    {
        JsonSchema loadedSchema = readingJsonSchemaValidatorService.getJsonSchema();
        assertEquals(customerSchema.toString(), loadedSchema.toString(), "Should be the same schema");
    }

    @Test
    void loadSchemaWrongPath()
    {
        readingJsonSchemaValidatorService.setJsonSchemaPath("some/random/path/lol");
        assertThrows(IllegalArgumentException.class, () -> readingJsonSchemaValidatorService.loadSchema());
    }
}
