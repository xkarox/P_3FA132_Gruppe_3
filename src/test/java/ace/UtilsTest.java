package ace;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class UtilsTest
{
    @Test
    void checkValueEqualsTest()
    {
        int item1 = 1;
        int item2 = 2;
        int item3 = 2;

        String resultString = "Expected: " + item1 + ", but got: "
                + item2 + " | " + ErrorMessages.SqlUpdate;

        Utils.checkValueEquals(item2, item3, ErrorMessages.SqlUpdate);

        boolean exceptionTriggert = false;
        try
        {
            Utils.checkValueEquals(item1, item2, ErrorMessages.SqlUpdate);
        } catch (RuntimeException e)
        {
            exceptionTriggert = true;
            assertEquals(e.getMessage(), resultString);
        }
        assertTrue(exceptionTriggert, "Because the exception should have been triggert");
    }

    @Test
        // Just for coverage in jacoco report
    void staticTest()
    {
        Utils utils = new Utils();
    }
}
