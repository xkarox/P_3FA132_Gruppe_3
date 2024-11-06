package ace;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import java.text.SimpleDateFormat;

public class Utils
{
    public static <T> void checkValueEquals(T expectedValue, T result, ErrorMessages errorMessage)
    {
        if (!result.equals(expectedValue))
        {
            StringBuilder strBuilder = new StringBuilder();
            strBuilder.append("Expected: ");
            strBuilder.append(expectedValue);
            strBuilder.append(", but got: ");
            strBuilder.append(result);
            strBuilder.append(" | ");
            strBuilder.append(errorMessage.toString());
            String resultString = strBuilder.toString();
            throw new IllegalArgumentException(resultString);
        }
    }

    public static ObjectMapper getObjectMapper() {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
        objectMapper.setDateFormat(new SimpleDateFormat("yyyy-MM-dd"));
        return objectMapper;
    }
}
