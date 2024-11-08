package dev.hv.model.classes;

import dev.hv.model.ICustomer;
import dev.hv.model.ICustomer.Gender;
import dev.hv.model.IReading.KindOfMeter;
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
    private final double _meterCount = 1234.56;
    private final String _meterId = "METER-123";
    private final UUID _id = UUID.randomUUID();

    @BeforeEach
    public void setUp()
    {
        this._customer = new Customer(UUID.randomUUID(), "John", "Doe", LocalDate.now(), Gender.M);
        this._reading = new Reading();
        this._reading.setComment(this._comment);
        this._reading.setCustomer(this._customer);
        this._reading.setDateOfReading(this._dateOfReading);
        this._reading.setKindOfMeter(this._kindOfMeter);
        this._reading.setMeterCount(this._meterCount);
        this._reading.setMeterId(this._meterId);
        this._reading.setSubstitute(true);
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
        Customer newCustomer = new Customer(UUID.randomUUID(), "Max", "Mueller", LocalDate.of(2000, 1, 1), Gender.M);
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
        assertTrue(this._reading.getSubstitute(), "The substitute flag should be set to true.");

        boolean newSubstituteFalse = false;
        _reading.setSubstitute(newSubstituteFalse);
        assertFalse(this._reading.getSubstitute(), "The substitute flag should be set to false.");
    }

    @Test
    void testSetId()
    {
        UUID newId = UUID.randomUUID();
        this._reading.setId(newId);
        assertNotNull(_customer.getId(), "ID should not be null after setting a new ID");

        UUID id = this._reading.getId();
        assertEquals(newId, id, "The ID should match the value set by setId().");

        boolean exceptionThrown = false;
        try
        {
            this._reading.setId(null);
        } catch (IllegalArgumentException e)
        {
            exceptionThrown = true;
            assertEquals(e.getMessage(), "ID cannot be null");
        }
        assertTrue(exceptionThrown, "Because the exception should have been triggert");
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
        LocalDate dateOfReading = this._reading.getDateOfReading();
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

    @Test
    void testGetSerializedStructure()
    {
        String expectedStructure = "";
        expectedStructure += "id UUID PRIMARY KEY NOT NULL,";
        expectedStructure += "comment VARCHAR(120),";
        expectedStructure += "customerId UUID,";
        expectedStructure += "dateOfReading DATE NOT NULL,";
        expectedStructure += "kindOfMeter int NOT NULL,"; // Longest element in enum is 9 chars long
        expectedStructure += "meterCount REAL NOT NULL,";
        expectedStructure += "meterId VARCHAR(60) NOT NULL,"; // Check length
        expectedStructure += "substitute BOOLEAN NOT NULL";

        assertEquals(expectedStructure, this._reading.getSerializedStructure());
    }

    @Test
    void testGetSerializedTableName()
    {
        assertEquals("reading", this._reading.getSerializedTableName());
    }

    @Test
    void printDateOfReadingTest()
    {
        assertEquals(this._reading.getDateOfReading().toString(), this._reading.printDateOfReading());
    }

    @Test
    void equalsTest()
    {
        UUID id = UUID.randomUUID();
        UUID customerId = UUID.randomUUID();
        this._customer.setId(customerId);

        Reading reading1 = new Reading(id, "none", customerId, this._dateOfReading, this._kindOfMeter, this._meterCount, this._meterId, true);
        Reading reading2 = new Reading(id, "none", customerId, this._dateOfReading, this._kindOfMeter, this._meterCount, this._meterId, true);

        Reading reading3 = new Reading(UUID.randomUUID(), "none", customerId, this._dateOfReading, this._kindOfMeter, this._meterCount, this._meterId, true);
        Reading reading4 = new Reading(id, "one", customerId, this._dateOfReading, this._kindOfMeter, this._meterCount, this._meterId, true);
        Reading reading5 = new Reading(id, "none", customerId, LocalDate.now().minusWeeks(2), this._kindOfMeter, this._meterCount, this._meterId, true);
        Reading reading6 = new Reading(id, "none", customerId, this._dateOfReading, KindOfMeter.HEIZUNG, this._meterCount, this._meterId, true);
        Reading reading7 = new Reading(id, "none", customerId, this._dateOfReading, this._kindOfMeter, 6969.69, this._meterId, true);
        Reading reading8 = new Reading(id, "none", customerId, this._dateOfReading, this._kindOfMeter, this._meterCount, "9999", true);
        Reading reading9 = new Reading(id, "none", customerId, this._dateOfReading, this._kindOfMeter, this._meterCount, this._meterId, false);

        assertEquals(reading1, reading2, "Because they should be the same");
        assertEquals(reading1, reading1, "Because they are the same");
        assertNotEquals(reading1, null, "Because it is null");
        assertNotEquals(reading1, this._customer, "Because equals should fail");

        assertNotEquals(reading1, reading3, "Diff id");
        assertNotEquals(reading1, reading4, "Diff comment");
        assertNotEquals(reading1, reading5, "Diff dateOfReading");
        assertNotEquals(reading1, reading6, "Diff kindOfMeter");
        assertNotEquals(reading1, reading7, "Diff meterCount");
        assertNotEquals(reading1, reading8, "Diff meterId");
        assertNotEquals(reading1, reading9, "Diff substitute");
    }
}
