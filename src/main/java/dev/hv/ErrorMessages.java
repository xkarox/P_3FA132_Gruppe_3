package dev.hv;

public enum ErrorMessages
{
    SqlUpdate("Expected: either (1) the row count for SQL Data Manipulation Language (DML) statements or (2) 0 for SQL statements that return nothing"),
    ServicesNotAvailable("The requested service is not available, increase the max connections or wait for a service to be released, alternatively enable multithreading support"),;

    private final String _errorMessage;

    ErrorMessages(String errorName)
    {
        this._errorMessage = errorName;
    }

    @Override
    public String toString()
    {
        return _errorMessage;
    }
}

