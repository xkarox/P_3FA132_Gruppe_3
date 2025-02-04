package dev.server.validator;

public class CustomerJsonSchemaValidatorService extends JsonSchemaValidatorServiceBase
{
    static CustomerJsonSchemaValidatorService instance;

    static
    {
        instance = new CustomerJsonSchemaValidatorService();
        instance.setJsonSchemaPath("schemas/customer.json");
        instance.loadSchema(CustomerJsonSchemaValidatorService.class);
    }

    public static JsonSchemaValidatorServiceBase getInstance()
    {
        return instance;
    }
}
