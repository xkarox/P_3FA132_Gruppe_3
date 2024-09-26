package ace.test.model.classes;

import ace.model.classes.Reading;
import ace.model.interfaces.IReading.KindOfMeter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.time.LocalDate;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;

public class ReadingTest
{
    private Reading _reading;
    private Customer _customer;
    private KindOfMeter _kindOfMeter;

    @BeforeEach
    public void setUp()
    {
        _reading = new Reading();
        _customer = new Customer();
        _kindOfMeter = KindOfMeter.STROM;
    }

    @Test
    public void testSetAndGetComment() {
        String comment = "Test comment";
        _reading.setComment(comment);
        assertEquals(comment, _reading.getComment(), "Comment should match the set value");
    }

    @Test
    public void testSetAndGetCustomer() {
        _reading.setCustomer(this._customer);
        assertEquals(this._customer, _reading.getCustomer(), "Customer should match the set mock object");
    }

    @Test
    public void testSetAndGetDateOfReading() {
        LocalDate date = LocalDate.of(2023, 9, 26);
        _reading.setDateOfReading(date);
        assertEquals(date, _reading.getDateOfReading(), "Date of reading should match the set date");
    }

    @Test
    public void testSetAndGetKindOfMeter() {
        _reading.setKindOfMeter(_kindOfMeter);
        assertEquals(_kindOfMeter, _reading.getKindOfMeter(), "Kind of meter should match the set value");
    }

    @Test
    public void testSetAndGetMeterCount() {
        double meterCount = 123.45;
        _reading.setMeterCount(meterCount);
        assertEquals(meterCount, _reading.getMeterCount(), "Meter count should match the set value");
    }

    @Test
    public void testSetAndGetMeterId() {
        String meterId = "Meter123";
        _reading.setMeterId(meterId);
        assertEquals(meterId, _reading.getMeterId(), "Meter ID should match the set value");
    }

    @Test
    public void testSetAndGetSubstitute() {
        _reading.setSubstitute(true);
        assertTrue(_reading.getSubstitute(), "Substitute should match the set value (true)");

        _reading.setSubstitute(false);
        assertFalse(_reading.getSubstitute(), "Substitute should match the set value (false)");
    }

    @Test
    public void testPrintDateOfReading() {
        LocalDate date = LocalDate.of(2023, 9, 26);
        _reading.setDateOfReading(date);
        assertEquals("2023-09-26", _reading.printDateOfReading(), "Printed date should match the set date string");
    }

    @Test
    public void testSetAndGetId() {
        UUID id = UUID.randomUUID();
        _reading.setId(id);
        assertEquals(id, _reading.getId(), "ID should match the set UUID");
    }
}
