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
    private final UUID _id = UUID.randomUUID();

    @BeforeEach
    public void setUp()
    {
        this._customer = new Customer("John", "Doe", LocalDate.now(), Gender.M);
        this._reading = new Reading();
        this._reading.setComment(this._comment);
        this._reading.setCustomer(this._customer);
        this._reading.setDateOfReading(this._dateOfReading);
        this._reading.setKindOfMeter(this._kindOfMeter);
        this._reading.setMeterCount(this._meterCount);
        this._reading.setMeterId(this._meterId);
        this._reading.setSubstitute(this._substitute);
        this._reading.setId(this._id);


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
        Customer newCustomer = new Customer("Max", "Mueller", LocalDate.of(2000, 1, 1), Gender.M);
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
        boolean newSubstituteTrue = true;
        _reading.setSubstitute(newSubstituteTrue);
        assertTrue(newSubstituteTrue, "The substitute flag should be set to true.");

        boolean newSubstituteFalse = false;
        _reading.setSubstitute(newSubstituteFalse);
        assertFalse(newSubstituteFalse, "The substitute flag should be set to false.");
    }

    @Test
    void testSetId()
    {
        UUID newId = UUID.randomUUID();
        _reading.setId(newId);
        assertNotNull(_customer.getId(), "ID should not be null after setting a new ID");

        UUID id = this._reading.getId();
        assertEquals(newId, id, "The ID should match the value set by setId().");
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
        ICustomer customer = this._reading.getCustomer();
        assertEquals(this._customer, customer, "The customer should match the value returned by getCustomer().");
    }

    @Test
    void testGetDateOfReading()
    {
        LocalDate dateOfReading  = this._reading.getDateOfReading();
        assertEquals(this._dateOfReading, dateOfReading, "The date of reading should match the value returned by getDateOfReading().");
    }

    @Test
    void testGetKindOfMeter()
    {
        KindOfMeter kindOfMeter = this._reading.getKindOfMeter();
        assertEquals(this._kindOfMeter, kindOfMeter, "The kind of meter should match the value returned by getKindOfMeter().");
    }

    @Test
    void testGetMeterCount()
    {
        double meterCount = this._reading.getMeterCount();
        assertEquals(this._meterCount, meterCount, "The meter count should match the value returned by getMeterCount().");
    }

    @Test
    void testGetMeterId()
    {
        String meterId = this._reading.getMeterId();
        assertEquals(this._meterId, meterId, "The meter ID should match the value returned by getMeterId().");
    }

    @Test
    void testGetSubstitute()
    {
        boolean newSubstituteTrue = true;
        this._reading.setSubstitute(newSubstituteTrue);
        assertTrue(this._reading.getSubstitute(), "The substitute flag should return true.");

        boolean newSubstituteFalse = false;
        this._reading.setSubstitute(newSubstituteFalse);
        assertFalse(this._reading.getSubstitute(), "The substitute flag should return false.");
    }

    @Test
    void testGetId()
    {
        UUID id = this._reading.getId();
        assertEquals(this._id, id, "The ID should match the value returned by getId().");
    }
}
