package server.validator;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.networknt.schema.JsonSchema;
import com.networknt.schema.JsonSchemaFactory;
import com.networknt.schema.SpecVersion;
import com.networknt.schema.ValidationMessage;

import java.io.InputStream;
import java.util.Set;

public class CustomerJsonSchemaValidatorService extends JsonSchemaValidatorServiceBase
{

    public CustomerJsonSchemaValidatorService()
    {
        this.setJsonSchemaPath("schemas/customer.json");
        this.loadSchema();
    }

    @Override
    void loadSchema()
    {
        InputStream schemaStream = getClass().getClassLoader().getResourceAsStream(this.getJsonSchemaPath());
        if (schemaStream == null)
        {
            throw new IllegalArgumentException("Schema file not found");
        }
        JsonSchemaFactory factory = JsonSchemaFactory.getInstance(SpecVersion.VersionFlag.V202012);
        this.setJsonSchema(factory.getSchema(schemaStream));
    }
}
