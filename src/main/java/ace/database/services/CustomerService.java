package ace.database.services;

import ace.database.DatabaseConnection;
import ace.model.classes.Customer;

import java.sql.*;
import java.util.List;
import java.util.UUID;

public class CustomerService extends AbstractBaseService<Customer>
{
    public CustomerService(DatabaseConnection dbConnection)
    {
        super(dbConnection);
    }


    @Override
    public Customer add(Customer item)
    {
        if (item == null)
        {
            throw new RuntimeException("Customer is null and cannot be inserted.");
        }

        String sqlStatement = "INSERT INTO " + item.getSerializedTableName() +
                " (id, firstName, lastName, birthDate, gender) VALUES (?, ?, ?, ?, ?);";

        try (Connection connection = this._dbConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sqlStatement))
        {
            statement.setObject(1, item.getId());
            statement.setString(2, item.getFirstName());
            statement.setString(3, item.getLastName());
            statement.setDate(4, Date.valueOf(item.getBirthDate()));
            statement.setString(5, item.getGender().toString());
            statement.executeUpdate();

        } catch (SQLException e)
        {
            throw new RuntimeException("SQL Error when trying to insert a new item: " + e.getMessage());
        }

        return item;
    }

    @Override
    public Customer getById(UUID id)
    {
        var result = this._dbConnection.getAllObjectsFromDbTableWithFilter(new Customer(), String.format("WHERE id = %s", id));
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
    public List<Customer> getAll()
    {
        return (List<Customer>) this._dbConnection.getAllObjectsFromDbTable(new Customer());
    }

    @Override
    public Customer update(Customer item)
    {
        if (item.getId() == null)
        {
            throw new RuntimeException("Cannot update customer without id");
        }
        StringBuilder sb = new StringBuilder("UPDATE ");
        sb.append(item.getSerializedTableName()).append("SET ");
        sb.append("firstName=").append(item.getFirstName());
        sb.append(",lastName=").append(item.getLastName());
        sb.append(",birthDate=").append(item.getBirthDate());
        sb.append(",gender=").append(item.getGender());
        sb.append(" WHERE id=").append(item.getId()).append(";");
        try
        {
            _dbConnection.executeSqlUpdateCommand(sb.toString(), 1);
            return item;
        } catch (Exception e)
        {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void remove(Customer item)
    {

    }
}
