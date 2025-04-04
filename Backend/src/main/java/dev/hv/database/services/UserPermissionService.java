package dev.hv.database.services;

import dev.hv.database.DatabaseConnection;
import dev.hv.database.provider.InternalServiceProvider;
import dev.hv.model.classes.Authentification.AuthUserPermissions;

import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;
import java.util.UUID;

public class UserPermissionService extends AbstractBaseService<AuthUserPermissions>
{
    public UserPermissionService(DatabaseConnection dbConnection, InternalServiceProvider provider)
    {
        super(dbConnection, provider, AuthUserPermissions.class);
    }

    public UserPermissionService(DatabaseConnection dbConnection)
    {
        super(dbConnection, null, AuthUserPermissions.class);
        try
        {
            if (!this._dbConnection.getConnection().isClosed())
                return;
            this._dbConnection.openConnection();
        } catch (IOException | SQLException e)
        {
            throw new RuntimeException(e);
        }
    }

    public List<AuthUserPermissions> getAllById(UUID id) throws ReflectiveOperationException, SQLException, IOException
    {
        var result = this._dbConnection.getAllObjectsFromDbTableWithFilter(AuthUserPermissions.class, String.format("WHERE id = '%s'", id));
        if (result.isEmpty())
            return null;
        return result;
    }

    @Override
    public void close() throws SQLException
    {
        if (this._provider != null)
        {
            this._provider.releaseDbConnection(this._dbConnection);
        }
        else {
            this._dbConnection.close();
        }
    }

    @Override
    public AuthUserPermissions add(AuthUserPermissions item) throws ReflectiveOperationException, SQLException, IOException
    {
        if (item == null)
            throw new IllegalArgumentException("AuthUserPermissions is null and cannot be inserted.");

        String sqlStatement = "INSERT INTO " + item.getSerializedTableName() +
                " (id, permission) VALUES (?, ?);";

        try(PreparedStatement stmt = this._dbConnection.newPrepareStatement(sqlStatement)){
            stmt.setString(1, item.getId().toString());
            stmt.setString(2, String.valueOf(item.getPermission().ordinal()));
            this._dbConnection.executePreparedStatementCommand(stmt, 1);
        }

        return item;
    }

    @Override
    public AuthUserPermissions update(AuthUserPermissions item) throws SQLException
    {
        if (item == null)
            throw new IllegalArgumentException("AuthUserPermissions is null and cannot be inserted.");

        String sqlStatement = "Update " + item.getSerializedTableName() +
                " SET permission = ? WHERE id = ?;";

        try(PreparedStatement stmt = this._dbConnection.newPrepareStatement(sqlStatement)){
            stmt.setString(1, String.valueOf(item.getPermission().ordinal()));
            stmt.setString(2, item.getId().toString());
            this._dbConnection.executePreparedStatementCommand(stmt, 1);
        }

        return item;
    }
}
