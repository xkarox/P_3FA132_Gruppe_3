package dev.hv;

import java.util.List;
import java.util.stream.Collectors;

public enum ResponseMessages
{
    // Database
    SqlUpdate("Expected: either (1) the row count for SQL Data Manipulation Language (DML) statements or (2) 0 for SQL statements that return nothing"),
    ResultSizeError("Expected size of result be equal to 1, but found %s"),
    ServicesNotAvailable("The requested service is not available, increase the max connections or wait for a service to be released, alternatively enable multithreading support"),
    DbConnectionNotRegistered("Connection was not registered with the current service provider."),
    DbFieldTypeNotSupported("Field type not supported for object creation"),

    // Controller error messages
    ControllerBadRequest("Invalid data provided"),
    ControllerNotFound("Resource not found"),
    ControllerInternalError("Internal Server IOError"),

    // Controller status messages
    ControllerUpdateSuccess("Resource successfully updated"),

    // Models
    ModelParameterNull("Parameter %s cannot be null"),

    // Json
    JsonSchemaFileNotFound("Schema file not found"),

    // Reading Query Endpoint
    InvalidDateFormatProvided("Invalid Date format provided. yyyy-MM-dd expected."),
    InvalidKindOfMeterProvided("Unknown Kind of Meter provided")
    ;



    private final String _errorMessage;

    ResponseMessages(String errorName)
    {
        this._errorMessage = errorName;
    }

    @Override
    public String toString()
    {
        return _errorMessage;
    }

    public String toString(List<?> values)
    {
        String joinedValues = values.stream()
                .map(Object::toString)
                .collect(Collectors.joining(", "));
        return String.format(this._errorMessage, joinedValues);
    }
}

