package ace.database.services;

import ace.database.DatabaseConnection;
import ace.model.classes.Customer;

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
        var result = this._dbConnection.getAllObjectsFromDbTableWithFilter(new Customer(), String.format("WHERE ID = %s", id));
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
