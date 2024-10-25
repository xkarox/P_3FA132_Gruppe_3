package ace.database.services;

import ace.database.DatabaseConnection;
import ace.database.DbHelperService;
import ace.model.classes.Customer;
import ace.model.classes.Reading;
import ace.model.interfaces.ICustomer.Gender;
import ace.model.interfaces.IReading.KindOfMeter;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

public class ReadingServiceTest {

    private Reading _testReading;
    private Reading _testReadingWithoutCustomer;
    private ReadingService _readingService;
    private CustomerService _customerService;
    private DatabaseConnection _databaseConnection;

    @BeforeEach
    void setUp() {
        Customer _testCustomer = new Customer(UUID.randomUUID(), "Albert", "Einstein", LocalDate.now(), Gender.M);
        this._testReading = new Reading(UUID.randomUUID(), "comment", _testCustomer, LocalDate.now(), KindOfMeter.WASSER, 133.03, "11", true);
        this._testReadingWithoutCustomer = new Reading(UUID.randomUUID(), "comment", null, LocalDate.now(), KindOfMeter.WASSER, 133.03, "11", true);
        this._databaseConnection = new DatabaseConnection();
        this._databaseConnection.openConnection(DbHelperService.loadProperties());
        this._readingService = new ReadingService(_databaseConnection);
        this._customerService = new CustomerService(_databaseConnection);
    }

    @Test
    void testAdd() {
        this._readingService.add(this._testReading);
        Reading readingFromDb = this._readingService.getById(this._testReading.getId());
        assertNotNull(readingFromDb, "Reading should not be null after being added to the database.");
        assertEquals(this._testReading, readingFromDb, "Readings are not the same");

        Customer createdCustomer = this._customerService.getById(this._testReading.getCustomer().getId());
        assertNotNull(createdCustomer, "A customer should be created");
        assertEquals(createdCustomer, this._testReading.getCustomer());

        this._readingService.add((this._testReadingWithoutCustomer));
        Reading readingWithoutCustomerFromDb = this._readingService.getById(this._testReadingWithoutCustomer.getId());
        assertNull(readingWithoutCustomerFromDb, "Reading should be null because of no customer");
    }

    @AfterEach
    void tearDown() {
        this._readingService.remove(this._testReading);
        this._readingService.remove(this._testReadingWithoutCustomer);
        this._customerService.remove((Customer)this._testReading.getCustomer());
        this._customerService.remove((Customer)this._testReadingWithoutCustomer.getCustomer());
        this._testReading = null;
        this._testReadingWithoutCustomer = null;
        this._databaseConnection.closeConnection();
    }
}
