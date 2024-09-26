package ace.model.classes;

import ace.model.interfaces.ICustomer;
import ace.model.interfaces.ICustomer.Gender;
import ace.model.interfaces.IReading.KindOfMeter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

public class ReadingTest
{
    private Reading _reading;
    private final String _comment = "Test Comment";
    private Customer _customer;
    private final LocalDate _dateOfReading = LocalDate.now();
    private final KindOfMeter _kindOfMeter = KindOfMeter.STROM;
    private final double _meterCount = 1234.56;;
    private final String _meterId = "METER-123";
    private final boolean _substitute = true;

    @BeforeEach
    public void setUp()
    {
        this._reading = new Reading();
        this._customer = new Customer("John", "Doe", LocalDate.now(), Gender.M);
    }

    @Test
    void testSetComment()
    {
        String newComment = "new Comment";
        _reading.setComment(newComment);
        String comment = _reading.getComment();
        assertEquals(newComment, comment, "The comment should match the value set by setComment().");
    }

    @Test
    void testSetCustomer()
    {
        Customer newCustomer = new Customer("Max", "Mueller", LocalDate.now(), Gender.M);
        _reading.setCustomer(newCustomer);
        ICustomer customer = _reading.getCustomer();
        assertEquals(newCustomer, customer, "The customer should match the value set by setCustomer().");
    }

    @Test
    void testSetDateOfReading()
    {
        LocalDate newDateOfReading = LocalDate.of(2024, 9, 25);
        _reading.setDateOfReading(newDateOfReading);
        LocalDate dateOfReading = _reading.getDateOfReading();
        assertEquals(newDateOfReading, dateOfReading, "The date of reading should match the value set by setDateOfReading().");
    }

    @Test
    void testSetKindOfMeter()
    {
        KindOfMeter newKindOfMeter = KindOfMeter.HEIZUNG;
        _reading.setKindOfMeter(newKindOfMeter);
        KindOfMeter kindOfMeter = _reading.getKindOfMeter();
        assertEquals(newKindOfMeter, kindOfMeter, "The kind of meter should match the value set by setKindOfMeter().");
    }

    @Test
    void testSetMeterCount()
    {
        double newMeterCount = 123.12;
        _reading.setMeterCount(newMeterCount);
        double meterCount = _reading.getMeterCount();
        assertEquals(newMeterCount, meterCount, "The meter count should match the value set by setMeterCount().");
    }

    @Test
    void testSetMeterId()
    {
        String newMeterId = "MeterId-Test-123";
        _reading.setMeterId(newMeterId);
        String meterId = _reading.getMeterId();
        assertEquals(newMeterId, meterId, "The meter ID should match the value set by setMeterId().");
    }

    @Test
    void testSetSubstitute()
    {
        boolean newSubstitute = true;
        _reading.setSubstitute(newSubstitute);
        assertTrue(newSubstitute, "The substitute flag should be set to true.");
    }

    @Test
    void testSetId()
    {
        int expectedId = this._id;
        _reading.setId(expectedId);
        int actualId = _reading.getId();
        assertEquals(expectedId, actualId, "The ID should match the value set by setId().");
    }

    @Test
    void testGetComment()
    {
        String comment = this._reading.getComment();
        assertEquals(this._comment, comment, "The comment should match the value returned by getComment().");
    }

    @Test
    void testGetCustomer()
    {
        Customer expectedCustomer = this._customer;
        _reading.setCustomer(expectedCustomer);
        Customer actualCustomer = _reading.getCustomer();
        assertEquals(expectedCustomer, actualCustomer, "The customer should match the value returned by getCustomer().");
    }

    @Test
    void testGetDateOfReading()
    {
        Date expectedDateOfReading = this._dateOfReading;
        _reading.setDateOfReading(expectedDateOfReading);
        Date actualDateOfReading = _reading.getDateOfReading();
        assertEquals(expectedDateOfReading, actualDateOfReading, "The date of reading should match the value returned by getDateOfReading().");
    }

    @Test
    void testGetKindOfMeter()
    {
        String expectedKindOfMeter = this._kindOfMeter;
        _reading.setKindOfMeter(expectedKindOfMeter);
        String actualKindOfMeter = _reading.getKindOfMeter();
        assertEquals(expectedKindOfMeter, actualKindOfMeter, "The kind of meter should match the value returned by getKindOfMeter().");
    }

    @Test
    void testGetMeterCount()
    {
        int expectedMeterCount = this._meterCount;
        _reading.setMeterCount(expectedMeterCount);
        int actualMeterCount = _reading.getMeterCount();
        assertEquals(expectedMeterCount, actualMeterCount, "The meter count should match the value returned by getMeterCount().");
    }

    @Test
    void testGetMeterId()
    {
        String expectedMeterId = this._meterId;
        _reading.setMeterId(expectedMeterId);
        String actualMeterId = _reading.getMeterId();
        assertEquals(expectedMeterId, actualMeterId, "The meter ID should match the value returned by getMeterId().");
    }

    @Test
    void testGetSubstitute()
    {
        boolean expectedSubstitute = this._substitute;
        _reading.setSubstitute(expectedSubstitute);
        boolean actualSubstitute = _reading.getSubstitute();
        assertTrue(actualSubstitute, "The substitute flag should be true when returned by getSubstitute().");
    }

    @Test
    void testGetId()
    {
        int expectedId = this._id;
        _reading.setId(expectedId);
        int actualId = _reading.getId();
        assertEquals(expectedId, actualId, "The ID should match the value returned by getId().");
    }
}
