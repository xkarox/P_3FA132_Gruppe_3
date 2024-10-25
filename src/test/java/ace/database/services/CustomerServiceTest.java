package ace.database.services;

import ace.database.DatabaseConnection;
import ace.database.DbHelperService;
import ace.model.classes.Customer;
import ace.model.interfaces.ICustomer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class CustomerServiceTest
{
    private Customer _testCustomer;
    private CustomerService _customerService;
    private DatabaseConnection _databaseConnection;

    @BeforeEach
    void setUp() {
        this._testCustomer = new Customer(UUID.randomUUID(), "John", "Doe", LocalDate.now(), ICustomer.Gender.M);
        this._databaseConnection = new DatabaseConnection();
        this._databaseConnection.openConnection(DbHelperService.loadProperties());
        this._customerService = new CustomerService(_databaseConnection);
    }

    @Test
    void testAdd() {
        this._customerService.add(this._testCustomer);

        Customer customerFromDb = this._customerService.getById(this._testCustomer.getId());

        assertNotNull(customerFromDb, "Customer should not be null after being added to the database.");
        assertEquals(this._testCustomer, customerFromDb, "Customer are not equal");

    }
    @AfterEach
    void tearDown() {
        this._testCustomer = null;
        this._customerService = null;
        this._customerService.remove(this._testCustomer);
        this._databaseConnection.closeConnection();
    }

}
