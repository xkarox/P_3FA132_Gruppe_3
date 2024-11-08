package dev.hv.database.services;

import dev.hv.database.DatabaseConnection;
import dev.hv.database.provider.InternalServiceProvider;
import dev.hv.database.intefaces.IBasicCrud;

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
