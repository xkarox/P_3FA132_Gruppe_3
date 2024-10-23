import ace.database.DatabaseConnection;
import ace.database.services.CustomerService;
import ace.model.classes.Customer;
import ace.model.decorator.FieldInfo;
import ace.model.interfaces.ICustomer;

import java.sql.*;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public class Main {

    public static void main(String[] args) {

        // testing
        Customer customer = new Customer(UUID.randomUUID(), "Name", "Name2", LocalDate.now(), ICustomer.Gender.M);
        add(customer);

    }

    public static Customer add(Customer item) {
        if (item == null) {
            throw new RuntimeException("Customer is null and cannot be inserted.");
        }
        StringBuilder sqlStatement = new StringBuilder();
        String tableName = item.getSerializedTableName();
        sqlStatement.append("INSERT INTO ").append(tableName).append(" (id, firstName, lastName, birthDate, gender) ");
        sqlStatement.append("VALUES (");
        sqlStatement.append("'").append(item.getId()).append("', ");
        sqlStatement.append("'").append(item.getFirstName()).append("', ");
        sqlStatement.append("'").append(item.getLastName()).append("', ");
        sqlStatement.append("'").append(Date.valueOf(item.getBirthDate())).append("', ");
        sqlStatement.append("'").append(item.getGender()).append("'");
        sqlStatement.append(");");
        try (ResultSet test = _dbConnection.executeSqlQueryCommand(sqlStatement.toString())) {
            System.out.println(test);
        } catch (SQLException e) {
            throw new RuntimeException("SQL Error when trying to insert a new item: " + e.getMessage(), e);
        }

        System.out.println(sqlStatement);


        return item;
    }

}
