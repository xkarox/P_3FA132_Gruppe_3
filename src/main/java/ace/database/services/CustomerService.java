package ace.database.services;

import ace.database.DatabaseConnection;
import ace.model.classes.Customer;
import ace.model.classes.Reading;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.SQLException;
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


        try (PreparedStatement stmt = this._dbConnection.getConnection().prepareStatement(sqlStatement))
        {
            stmt.setObject(1, item.getId());
            stmt.setString(2, item.getFirstName());
            stmt.setString(3, item.getLastName());
            stmt.setDate(4, Date.valueOf(item.getBirthDate()));
            stmt.setString(5, String.valueOf(item.getGender().ordinal()));
            this._dbConnection.executePreparedStatementCommand(stmt);
        } catch (SQLException e)
        {
            throw new RuntimeException(e);
        }

        return item;
    }

    @Override
    public Customer getById(UUID id) throws ReflectiveOperationException, SQLException
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
    public List<Customer> getAll() throws ReflectiveOperationException, SQLException
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
        sb.append(item.getSerializedTableName()).append(" SET");
        sb.append(" firstName='").append(item.getFirstName());
        sb.append("' ,lastName='").append(item.getLastName());
        sb.append("' ,birthDate='").append(item.getBirthDate());
        sb.append("' ,gender=").append(item.getGender().ordinal());
        sb.append(" WHERE id='").append(item.getId()).append("';");
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
        String delStatement = new StringBuilder("DELETE FROM ").append(item.getSerializedTableName())
                .append(" WHERE id=?").toString();
        if (item.getId() == null)
        {
            throw new RuntimeException("Cannot delete a customer without id");
        }
        try
        {
            PreparedStatement preparedStatement = _dbConnection.getConnection().prepareStatement(delStatement);
            preparedStatement.setString(1, item.getId().toString());
            preparedStatement.executeUpdate();

            cleanUpAfterCustomerRemove(item.getId());
        } catch (SQLException e)
        {
            throw new RuntimeException(e);
        }
    }

    private void cleanUpAfterCustomerRemove(UUID customerId)
    {
        String updateStatement = new StringBuilder("UPDATE ").append(new Reading().getSerializedTableName())
                .append(" SET").append(" customerId=NULL ").append("WHERE customerId=?").toString();

        try
        {
            PreparedStatement preparedStatement = _dbConnection.getConnection().prepareStatement(updateStatement);
            preparedStatement.setString(1, customerId.toString());
            preparedStatement.executeUpdate();
        } catch (SQLException e)
        {
            throw new RuntimeException(e);
        }

    }
}
