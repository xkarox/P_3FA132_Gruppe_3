package dev.server.validator;

import com.networknt.schema.*;
import dev.hv.Utils;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.InputStream;
import java.util.Set;

public abstract class JsonSchemaValidatorServiceBase
{
    protected JsonSchema _jsonSchema;
    protected String _jsonSchemaPath;

    public abstract boolean validate(String jsonString);

    public abstract JsonSchema getJsonSchema();

    public abstract void setJsonSchema(JsonSchema jsonSchema);

    public abstract String getJsonSchemaPath();

    public void setJsonSchemaPath(String path)
    {
        this._jsonSchemaPath = path;
    }

    public abstract void loadSchema(Class<? extends JsonSchemaValidatorServiceBase> currClass);
}
