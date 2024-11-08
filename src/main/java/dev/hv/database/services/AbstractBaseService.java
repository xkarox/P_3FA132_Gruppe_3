package dev.hv.database.services;

import dev.hv.database.DatabaseConnection;
import dev.hv.database.intefaces.IBasicCrud;
import dev.hv.database.provider.InternalServiceProvider;
import dev.hv.model.IId;
import dev.hv.model.interfaces.IDbItem;

import java.sql.PreparedStatement;
import java.sql.SQLException;

public abstract class AbstractBaseService<T> implements IBasicCrud<T>, AutoCloseable
{
    protected final InternalServiceProvider _provider;

    protected final DatabaseConnection _dbConnection;

    protected AbstractBaseService(DatabaseConnection dbConnection, InternalServiceProvider provider)
    {
        _dbConnection = dbConnection;
        _provider = provider;
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
}
