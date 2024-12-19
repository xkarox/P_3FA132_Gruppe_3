package dev.server.validator;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.networknt.schema.JsonSchema;
import com.networknt.schema.JsonSchemaFactory;
import com.networknt.schema.SpecVersion;
import com.networknt.schema.ValidationMessage;
import dev.hv.ResponseMessages;
import dev.hv.Utils;

import java.io.InputStream;
import java.util.Set;

public class CustomerJsonSchemaValidatorService extends JsonSchemaValidatorServiceBase
{
    static CustomerJsonSchemaValidatorService instance;

    static {
        instance = new CustomerJsonSchemaValidatorService();
        instance.setJsonSchemaPath("schemas/customer.json");
        instance.loadSchema(CustomerJsonSchemaValidatorService.class);
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
            throw new IllegalArgumentException(ResponseMessages.JsonSchemaFileNotFound.toString());
        }
        JsonSchemaFactory factory = JsonSchemaFactory.getInstance(SpecVersion.VersionFlag.V202012);
        this.setJsonSchema(factory.getSchema(schemaStream));
    }
}
