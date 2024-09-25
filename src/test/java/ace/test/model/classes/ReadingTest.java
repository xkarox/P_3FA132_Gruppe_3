package ace.test.model.classes;

import ace.model.classes.Reading;
import ace.model.interfaces.ICustomer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.UUID;

public class ReadingTest
{
    private Reading reading;
    private Customer customer;

    @BeforeEach
    public void setUp()
    {
        reading = new Reading();
        customer = new Customer();
    }

    @Test

}
