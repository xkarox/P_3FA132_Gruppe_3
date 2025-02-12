package dev.hv.database.services;

import dev.hv.database.DatabaseConnection;
import dev.hv.database.provider.InternalServiceProvider;
import dev.hv.model.classes.AuthenticationInformation;
import dev.hv.model.classes.Customer;

import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.SQLException;


public class AuthInformationService extends AbstractBaseService<AuthenticationInformation>
{
    public AuthInformationService(DatabaseConnection dbConnection, InternalServiceProvider provider)
    {
        super(dbConnection, provider, AuthenticationInformation.class);
    }

    public AuthInformationService(DatabaseConnection dbConnection)
    {
        super(dbConnection, null, AuthenticationInformation.class);
    }

    public static AuthenticationInformation CreateNewAuthInformation(Customer customer) throws ReflectiveOperationException, SQLException, IOException
    {
        return new AuthenticationInformation(customer.getId(), CryptoService.CreateNewUsername(customer), null);
    }

    public AuthenticationInformation getByUserName(String userName) throws ReflectiveOperationException, SQLException, IOException
    {
        return this.getAll().stream().filter(x -> x.getUsername().equals(userName)).findFirst().orElse(null);
    }

    @Override
    public AuthenticationInformation add(AuthenticationInformation item) throws ReflectiveOperationException, SQLException, IOException
    {
        if (item == null)
            throw new IllegalArgumentException("AuthItem is null and cannot be inserted.");

        String sqlStatement = "INSERT INTO " + item.getSerializedTableName() +
                " (id, username, password) VALUES (?, ?, ?);";

        try(PreparedStatement stmt = this._dbConnection.newPrepareStatement(sqlStatement)){
            stmt.setString(1, item.getId().toString());
            stmt.setString(2, item.getUsername());
            stmt.setString(3, CryptoService.hashStringWithSalt(item.getPassword()));
            this._dbConnection.executePreparedStatementCommand(stmt, 1);
        }

        return item;
    }

    public AuthenticationInformation addBlankUser(Customer customer) throws SQLException, ReflectiveOperationException, IOException
    {
        if (customer == null)
            throw new IllegalArgumentException("Customer is null and cannot be inserted.");

        AuthenticationInformation item = CreateNewAuthInformation(customer);

        String sqlStatement = "INSERT INTO " + item.getSerializedTableName() +
                " (id, username, password) VALUES (?, ?, ?);";

        try(PreparedStatement stmt = this._dbConnection.newPrepareStatement(sqlStatement)){
            stmt.setString(1, item.getId().toString());
            stmt.setString(2, item.getUsername());
            stmt.setString(3, null);
            this._dbConnection.executePreparedStatementCommand(stmt, 1);
        }

        return item;
    }

    @Override
    public AuthenticationInformation update(AuthenticationInformation item) throws SQLException
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

    public boolean DisplayNameAvailable(String displayName) throws ReflectiveOperationException, SQLException, IOException
    {
        return this.getAll().stream().noneMatch(x -> x.getUsername().equals(displayName));
    }

    public AuthenticationInformation getUserByName(String userName) throws ReflectiveOperationException, SQLException, IOException
    {
        return this.getAll().stream().filter(x -> x.getUsername().equals(userName)).findFirst().orElse(null);
    }

    @Override
    public void close() throws SQLException
    {
        _dbConnection.close();
    }
}
