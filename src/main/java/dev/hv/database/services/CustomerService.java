package dev.hv.database.services;

import dev.hv.database.DatabaseConnection;
import dev.hv.database.provider.InternalServiceProvider;
import dev.hv.model.classes.Customer;
import dev.hv.model.classes.Reading;

import java.io.IOException;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public class CustomerService extends AbstractBaseService<Customer>
{
    public CustomerService(DatabaseConnection dbConnection, InternalServiceProvider provider)
    {
        super(dbConnection, provider);
    }

    public CustomerService(DatabaseConnection dbConnection)
    {
        super(dbConnection, null);
    }


    @Override
    // Req. Nr.: 3
    public Customer add(Customer item) throws IllegalArgumentException, SQLException
    {
        if (item == null)
            throw new IllegalArgumentException("Customer is null and cannot be inserted.");

        String sqlStatement = "INSERT INTO " + item.getSerializedTableName() +
                " (id, firstName, lastName, birthDate, gender) VALUES (?, ?, ?, ?, ?);";

        try (PreparedStatement stmt = this._dbConnection.newPrepareStatement(sqlStatement))
        {
            stmt.setString(1, item.getId().toString());
            stmt.setString(2, item.getFirstName());
            stmt.setString(3, item.getLastName());
            stmt.setDate(4, item.getBirthDate() != null ? Date.valueOf(item.getBirthDate()) : null);
            stmt.setString(5, String.valueOf(item.getGender().ordinal()));
            this._dbConnection.executePreparedStatementCommand(stmt, 1);
        }

        return item;
    }

    @Override
    // Req. Nr.: 4
    public Customer getById(UUID id) throws ReflectiveOperationException, SQLException, RuntimeException
    {
        var result = this._dbConnection.getAllObjectsFromDbTableWithFilter(new Customer(), String.format("WHERE id = '%s'", id));
        if (result.size() > 1)
        {
            throw new RuntimeException(String.format("Expected size of result be equal to 1, but found %d", result.size()));
        }
        if (result.isEmpty())
            return null;
        return (Customer) result.getFirst();
    }

    @SuppressWarnings("unchecked")
    @Override
    // Req. Nr.: 7
    public List<Customer> getAll() throws ReflectiveOperationException, SQLException
    {
        return (List<Customer>) this._dbConnection.getAllObjectsFromDbTable(new Customer());
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
    // Req. Nr.: 6
    public void remove(Customer item) throws IllegalArgumentException, SQLException
    {
        removeDbItem(item);
        cleanUpAfterCustomerRemove(item.getId());
    }

    // Req. Nr.: 14
    private void cleanUpAfterCustomerRemove(UUID customerId) throws SQLException
    {
        String sqlStatement = "UPDATE " + new Reading().getSerializedTableName() + " " +
                "SET customerId=NULL WHERE customerId = ?";

        try (PreparedStatement stmt = this._dbConnection.newPrepareStatement(sqlStatement)) {
            stmt.setString(1, customerId.toString());

            this._dbConnection.executePreparedStatementCommand(stmt);
        }
    }

    @Override
    public void close() throws SQLException
    {
        if (this._provider != null){
            this._provider.releaseDbConnection(this._dbConnection);
            this._provider.releaseCustomerService(this);
        }
    }
}
