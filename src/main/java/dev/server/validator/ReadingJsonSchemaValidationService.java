package dev.server.validator;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.networknt.schema.JsonSchema;
import com.networknt.schema.JsonSchemaFactory;
import com.networknt.schema.SpecVersion;
import com.networknt.schema.ValidationMessage;
import dev.hv.Utils;

import java.io.InputStream;
import java.util.Set;

public class ReadingJsonSchemaValidationService extends JsonSchemaValidatorServiceBase
{
    static ReadingJsonSchemaValidationService instance;

    static {
        instance = new ReadingJsonSchemaValidationService();
        instance.setJsonSchemaPath("schemas/reading.json");
        instance.loadSchema(ReadingJsonSchemaValidationService.class);
    }

    public static JsonSchemaValidatorServiceBase getInstance()
    {
        return instance;
    }


    @Override
    public boolean validate(String jsonString)
    {
        try
        {
            JsonNode jsonNode = Utils.getObjectMapper().readTree(jsonString);
            Set<ValidationMessage> validationMessages = this._jsonSchema.validate(jsonNode);
            return !validationMessages.isEmpty();
        } catch (JsonProcessingException e)
        {
            return false;
        }
    }

    @Override
    public JsonSchema getJsonSchema()
    {
        return this._jsonSchema;
    }

    @Override
    public void setJsonSchema(JsonSchema jsonSchema)
    {
        this._jsonSchema = jsonSchema;
    }

    @Override
    public String getJsonSchemaPath()
    {
        return this._jsonSchemaPath;
    }

    @Override
    public void loadSchema(Class<? extends JsonSchemaValidatorServiceBase> currClass)
    {
        InputStream schemaStream = currClass.getClassLoader().getResourceAsStream(getJsonSchemaPath());
        if (schemaStream == null)
        {
            throw new IllegalArgumentException("Schema file not found");
        }
        JsonSchemaFactory factory = JsonSchemaFactory.getInstance(SpecVersion.VersionFlag.V202012);
        this.setJsonSchema(factory.getSchema(schemaStream));
    }
}
