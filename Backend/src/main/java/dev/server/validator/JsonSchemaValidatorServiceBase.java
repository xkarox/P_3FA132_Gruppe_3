package dev.server.validator;

import com.networknt.schema.*;
import dev.hv.ResponseMessages;
import dev.hv.Utils;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import java.io.InputStream;
import java.util.Set;

public abstract class JsonSchemaValidatorServiceBase
{
    protected JsonSchema _jsonSchema;
    protected String _jsonSchemaPath;

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

    public JsonSchema getJsonSchema()
    {
        return this._jsonSchema;
    }

    public void setJsonSchema(JsonSchema jsonSchema)
    {
        this._jsonSchema = jsonSchema;
    }

    public String getJsonSchemaPath()
    {
        return this._jsonSchemaPath;
    }

    public void setJsonSchemaPath(String path)
    {
        this._jsonSchemaPath = path;
    }

    public void loadSchema(Class<? extends JsonSchemaValidatorServiceBase> currClass)
    {
        InputStream schemaStream = currClass.getClassLoader().getResourceAsStream(getJsonSchemaPath());
        if (schemaStream == null)
        {
            throw new IllegalArgumentException(ResponseMessages.JsonSchemaFileNotFound.toString());
        }
        JsonSchemaFactory factory = JsonSchemaFactory.getInstance(SpecVersion.VersionFlag.V202012);
        this.setJsonSchema(factory.getSchema(schemaStream));
    }
}
