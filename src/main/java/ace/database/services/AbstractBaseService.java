package ace.database.services;

import ace.database.DatabaseConnection;
import ace.database.provider.InternalServiceProvider;
import ace.database.intefaces.IBasicCrud;

public abstract class AbstractBaseService<T> implements IBasicCrud<T>, AutoCloseable
{
    protected final InternalServiceProvider _provider;

    protected final DatabaseConnection _dbConnection;

    protected AbstractBaseService(DatabaseConnection dbConnection, InternalServiceProvider provider)
    {
        _dbConnection = dbConnection;
        _provider = provider;
    }
}
