package dev.hv.database.services;

import com.sun.jna.platform.win32.Guid;
import dev.hv.database.DatabaseConnection;
import dev.hv.database.provider.InternalServiceProvider;
import dev.hv.model.classes.Authentification.AuthUser;
import dev.hv.model.classes.Customer;
import dev.hv.model.classes.Reading;
import dev.provider.ServiceProvider;

import java.io.IOException;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.*;
import java.util.List;
import java.util.UUID;

public class CustomerService extends AbstractBaseService<Customer>
{
    public CustomerService(DatabaseConnection dbConnection, InternalServiceProvider provider)
    {
        super(dbConnection, provider, Customer.class);
    }

    public CustomerService(DatabaseConnection dbConnection)
    {
        super(dbConnection, null, Customer.class);
    }


    @Override
    // Req. Nr.: 3
    public Customer add(Customer item) throws IllegalArgumentException, SQLException
    {
        if (item == null)
            throw new IllegalArgumentException("Customer is null and cannot be inserted.");

        String sqlStatement = CustomerSqlQuery(item.getSerializedTableName());
        try (PreparedStatement stmt = this._dbConnection.newPrepareStatement(sqlStatement))
        {
            addCustomerToPreparedStatement(stmt, item);
            this._dbConnection.executePreparedStatementCommand(stmt, 1);
        }

        return item;
    }

    public static String CustomerSqlQuery(String tableName)
    {
        return "INSERT INTO " + tableName + " (id, firstName, lastName, birthDate, gender) VALUES (?, ?, ?, ?, ?);";
    }

    public void addCustomerToPreparedStatement(PreparedStatement stmt, Customer item) throws SQLException
    {
        stmt.setString(1, item.getId().toString());
        stmt.setString(2, item.getFirstName());
        stmt.setString(3, item.getLastName());
        stmt.setDate(4, item.getBirthDate() != null ? Date.valueOf(item.getBirthDate()) : null);
        stmt.setString(5, String.valueOf(item.getGender().ordinal()));
    }

    public void addBatch(List<Customer> items) throws SQLException
    {
        if (items == null || items.isEmpty())
            throw new IllegalArgumentException("Customers are null or empty and cannot be inserted.");

        String tableName = items.getFirst().getSerializedTableName();
        String sqlStatement = CustomerSqlQuery(tableName);

        try (PreparedStatement stmt = this._dbConnection.newPrepareStatement(sqlStatement))
        {
            for (Customer item : items)
            {
                addCustomerToPreparedStatement(stmt, item);
                stmt.addBatch();
            }

            boolean commitState = this._dbConnection.getConnection().getAutoCommit();
            try{
                // Disable auto commit for rollback on failure in batch
                this._dbConnection.getConnection().setAutoCommit(false);
                stmt.executeBatch();
                this._dbConnection.getConnection().commit();
            } catch (SQLException e) {
                this._dbConnection.getConnection().rollback();
                throw new SQLException("Error, rolling back commits");

            } finally {
                // Reset to original state
                this._dbConnection.getConnection().setAutoCommit(commitState);
            }
        }
    }

    @Override
    // Req. Nr.: 5
    public Customer update(Customer item) throws IllegalArgumentException, SQLException
    {
        if (item.getId() == null)
            throw new IllegalArgumentException("Cannot update customer without id");

        String sqlStatement = "UPDATE " + item.getSerializedTableName() + " " +
                "SET firstName = ?, lastName = ?, birthDate = ?, gender = ? WHERE id = ?";

        try (PreparedStatement stmt = this._dbConnection.newPrepareStatement(sqlStatement)) {
            stmt.setString(1, item.getFirstName());
            stmt.setString(2, item.getLastName());
            stmt.setDate(3, item.getBirthDate() != null ? Date.valueOf(item.getBirthDate()) : null);
            stmt.setInt(4, item.getGender().ordinal());
            stmt.setString(5, item.getId().toString());

            this._dbConnection.executePreparedStatementCommand(stmt, 1);
        }

        return item;
    }

    @Override
    public void remove(Customer item) throws SQLException
    {
        super.remove(item);
        if (item.getId() != null)
            cleanUpAfterCustomerRemove(item.getId());
        else throw new IllegalArgumentException("Cannot delete a customer without id"); // ToDo: better handling
    }

    private void cleanUpAfterCustomerRemove(UUID customerId) throws SQLException
    {
        String sqlStatement = "UPDATE " + new Reading().getSerializedTableName() + " " +
                "SET customerId=NULL WHERE customerId = ?";

        try (PreparedStatement stmt = this._dbConnection.newPrepareStatement(sqlStatement)) {
            stmt.setString(1, customerId.toString());

            this._dbConnection.executePreparedStatementCommand(stmt);
        }

        try(AuthUserService authUserService = ServiceProvider.getAuthUserService()){
            if (authUserService.checkIfAuthDatabaseExists())
                authUserService.remove(new AuthUser(customerId));
        } catch (IOException e)
        {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void close() throws SQLException
    {
        if (this._provider != null)
        {
            this._provider.releaseDbConnection(this._dbConnection);
            this._provider.releaseCustomerService(this);
        }
    }
}
