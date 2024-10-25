package ace.database.services;

import ace.database.DatabaseConnection;
import ace.database.DbHelperService;
import ace.model.classes.Customer;
import ace.model.classes.Reading;
import ace.model.interfaces.ICustomer;
import ace.model.interfaces.IReading;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

public class ReadingServiceTest
{
    private Reading _testReading;
    private Customer _testCustomer;
    private ReadingService _readingService;
    private CustomerService _customerService;

    @BeforeEach
    void SetUp() {
        this._testCustomer = new Customer(UUID.randomUUID(), "John", "Doe"
                , LocalDate.now(), ICustomer.Gender.M);
        this._testReading = new Reading(UUID.randomUUID()
                , "Omae wa mou shindeiru!", this._testCustomer.getId()
                , LocalDate.now(), IReading.KindOfMeter.STROM
                , 1234.5, "10006660001", false);
        DatabaseConnection _databaseConnection = new DatabaseConnection();
        _databaseConnection.openConnection(DbHelperService.loadProperties());
        this._customerService = new CustomerService(_databaseConnection);
        this._readingService = new ReadingService(_databaseConnection);
    }

    @Test
    void updateTest()
    {
        this._customerService.add(this._testCustomer);
//        add origin reading
        this._readingService.add(this._testReading);
//        modify reading
        this._testReading.setComment("NANI?!");
        this._testReading.setDateOfReading(LocalDate.of(2000, 11 ,2));
        this._testReading.setKindOfMeter(IReading.KindOfMeter.HEIZUNG);
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
