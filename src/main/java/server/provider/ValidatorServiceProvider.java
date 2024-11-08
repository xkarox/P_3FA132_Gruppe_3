package server.provider;

import server.validator.CustomerJsonSchemaValidatorService;
import server.validator.ReadingJsonSchemaValidationService;

public class ValidatorServiceProvider
{
    private CustomerJsonSchemaValidatorService _customerValidator;
    private ReadingJsonSchemaValidationService _readingValidator;

    public ValidatorServiceProvider()
    {
        _customerValidator = new CustomerJsonSchemaValidatorService();
        _readingValidator = new ReadingJsonSchemaValidationService();
    }

    public CustomerJsonSchemaValidatorService get_customerValidator()
    {
        return _customerValidator;
    }

    public ReadingJsonSchemaValidationService get_readingValidator()
    {
        return _readingValidator;
    }

    public void setCustomerValidator(CustomerJsonSchemaValidatorService customerValidator)
    {
        this._customerValidator = customerValidator;
    }
}
