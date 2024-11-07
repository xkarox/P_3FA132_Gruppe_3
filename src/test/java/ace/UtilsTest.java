package ace;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.Test;

import java.text.SimpleDateFormat;
import java.util.Set;

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
    void getObjectMapper()
    {
        Object expectedJavaTimeModuleTypeId = String.valueOf(new JavaTimeModule().getTypeId());
        SimpleDateFormat expectedDateFormat = new SimpleDateFormat("yyyy-MM-dd");
        ObjectMapper objMapper = Utils.getObjectMapper();
        Set<Object> moduleIds =  objMapper.getRegisteredModuleIds();
        System.out.println();
        assertEquals(1, moduleIds.size(), "There should be only one registered module");
        assertTrue(moduleIds.contains(expectedJavaTimeModuleTypeId), "The registered modules should contain the JavaTimeModule");
        assertEquals(expectedDateFormat, objMapper.getDateFormat(), "Date format should be set to yyyy-MM-dd");
    }

    @Test
        // Just for coverage in jacoco report
    void staticTest()
    {
        Utils utils = new Utils();
    }


    @Test
    void getLastPartAfterDot()
    {
        String test = "this.is.a.string";
        String expectedOutput = "string";

        String output = Utils.getLastPartAfterDot(test);

        assertEquals(expectedOutput, output, "Strings should match");
    }

    @Test
    void getLastPartAfterDotNoDotInput()
    {
        String test = "string";
        String expectedOutput = "string";

        String output = Utils.getLastPartAfterDot(test);

        assertEquals(expectedOutput, output, "Strings should match");
    }
}
