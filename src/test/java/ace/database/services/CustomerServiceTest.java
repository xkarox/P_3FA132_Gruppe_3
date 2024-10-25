package ace.database.services;

import ace.database.DatabaseConnection;
import ace.database.DbHelperService;
import ace.model.classes.Customer;
import ace.model.classes.Reading;
import ace.model.interfaces.ICustomer;
import ace.model.interfaces.IReading;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

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
        _databaseConnection.openConnection(DbHelperService.loadProperties());
        this._customerService = new CustomerService(_databaseConnection);
        this._readingService = new ReadingService(_databaseConnection);
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

    }

}
