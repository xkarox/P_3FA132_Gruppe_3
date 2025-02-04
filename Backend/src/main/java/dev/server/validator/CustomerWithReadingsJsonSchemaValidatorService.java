package dev.server.validator;

public class CustomerWithReadingsJsonSchemaValidatorService extends JsonSchemaValidatorServiceBase
{
    static CustomerWithReadingsJsonSchemaValidatorService instance;

    static
    {
        instance = new CustomerWithReadingsJsonSchemaValidatorService();
        instance.setJsonSchemaPath("schemas/customerWithReadings.json");
        instance.loadSchema(CustomerWithReadingsJsonSchemaValidatorService.class);
    }

    public static JsonSchemaValidatorServiceBase getInstance()
    {
        return instance;
    }

}
