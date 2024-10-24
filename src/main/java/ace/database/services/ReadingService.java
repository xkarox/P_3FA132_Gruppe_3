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
            throw new RuntimeException("SQL Error when trying to insert a new item: " + e.getMessage(), e);
        }

        return item;
    }

    @Override
    public Reading getById(UUID id) {
        return null;
    }

    @Override
    public List<Reading> getAll() {
        return List.of();
    }

    @Override
    public Reading update(Reading item) {
        return null;
    }

    @Override
    public void remove(Reading item) {

    }
}
