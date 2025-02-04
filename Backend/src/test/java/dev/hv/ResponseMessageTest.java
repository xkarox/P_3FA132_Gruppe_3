package dev.hv;


import org.junit.jupiter.api.Test;
import java.util.List;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class ResponseMessageTest
{
    @Test
    void toStringWithValues()
    {
        String parameter = "TestClass";

        String expectedMessage = String.format(ResponseMessages.ModelParameterNull.toString(), parameter);
        String orgErrorMessage = ResponseMessages.ModelParameterNull.toString(List.of(parameter));
        assertEquals(expectedMessage, orgErrorMessage, "Messages should be equal");
    }
}
