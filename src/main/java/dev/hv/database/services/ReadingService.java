package dev.hv.database.services;

import dev.hv.database.DatabaseConnection;
import dev.hv.database.provider.InternalServiceProvider;
import dev.hv.model.classes.Customer;
import dev.hv.model.classes.Reading;
import dev.provider.ServiceProvider;

import java.io.IOException;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public class ReadingService extends AbstractBaseService<Reading>
{
    public ReadingService(DatabaseConnection dbConnection, InternalServiceProvider provider)
    {
        super(dbConnection, provider);
    }

    public ReadingService(DatabaseConnection dbConnection)
    {
        super(dbConnection, null);
    }

    @Override
    public Reading add(Reading item) throws SQLException, ReflectiveOperationException, IOException
    {
        if (item == null)
            throw new IllegalArgumentException("Reading is null and cannot be inserted.");
        if (item.getCustomer() == null)
            throw new IllegalArgumentException("Creating a reading without a Customer is not possible.");

        String sqlStatement = "INSERT INTO " + item.getSerializedTableName() +
                " (id, comment, customerId, dateOfReading, kindOfMeter, meterCount,  meterId, substitute) VALUES (?, ?, ?, ?, ?, ?, ?, ?);";

        try (PreparedStatement stmt = this._dbConnection.newPrepareStatement(sqlStatement))
        {
            stmt.setString(1, item.getId().toString());
            stmt.setString(2, item.getComment());

            String customerId = null;
            try (CustomerService customerService = ServiceProvider.Services.getCustomerService())
            {
                Customer existingCustomer = customerService.getById(item.getCustomer().getId());
                if (existingCustomer == null)
                {
                    customerService.add((Customer) item.getCustomer());
                }
                customerId = item.getCustomer().getId().toString();
            }

            stmt.setString(3, customerId);
            stmt.setDate(4, Date.valueOf(item.getDateOfReading()));
            stmt.setString(5, String.valueOf(item.getKindOfMeter().ordinal()));
            stmt.setDouble(6, item.getMeterCount());
            stmt.setString(7, item.getMeterId());
            stmt.setBoolean(8, item.getSubstitute());
            this._dbConnection.executePreparedStatementCommand(stmt, 1);
        }
        return item;
    }

    @Override
    public Reading getById(UUID id) throws ReflectiveOperationException, SQLException
    {
        var result = this._dbConnection.getAllObjectsFromDbTableWithFilter(new Reading(), String.format("WHERE id = '%s'", id));
        if (result.size() > 1)
            throw new RuntimeException(String.format("Expected size of result be equal to 1, but found %d", result.size()));
        if (result.isEmpty())
            return null;
        return (Reading) result.getFirst();
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<Reading> getAll() throws ReflectiveOperationException, SQLException
    {
        return (List<Reading>) this._dbConnection.getAllObjectsFromDbTable(new Reading());
    }

    @Override
    public Reading update(Reading item) throws SQLException, IllegalArgumentException
    {
        if (item.getId() == null)
            throw new IllegalArgumentException("Cannot update reading without id");


        String sqlStatement = "UPDATE " + item.getSerializedTableName() + " " +
                "SET customerId = ?, comment = ?, dateOfReading = ?, kindOfMeter = ?, meterCount = ?, " +
                "meterId = ?, substitute = ? WHERE id = ?";

        try (PreparedStatement stmt = this._dbConnection.newPrepareStatement(sqlStatement)) {
            stmt.setString(1, item.getCustomer().getId().toString());
            stmt.setString(2, item.getComment());
            stmt.setDate(3, Date.valueOf(item.getDateOfReading()));
            stmt.setString(4, String.valueOf(item.getKindOfMeter().ordinal()));
            stmt.setDouble(5, item.getMeterCount());
            stmt.setString(6, item.getMeterId());
            stmt.setBoolean(7, item.getSubstitute());
            stmt.setString(8, item.getId().toString());
            this._dbConnection.executePreparedStatementCommand(stmt, 1);
        }
        return item;
    }

    @Override
    public void remove(Reading item) throws SQLException
    {
        removeDbItem(item);
    }

    @Override
    public void close() throws SQLException
    {
        if (this._provider != null)
        {
            this._provider.releaseDbConnection(this._dbConnection);
            this._provider.releaseReadingService(this);
        }
    }
}
