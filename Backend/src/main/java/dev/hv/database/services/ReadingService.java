package dev.hv.database.services;

import dev.hv.database.DatabaseConnection;
import dev.hv.database.provider.InternalServiceProvider;
import dev.hv.model.interfaces.IReading;
import dev.hv.model.classes.Customer;
import dev.hv.model.classes.Reading;
import dev.provider.ServiceProvider;

import java.io.IOException;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.*;

public class ReadingService extends AbstractBaseService<Reading>
{
    public ReadingService(DatabaseConnection dbConnection, InternalServiceProvider provider)
    {
        super(dbConnection, provider, Reading.class);
    }

    public ReadingService(DatabaseConnection dbConnection)
    {
        super(dbConnection, null, Reading.class);
    }

    @Override
    // Req. Nr.: 8
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

            String customerId;
            try (CustomerService customerService = ServiceProvider.Services.getCustomerService())
            {
                Customer existingCustomer = customerService.getById(item.getCustomer().getId());
                if (existingCustomer == null)
                {
                    // Req. Nr.: 16
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


    public List<Reading> getReadingsByCustomerId(UUID id) throws ReflectiveOperationException, SQLException, IOException
    {
        var result = this._dbConnection.getAllObjectsFromDbTableWithFilter(Reading.class, String.format("WHERE customerId = '%s'", id));
        return result.isEmpty() ? List.of() : result;
    }

    public Collection<Reading> queryReadings(Optional<UUID> customerId,
                                 Optional<LocalDate> startDate,
                                 Optional<LocalDate> endDate,
                                 Optional<IReading.KindOfMeter> kindOfMeter) throws SQLException, ReflectiveOperationException, IOException
    {
        StringBuilder whereClauseBuilder = new StringBuilder("WHERE");
        customerId.ifPresentOrElse(
                id -> whereClauseBuilder.append(" customerId = '")
                        .append(id)
                        .append("'"),
                () -> whereClauseBuilder.append(" customerId IS NULL"));

        startDate.ifPresentOrElse(
                date -> whereClauseBuilder.append(" AND dateOfReading BETWEEN '")
                        .append(date)
                        .append("'"),
                () -> whereClauseBuilder.append(" AND dateOfReading BETWEEN '")
                        .append(LocalDate.of(0, 1, 1))
                        .append("'"));

        endDate.ifPresentOrElse(
                date -> whereClauseBuilder.append(" AND '")
                        .append(date)
                        .append("'"),
                () -> whereClauseBuilder.append(" AND '")
                        .append(LocalDate.now().plusDays(1))
                        .append("'"));
        kindOfMeter.ifPresent(
                ofMeter -> whereClauseBuilder.append(" AND kindOfMeter = ")
                        .append(ofMeter.ordinal()));

        return this._dbConnection.getAllObjectsFromDbTableWithFilter(Reading.class, whereClauseBuilder.toString());
    }

    @Override
    // Req. Nr.: 12
    public List<Reading> getAll() throws ReflectiveOperationException, SQLException, IOException
    {
        return this._dbConnection.getAllObjectsFromDbTable(Reading.class);
    }

    @Override
    // Req. Nr.: 10
    public Reading update(Reading item) throws SQLException, IllegalArgumentException
    {
        if (item.getId() == null)
            throw new IllegalArgumentException("Cannot update reading without id");


        String sqlStatement = "UPDATE " + item.getSerializedTableName() + " " +
                "SET customerId = ?, comment = ?, dateOfReading = ?, kindOfMeter = ?, meterCount = ?, " +
                "meterId = ?, substitute = ? WHERE id = ?";

        try (PreparedStatement stmt = this._dbConnection.newPrepareStatement(sqlStatement))
        {
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
    public void close() throws SQLException
    {
        if (this._provider != null)
        {
            this._provider.releaseDbConnection(this._dbConnection);
            this._provider.releaseReadingService(this);
        }
    }
}
