package dev.server.provider;

import dev.server.validator.CustomerJsonSchemaValidatorService;
import dev.server.validator.ReadingJsonSchemaValidationService;

public class ValidatorServiceProvider
{
    private CustomerJsonSchemaValidatorService _customerValidator;
    private ReadingJsonSchemaValidationService _readingValidator;

    public ValidatorServiceProvider()
    {
        _customerValidator = new CustomerJsonSchemaValidatorService();
        _readingValidator = new ReadingJsonSchemaValidationService();
    }

    public CustomerJsonSchemaValidatorService getCustomerValidator()
    {
        return _customerValidator;
    }

    public ReadingJsonSchemaValidationService getReadingValidator()
    {
        return _readingValidator;
    }

    public void setCustomerValidator(CustomerJsonSchemaValidatorService customerValidator)
    {
        this._customerValidator = customerValidator;
    }
}
