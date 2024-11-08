package ace;

import ace.model.interfaces.IId;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Map;

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

    public static String getLastPartAfterDot(String str) {
        int lastIndex = str.lastIndexOf('.');
        if (lastIndex == -1) {
            return str; // No dot found, return the original string
        }
        return str.substring(lastIndex + 1);
    }

    public static String unpackFromJsonString(String objectJson, Class classType) throws JsonProcessingException
    {
        String key = Utils.getLastPartAfterDot(classType.toString().toLowerCase());
        ObjectMapper _objMapper = Utils.getObjectMapper();
        Map<String, Object> map = _objMapper.readValue(objectJson, Map.class);
        Object customer = map.get(key);
        return _objMapper.writeValueAsString(customer);
    }

    public static String packIntoJsonString(IId object, Class classType) throws JsonProcessingException
    {
        String key = Utils.getLastPartAfterDot(classType.toString().toLowerCase());
        ObjectMapper _objMapper = Utils.getObjectMapper();
        Map<String, Object> responseCustomer = new HashMap<>();
        responseCustomer.put(key, object);
        return _objMapper.writeValueAsString(responseCustomer);
    }
}
