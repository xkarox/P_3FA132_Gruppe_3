package server.provider;

import server.validator.CustomerJsonSchemaValidatorService;
import server.validator.ReadingJsonSchemaValidationService;

public class ValidatorServiceProvider
{
    private final CustomerJsonSchemaValidatorService customerValidator;
    private final ReadingJsonSchemaValidationService readingValidator;

    public ValidatorServiceProvider()
    {
        customerValidator = new CustomerJsonSchemaValidatorService();
        readingValidator = new ReadingJsonSchemaValidationService();
    }

    public CustomerJsonSchemaValidatorService getCustomerValidator()
    {
        return customerValidator;
    }

    public ReadingJsonSchemaValidationService getReadingValidator()
    {
        return readingValidator;
    }
}
