package ace;

public enum ErrorMessages
{
    SqlUpdate("Expected: either (1) the row count for SQL Data Manipulation Language (DML) statements or (2) 0 for SQL statements that return nothing");

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

