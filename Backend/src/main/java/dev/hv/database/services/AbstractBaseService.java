package dev.hv.database.services;

import dev.hv.ResponseMessages;
import dev.hv.database.DatabaseConnection;
import dev.hv.database.intefaces.IBasicCrud;
import dev.hv.database.provider.InternalServiceProvider;
import dev.hv.model.IId;
import dev.hv.model.interfaces.IDbItem;

import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;
import java.util.UUID;

public abstract class AbstractBaseService<T extends IDbItem & IId> implements IBasicCrud<T>, AutoCloseable
{
    protected final InternalServiceProvider _provider;

    protected final DatabaseConnection _dbConnection;
    private final Class<T> _instanceType; // cannot read T for some reason

    protected AbstractBaseService(DatabaseConnection dbConnection, InternalServiceProvider provider, Class<T> type)
    {
        _dbConnection = dbConnection;
        _provider = provider;
        _instanceType = type;
    }

    public T getById(UUID id) throws ReflectiveOperationException, SQLException, IOException
    {
        var result = this._dbConnection.getAllObjectsFromDbTableWithFilter(_instanceType, String.format("WHERE id = '%s'", id));
        if (result.size() > 1)
            throw new RuntimeException(ResponseMessages.ResultSizeError.toString(List.of(result.size())));
        if (result.isEmpty())
            return null;
        return result.getFirst();
    }

    public List<T> getAll() throws ReflectiveOperationException, SQLException, IOException
    {

        return this._dbConnection.getAllObjectsFromDbTable(_instanceType);
    }

    public void remove(T item) throws SQLException
    {
        if (item.getId() == null)
            throw new IllegalArgumentException("Cannot delete a item without id"); // ToDo: better handling

        String sqlStatement = "DELETE FROM " + item.getSerializedTableName() + " WHERE id = ?";

        try (PreparedStatement stmt = this._dbConnection.newPrepareStatement(sqlStatement)) {
            stmt.setString(1, item.getId().toString());

            this._dbConnection.executePreparedStatementCommand(stmt, 1);
        }
    }

    public abstract void close() throws SQLException;
}
