package server.filter;

import com.networknt.schema.JsonSchema;

public abstract class JsonSchemaValidatorFilterBase
{
    private JsonSchema _jsonSchema;

    abstract JsonSchema loadSchema();
}
