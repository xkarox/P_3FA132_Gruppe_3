package dev.hv.database.services;

import dev.hv.database.DatabaseConnection;
import dev.hv.database.provider.InternalServiceProvider;
import dev.hv.model.classes.Authentification.AuthUser;
import dev.hv.model.classes.Authentification.AuthUserPermissions;
import dev.hv.model.classes.Customer;
import dev.hv.model.enums.UserRoles;
import dev.provider.ServiceProvider;

import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.UUID;


public class AuthUserService extends AbstractBaseService<AuthUser>
{
    public AuthUserService(DatabaseConnection dbConnection, InternalServiceProvider provider)
    {
        super(dbConnection, provider, AuthUser.class);
    }

    public AuthUserService(DatabaseConnection dbConnection)
    {
        super(dbConnection, null, AuthUser.class);
        try
        {
            this._dbConnection.openConnection();
        } catch (IOException | SQLException e)
        {
            throw new RuntimeException(e);
        }
    }

    public static AuthUser CreateNewAuthInformation(Customer customer) throws ReflectiveOperationException, SQLException, IOException
    {
        return new AuthUser(customer.getId(), CryptoService.CreateNewUsername(customer), null);
    }

    public AuthUser getByUserName(String userName) throws ReflectiveOperationException, SQLException, IOException
    {
        return this.getAll().stream().filter(x -> x.getUsername().equals(userName)).findFirst().orElse(null);
    }

    @Override
    public AuthUser add(AuthUser item) throws ReflectiveOperationException, SQLException, IOException
    {
        if (item == null)
            throw new IllegalArgumentException("AuthItem is null and cannot be inserted.");

        String sqlStatement = "INSERT INTO " + item.getSerializedTableName() +
                " (id, username, password, role) VALUES (?, ?, ?, ?);";

        try(PreparedStatement stmt = this._dbConnection.newPrepareStatement(sqlStatement)){
            stmt.setString(1, item.getId().toString());
            stmt.setString(2, item.getUsername());
            stmt.setString(3, CryptoService.hashStringWithSalt(item.getPassword()));
            stmt.setString(4, String.valueOf(item.getRole().ordinal()));
            this._dbConnection.executePreparedStatementCommand(stmt, 1);
        }

        if (item.getPermissions() != null){
            try(UserPermissionService ups = ServiceProvider.getUserPermissionService()){
                for (var permission : item.getPermissions()){
                    ups.add(new AuthUserPermissions(item.getId(), permission));
                }
            }
        }

        return item;
    }

    // ToDo: Why oh why
    public AuthUser addBlankUser(Customer customer) throws SQLException, ReflectiveOperationException, IOException
    {
        if (customer == null)
            throw new IllegalArgumentException("Customer is null and cannot be inserted.");

        AuthUser item = CreateNewAuthInformation(customer);

        String sqlStatement = "INSERT INTO " + item.getSerializedTableName() +
                " (id, username, password, role) VALUES (?, ?, ?, ?);";

        try(PreparedStatement stmt = this._dbConnection.newPrepareStatement(sqlStatement)){
            stmt.setString(1, item.getId().toString());
            stmt.setString(2, item.getUsername());
            stmt.setString(3, null);
            stmt.setString(4, UserRoles.USER.toString());
            this._dbConnection.executePreparedStatementCommand(stmt, 1);
        }

        return item;
    }

    @Override
    public AuthUser update(AuthUser item) throws SQLException
    {
        if (item == null)
            throw new IllegalArgumentException("AuthItem is null and cannot be inserted.");

        String sqlStatement = "Update " + item.getSerializedTableName() +
                " SET username = ?, SET password = ? WHERE Id = ?;";

        try(PreparedStatement stmt = this._dbConnection.newPrepareStatement(sqlStatement)){
            stmt.setString(1, item.getUsername());
            stmt.setString(2, CryptoService.hashStringWithSalt(item.getPassword()));
            stmt.setString(3, item.getId().toString());
            this._dbConnection.executePreparedStatementCommand(stmt, 1);
        }

        return item;
    }

    @Override
    public void remove(AuthUser item) throws SQLException
    {
        super.remove(item);
        if (item.getId() != null)
            cleanUpAfterUserRemove(item.getId());
        else throw new IllegalArgumentException("Cannot delete a user without id"); // ToDo: better handling
    }

    private void cleanUpAfterUserRemove(UUID authUserId) throws SQLException
    {
        try(UserPermissionService ups = ServiceProvider.getUserPermissionService()){
            for(var permission : ups.getAllById(authUserId))
            {
                ups.remove(permission);
            }
        } catch (ReflectiveOperationException | IOException e)
        {
            throw new RuntimeException(e);
        }
    }

    public boolean DisplayNameAvailable(String displayName) throws ReflectiveOperationException, SQLException, IOException
    {
        return this.getAll().stream().noneMatch(x -> x.getUsername().equals(displayName));
    }

    public AuthUser getUserByName(String userName) throws ReflectiveOperationException, SQLException, IOException
    {
        return this.getAll().stream().filter(x -> x.getUsername().equals(userName)).findFirst().orElse(null);
    }

    public boolean checkIfAuthDatabaseExists() throws SQLException
    {
        return this._dbConnection.getAllTableNames().contains(new AuthUser().getSerializedTableName().toLowerCase());
    }

    @Override
    public void close() throws SQLException
    {
        _dbConnection.close();
    }
}
