package ace.database.services;

import ace.database.DatabaseConnection;
import ace.model.classes.Customer;
import ace.model.classes.Reading;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;
import java.util.UUID;

public class CustomerService extends AbstractBaseService<Customer>
{

    protected CustomerService(DatabaseConnection dbConnection)
    {
        super(dbConnection);
    }

    @Override
    public Customer add(Customer item)
    {
        return null;
    }

    @Override
    public Customer getById(UUID id)
    {
        return null;
    }

    @Override
    public List<Customer> getAll()
    {
        return List.of();
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
        if (customerId == null)
        {
            throw new IllegalArgumentException("Customer Id cannot be null");
        }
        String updateStatement =  new StringBuilder("UPDATE ").append(new Reading().getSerializedTableName())
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
