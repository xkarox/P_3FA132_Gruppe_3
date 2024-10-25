package ace.database.services;

import ace.database.DatabaseConnection;
import ace.model.classes.Customer;
import ace.model.classes.Reading;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public class ReadingService extends AbstractBaseService<Reading> {
    public ReadingService(DatabaseConnection dbConnection) {
        super(dbConnection);
    }


    @Override
    public Reading add(Reading item) {
        if (item == null) {
            throw new RuntimeException("Reading is null and cannot be inserted.");
        }

        String sqlStatement = "INSERT INTO " + item.getSerializedTableName() +
                " (id, comment, customerId, dateOfReading, kindOfMeter, meterCount, meterId, substitute) VALUES (?, ?, ?, ?, ?, ?, ?, ?);";

        try (Connection connection = this._dbConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sqlStatement)) {

            statement.setObject(1, item.getId());
            statement.setString(2, item.getComment());
            if (item.getCustomer() == null) {
                return null;
            }
            else {
                CustomerService customerService = new CustomerService(this._dbConnection);
                Customer existingCustomer = customerService.getById(item.getCustomer().getId());

                // customer does not exists
                if (existingCustomer == null) {
                    customerService.add((Customer)item.getCustomer());
                }

                statement.setObject(3, item.getCustomer().getId());
            }
            statement.setDate(4, Date.valueOf(item.getDateOfReading()));
            statement.setString(5, item.getKindOfMeter().toString());
            statement.setDouble(6, item.getMeterCount());
            statement.setString(7, item.getMeterId());
            statement.setBoolean(8, item.getSubstitute());
            statement.executeUpdate();
        }

        catch (SQLException e) {
            throw new RuntimeException("SQL Error when trying to insert a new item: " + e.getMessage());
        }

        return item;
    }

    @Override
    public Reading getById(UUID id)
    {
        var result = this._dbConnection.getAllObjectsFromDbTableWithFilter(new Reading(), String.format("WHERE id = %s", id));
        if (result.size() > 1)
        {
            throw new RuntimeException(String.format("Expected size of result be equal to 1, but found %d", result.size()));
        }
        if (result.isEmpty())
            return null;
        return (Reading) result.getFirst();
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<Reading> getAll()
    {
       return (List<Reading>) this._dbConnection.getAllObjectsFromDbTable(new Reading());
    }

    @Override
    public Reading update(Reading item)
    {
        if (item.getId() == null)
        {
            throw new RuntimeException("Cannot update reading without id");
        }
        StringBuilder sb = new StringBuilder("UPDATE ");
        sb.append(item.getSerializedTableName()).append("SET ");
        sb.append("customerId=").append(Objects.requireNonNull(item.getCustomer()).getId());
        sb.append(",dateOfReading=").append(item.getDateOfReading());
        sb.append(",kindOfMeter=").append(item.getKindOfMeter());
        sb.append(",meterCount=").append(item.getMeterCount());
        sb.append(",meterId=").append(item.getMeterId());
        sb.append(",substitute=").append(item.getSubstitute());
        sb.append(" WHERE Id=").append(item.getId());
        try
        {
            _dbConnection.executeSqlUpdateCommand(sb.toString(), 1);
            return item;
        }
        catch (Exception e)
        {
            throw new RuntimeException(e);
        }

    }

    @Override
    public void remove(Reading item)
    {

    }
}
