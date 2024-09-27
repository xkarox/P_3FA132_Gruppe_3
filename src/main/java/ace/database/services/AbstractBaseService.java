package ace.database.services;

import ace.database.DatabaseConnection;
import ace.database.intefaces.IBasicCrud;

public abstract class AbstractBaseService<T> implements IBasicCrud<T>
{
    protected final DatabaseConnection _dbConnection;

    protected AbstractBaseService(DatabaseConnection dbConnection)
    {
        _dbConnection = dbConnection;
    }
}
