package server.validator;

import ace.Utils;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.networknt.schema.JsonSchema;
import com.networknt.schema.ValidationMessage;

import java.util.Set;

public abstract class JsonSchemaValidatorServiceBase
{
    private JsonSchema _jsonSchema;
    private String _jsonSchemaPath;
    private static final ObjectMapper _objMapper = Utils.getObjectMapper();

    public boolean validate(String jsonString)
    {
        try
        {
            JsonNode jsonNode = getObjectMapper().readTree(jsonString);
            Set<ValidationMessage> validationMessages = _jsonSchema.validate(jsonNode);
            return validationMessages.isEmpty();
        } catch (JsonProcessingException e)
        {
            return false;
        }
    }

    protected JsonSchema getJsonSchema()
    {
        return this._jsonSchema;
    }

    protected void setJsonSchema(JsonSchema jsonSchema)
    {
        this._jsonSchema = jsonSchema;
    }

    protected String getJsonSchemaPath()
    {
        return this._jsonSchemaPath;
    }

    protected void setJsonSchemaPath(String path)
    {
        this._jsonSchemaPath = path;
    }

    abstract void loadSchema();

    ObjectMapper getObjectMapper(){ return _objMapper; }
}
