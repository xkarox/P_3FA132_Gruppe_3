package dev.server.validator;

import com.networknt.schema.JsonSchemaFactory;
import com.networknt.schema.SpecVersion;

import java.io.InputStream;

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
