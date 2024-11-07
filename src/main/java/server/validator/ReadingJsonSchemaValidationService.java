package server.validator;

import com.networknt.schema.JsonSchemaFactory;
import com.networknt.schema.SpecVersion;

import java.io.InputStream;

public class ReadingJsonSchemaValidationService extends JsonSchemaValidatorServiceBase
{
    public ReadingJsonSchemaValidationService()
    {
        this.setJsonSchemaPath("schemas/reading.json");
        this.loadSchema();
    }

    @Override
    void loadSchema()
    {
        InputStream schemaStream = getClass().getClassLoader().getResourceAsStream(this.getJsonSchemaPath());
        if (schemaStream == null) {
            throw new IllegalArgumentException("Schema file not found");
        }
        JsonSchemaFactory factory = JsonSchemaFactory.getInstance(SpecVersion.VersionFlag.V202012);
        this.setJsonSchema(factory.getSchema(schemaStream));
    }
}
