package ace.database.services;

import ace.database.DatabaseConnection;
import ace.model.interfaces.ICustomer.Gender;
import ace.database.DbHelperService;
import ace.database.DbTestHelper;
import ace.model.classes.Customer;
import ace.model.interfaces.ICustomer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

public class CustomerServiceTest
{
    private Customer _testCustomer;
    private CustomerService _customerService;

    @BeforeEach
    void SetUp() {
        this._testCustomer = new Customer(UUID.randomUUID(), "John", "Doe", LocalDate.now(), Gender.M);
        DatabaseConnection _databaseConnection = new DatabaseConnection();
        _databaseConnection.openConnection(DbHelperService.loadProperties(DbTestHelper.loadTestDbProperties()));
        _databaseConnection.removeAllTables();
        _databaseConnection.createAllTables();
        this._customerService = new CustomerService(_databaseConnection);
    }

    @Test
    void updateTest() {
//        add origin customer
        this._customerService.add(this._testCustomer);

//        modify customer
        this._testCustomer.setFirstName("Peter");
        this._testCustomer.setLastName("Griffin");
        this._testCustomer.setBirthDate(LocalDate.of(2000, 11, 2));
        this._testCustomer.setGender(ICustomer.Gender.W);
//        update customer
        this._customerService.update(this._testCustomer);
//        get customer
        Customer updatedCustomer = this._customerService.getById(this._testCustomer.getId());
//        check if customer is updated correctly
        assertEquals(this._testCustomer, updatedCustomer, "Customer should be changed");
    }

    @Test
    void getByIdTest()
    {
        var nullResult = this._customerService.getById(UUID.randomUUID());
        assertNull(nullResult, "Because there are no items in the db");

        List<Customer> customers = createTestData();
        Customer customer = customers.getFirst();
        var result = this._customerService.getById(customer.getId());
        assertEquals(customer, result, "Because the customer should exist");
    }

    @Test
    void getAllTest()
    {
        var nullResult = this._customerService.getAll();
        assertTrue(nullResult.isEmpty(), "Because there are no items in the db");

        List<Customer> customers = createTestData();
        var result = this._customerService.getAll();
        assertEquals(customers, result, "Because all customers should exist");
    }

    private List<Customer> createTestData()
    {
        List<Customer> items = new ArrayList<>();
        items.add( new Customer(UUID.randomUUID(), "John", "Doe", LocalDate.now(), Gender.M));
        items.add( new Customer(UUID.randomUUID(), "Jane", "Doe", LocalDate.now().plusMonths(1), Gender.W));
        items.add( new Customer(UUID.randomUUID(), "James", "Doe", LocalDate.now().plusYears(2), Gender.M));
        items.add( new Customer(UUID.randomUUID(), "Juno", "Doe", LocalDate.now().minusWeeks(20), Gender.D));

        for(Customer item : items)
        {
            this._customerService.add(item);
        }
        return items;
    }
}
