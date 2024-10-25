package ace.database.services;

import ace.database.DatabaseConnection;
import ace.database.DbHelperService;
import ace.database.DbTestHelper;
import ace.model.classes.Customer;
import ace.model.classes.Reading;
import ace.model.interfaces.IReading.KindOfMeter;
import org.junit.jupiter.api.AfterEach;
import ace.model.interfaces.ICustomer.Gender;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.*;

public class ReadingServiceTest
{

    private Reading _testReading;
    private Reading _testReadingWithoutCustomer;
    private Customer _testCustomer;
    private ReadingService _readingService;
    private CustomerService _customerService;

    @BeforeEach
    void SetUp()
    {
        this._testCustomer = new Customer(UUID.randomUUID(), "John", "Doe"
                , LocalDate.now(), Gender.M);
        this._testReading = new Reading(UUID.randomUUID()
                , "Omae wa mou shindeiru!", this._testCustomer.getId()
                , LocalDate.now(), KindOfMeter.STROM
                , 1234.5, "10006660001", false);
        this._testReadingWithoutCustomer = new Reading(UUID.randomUUID()
                , "lalalala", null, LocalDate.now()
                , KindOfMeter.WASSER, 1823.293, "8231891239", true);
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

    @Test
    void updateTest()
    {
        this._customerService.add(this._testCustomer);
//        add origin reading
        this._readingService.add(this._testReading);
//        modify reading
        this._testReading.setComment("NANI?!");
        this._testReading.setDateOfReading(LocalDate.of(2000, 11, 2));
        this._testReading.setKindOfMeter(KindOfMeter.HEIZUNG);
        this._testReading.setMeterCount(98765.5);
        this._testReading.setMeterId("456738901");
        this._testReading.setSubstitute(true);
//        update reading
        this._readingService.update(this._testReading);
//        get reading
        Reading updatedReading = this._readingService.getById(this._testReading.getId());
//        check if reading updated correctly
        assertEquals(this._testReading, updatedReading, "Reading should be changed");
    }

    @Test
    void getByIdTest()
    {
        this._customerService.add(this._testCustomer);
        this._readingService.add(this._testReading);

        var nullResult = this._readingService.getById(UUID.randomUUID());
        assertNull(nullResult, "Because there are no items in the db");

        var result = this._readingService.getById(this._testReading.getId());
        assertEquals(this._testReading, result, "Because the customer should exist");
    }

    @Test
    void getAllTest()
    {
        this._customerService.add(this._testCustomer);
        this._readingService.add(this._testReading);

        Reading reading2 = new Reading(UUID.randomUUID()
                , "no comment", this._testCustomer.getId()
                , LocalDate.now(), KindOfMeter.HEIZUNG
                , 999.9, "10009960001", true);

        this._readingService.add(reading2);

        var firstResult = this._readingService.getAll();
        assertEquals(2, firstResult.size(), "Because there are 2 items");
        assertEquals(this._testReading, firstResult.getFirst());
        assertEquals(reading2, firstResult.getLast());
    }


    @Test
    void removeTest()
    {
//        add Customer and Reading
        this._customerService.add(this._testCustomer);
        this._readingService.add(this._testReading);
//        remove reading
        this._readingService.remove(this._testReading);
//        try to get reading
        assertNull(this._readingService.getById(this._testReading.getId()), "Should return null because the " +
                "reading was deleted before");
    }
}
