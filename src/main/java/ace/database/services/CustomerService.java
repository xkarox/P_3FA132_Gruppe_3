package ace.database.services;

import ace.database.DatabaseConnection;
import ace.model.classes.Customer;

import java.sql.*;
import java.util.UUID;

public class CustomerService extends AbstractBaseService<Customer> {
    protected CustomerService(DatabaseConnection dbConnection) {
        super(dbConnection);
    }


    @Override
    public Customer add(Customer item) {
        if (item == null) {
            throw new RuntimeException("Customer is null and cannot be inserted.");
        }

        String sqlStatement = "INSERT INTO " + item.getSerializedTableName() +
                " (id, firstName, lastName, birthDate, gender) VALUES (?, ?, ?, ?, ?);";

        try (Connection connection = this._dbConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sqlStatement)) {

            statement.setObject(1, item.getId());
            statement.setString(2, item.getFirstName());
            statement.setString(3, item.getLastName());
            statement.setDate(4, Date.valueOf(item.getBirthDate()));
            statement.setString(5, item.getGender().toString());
            statement.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException("SQL Error when trying to insert a new item: " + e.getMessage(), e);
        }

        return item;
    }

    @Override
    public Customer getById(UUID id) {
        return null;
    }

    @Override
    public Customer update(Customer item) {
        return null;
    }

    @Override
    public void remove(Customer item) {

    }
}
