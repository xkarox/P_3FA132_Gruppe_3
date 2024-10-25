package ace.database.services;

import ace.database.DatabaseConnection;
import ace.model.interfaces.ICustomer.Gender;
import ace.database.DbHelperService;
import ace.database.DbTestHelper;
import ace.model.classes.Customer;
import ace.model.classes.Reading;
import ace.model.interfaces.ICustomer;
import ace.model.interfaces.IReading;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class CustomerServiceTest
{
    private Customer _testCustomer;
    private Reading _testReading;
    private CustomerService _customerService;
    private ReadingService _readingService;

    @BeforeEach
    void SetUp() {
        this._testCustomer = new Customer(UUID.randomUUID(), "John", "Doe", LocalDate.now(),
                ICustomer.Gender.M);
        this._testReading = new Reading(UUID.randomUUID(), "", this._testCustomer.getId(), LocalDate.now(),
                IReading.KindOfMeter.HEIZUNG, 1.69, "90-238-01sdf", false);
        DatabaseConnection _databaseConnection = new DatabaseConnection();
        _databaseConnection.openConnection(DbHelperService.loadProperties(DbTestHelper.loadTestDbProperties()));
        _databaseConnection.removeAllTables();
        _databaseConnection.createAllTables();
        this._customerService = new CustomerService(_databaseConnection);
        this._readingService = new ReadingService(_databaseConnection);
    }

    @Test
    void testAdd()
    {
        this._customerService.add(this._testCustomer);

        Customer customerFromDb = this._customerService.getById(this._testCustomer.getId());

        assertNotNull(customerFromDb, "Customer should not be null after being added to the database.");
        assertEquals(this._testCustomer, customerFromDb, "Customer are not equal");
    }

    @Test
    void updateTest()
    {
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
    void removeTest()
    {
//        add customer and reading
        this._customerService.add(this._testCustomer);
        this._readingService.add(this._testReading);
//        remove customer
        this._customerService.remove(this._testCustomer);
//        try to get customer -> check if null
        assertNull(this._customerService.getById(this._testCustomer.getId()), "Should return null because the " +
                "customer was deleted before");
//         get reading -> check if customer id is null
        Reading reading = this._readingService.getById(this._testReading.getId());
        assertNull(reading.getCustomer(), "Should return null because customer is already deleted");
    }

    @AfterEach
    void tearDown() {
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
        items.add(new Customer(UUID.randomUUID(), "John", "Doe", LocalDate.now(), Gender.M));
        items.add(new Customer(UUID.randomUUID(), "Jane", "Doe", LocalDate.now().plusMonths(1), Gender.W));
        items.add(new Customer(UUID.randomUUID(), "James", "Doe", LocalDate.now().plusYears(2), Gender.M));
        items.add(new Customer(UUID.randomUUID(), "Juno", "Doe", LocalDate.now().minusWeeks(20), Gender.D));

        for (Customer item : items)
        {
            this._customerService.add(item);
        }
        return items;
    }
}
