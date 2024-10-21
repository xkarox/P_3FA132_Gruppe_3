package ace.database.services;

import ace.database.DatabaseConnection;
import ace.model.classes.Customer;
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
    public Customer update(Customer item)
    {
        return null;
    }

    @Override
    public void remove(Customer item)
    {

    }
}
