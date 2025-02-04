package dev.server.validator;

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
}
