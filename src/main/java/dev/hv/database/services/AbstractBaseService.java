package dev.hv.database.services;

import dev.hv.ResponseMessages;
import dev.hv.database.DatabaseConnection;
import dev.hv.database.intefaces.IBasicCrud;
import dev.hv.database.provider.InternalServiceProvider;
import dev.hv.model.IId;
import dev.hv.model.classes.Customer;
import dev.hv.model.classes.Reading;
import dev.hv.model.interfaces.IDbItem;

import java.io.IOException;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;
import java.util.TimerTask;
import java.util.UUID;

public abstract class AbstractBaseService<T> implements IBasicCrud<T>, AutoCloseable
{
    protected final InternalServiceProvider _provider;

    protected final DatabaseConnection _dbConnection;

    protected AbstractBaseService(DatabaseConnection dbConnection, InternalServiceProvider provider)
    {
        _dbConnection = dbConnection;
        _provider = provider;
    }

    public T getById(UUID id) throws ReflectiveOperationException, SQLException, IOException
    {
        var genericType = getGenericType();
        var result = this._dbConnection.getAllObjectsFromDbTableWithFilter(genericType, String.format("WHERE id = '%s'", id));
        if (result.size() > 1)
            throw new RuntimeException(ResponseMessages.ResultSizeError.toString(List.of(result.size())));
        if (result.isEmpty())
            return null;
        return (T) result.getFirst();
    }

    public List<T> getAll() throws ReflectiveOperationException, SQLException, IOException
    {
        return (List<T>) this._dbConnection.getAllObjectsFromDbTable(getGenericType());
    }

    public void remove(T item) throws SQLException
    {
        var genericType = getGenericType();
        removeDbItem((IDbItem & IId) item);
        if (genericType == Reading.class)
            cleanUpAfterCustomerRemove(((Reading) item).getCustomerId());
    }

    public <T extends IDbItem & IId> void removeDbItem(T item) throws SQLException
    {
        if (item.getId() == null)
            throw new IllegalArgumentException("Cannot delete a item without id");

        String sqlStatement = "DELETE FROM " + item.getSerializedTableName() + " WHERE id = ?";

        try (PreparedStatement stmt = this._dbConnection.newPrepareStatement(sqlStatement)) {
            stmt.setString(1, item.getId().toString());

            this._dbConnection.executePreparedStatementCommand(stmt, 1);
        }
    }

    private void cleanUpAfterCustomerRemove(UUID customerId) throws SQLException
    {
        String sqlStatement = "UPDATE " + new Reading().getSerializedTableName() + " " +
                "SET customerId=NULL WHERE customerId = ?";

        try (PreparedStatement stmt = this._dbConnection.newPrepareStatement(sqlStatement)) {
            stmt.setString(1, customerId.toString());

            this._dbConnection.executePreparedStatementCommand(stmt);
        }
    }

    protected Class<? extends IDbItem> getGenericType() {
        Type superClass = getClass().getGenericSuperclass();
        if (superClass instanceof ParameterizedType) {
            ParameterizedType parameterized = (ParameterizedType) superClass;
            Type type = parameterized.getActualTypeArguments()[0];
            if (type instanceof Class<?>) {
                return (Class<? extends IDbItem>) type; // Returns the actual class of T
            }
        }
        return null; // Fallback if type cannot be determined
    }

    public void close() throws SQLException
    {
        var genericType = getGenericType();
        if (this._provider != null)
        {
            this._provider.releaseDbConnection(this._dbConnection);
            if (genericType == Reading.class)
                this._provider.releaseReadingService((ReadingService) this);
            else if (genericType == Customer.class)
                this._provider.releaseCustomerService((CustomerService) this);
        }
    }
}
