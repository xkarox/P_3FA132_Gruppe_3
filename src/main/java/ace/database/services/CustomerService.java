package ace.database.services;

import ace.database.DatabaseConnection;
import ace.model.classes.Customer;

import javax.xml.crypto.Data;
import java.util.UUID;

public class CustomerService extends AbstractBaseService<Customer>
{
    private static final String dbURL = "";
    private static final String dbUser = "";
    private static final String dbPassword = "";

    protected CustomerService(DatabaseConnection dbConnection)
    {
        super(dbConnection);
    }

    @Override
    public Customer add(Customer item)
    {
        if (item == null) {
            throw new RuntimeException("Customer is null and cannot be inserted.");
        }
        if (this._dbConnection != null)
        {
            try
            {
                String sqlStatement = "INSERT INTO Customers (id, firstName, lastName, birthDate, gender) " +
                        "VALUES (?, ?, ?, ?, ?);";
            }
            catch (Exception e)
            {
                throw new RuntimeException("An Error occurred when trying to insert a new item into the Customers table.");
            }
        }
        return item;
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
