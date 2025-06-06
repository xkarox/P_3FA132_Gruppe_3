package dev.hv;

import com.fasterxml.jackson.core.type.TypeReference;
import dev.hv.model.interfaces.IId;
import com.fasterxml.jackson.databind.JsonNode;
import dev.hv.model.interfaces.IId;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import dev.hv.model.classes.Customer;
import dev.hv.model.classes.CustomerWrapper;
import dev.hv.model.classes.Reading;
import dev.hv.model.classes.ReadingWrapper;
import dev.server.validator.CustomerJsonSchemaValidatorService;
import dev.server.validator.ReadingJsonSchemaValidationService;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBException;
import jakarta.xml.bind.Unmarshaller;

import java.io.IOException;
import java.io.StringReader;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.*;

public class Utils
{
    public static <T> void checkValueEquals(T expectedValue, T result, ResponseMessages errorMessage)
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

    public static ObjectMapper getObjectMapper()
    {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
        objectMapper.setDateFormat(new SimpleDateFormat("yyyy-MM-dd"));
        return objectMapper;
    }

    public static String getLastPartAfterDot(String str)
    {
        int lastIndex = str.lastIndexOf('.');
        if (lastIndex == -1)
        {
            return str; // No dot found, return the original string
        }
        return str.substring(lastIndex + 1);
    }


    //    Removes the customer or reading declaration
    public static String unpackFromJsonString(String objectJson, Class classType) throws JsonProcessingException
    {
        String key = Utils.getLastPartAfterDot(classType.toString().toLowerCase());
        ObjectMapper _objMapper = Utils.getObjectMapper();
        Map<String, Object> map = _objMapper.readValue(objectJson, Map.class);
        Object customer = map.get(key);
        return _objMapper.writeValueAsString(customer);
    }

    public static Collection<? extends IId> unpackCollectionFromJsonString(String objectJson, Class classType) throws JsonProcessingException
    {
        String key = Utils.getLastPartAfterDot(classType.toString().toLowerCase()) + "s";
        ObjectMapper _objMapper = Utils.getObjectMapper();
        if (classType == Reading.class)
        {
            Map<String, Collection<Reading>> collection = _objMapper.readValue(objectJson, new TypeReference<Map<String, Collection<Reading>>>()
            {
            });
            return collection.get("readings");
        } else
        {
            Map<String, Collection<Customer>> collection = _objMapper.readValue(objectJson, new TypeReference<Map<String, Collection<Customer>>>()
            {
            });
            return collection.get("customers");
        }
    }

    public static String packIntoJsonString(IId object, Class classType) throws JsonProcessingException
    {
        String key = Utils.getLastPartAfterDot(classType.toString().toLowerCase());
        ObjectMapper _objMapper = Utils.getObjectMapper();
        Map<String, Object> responseCustomer = new HashMap<>();
        responseCustomer.put(key, object);
        return _objMapper.writeValueAsString(responseCustomer);
    }

    public static String packIntoJsonString(Map<? extends Object, ? extends Object> object) throws JsonProcessingException
    {
        return Utils.getObjectMapper().writeValueAsString(object);
    }

    public static String packIntoJsonString(Collection<? extends IId> objects, Class classType) throws JsonProcessingException
    {
        String key = Utils.getLastPartAfterDot(classType.toString().toLowerCase()) + "s";
        ObjectMapper _objMapper = Utils.getObjectMapper();

        Map<String, Collection<? extends IId>> responseArray = new HashMap<>();
        responseArray.put(key, objects);

        return _objMapper.writeValueAsString(responseArray);
    }

    public static String mergeJsonString(Map<String, Object> objects) throws JsonProcessingException
    {
        ObjectMapper objectMapper = Utils.getObjectMapper();
        return objectMapper.writeValueAsString(objects);

    }

    public static Response createErrorResponse(Response.Status status, String message) throws JsonProcessingException
    {
        Map<String, String> errorResponse = new HashMap<>();
        errorResponse.put("message", message);
        try
        {
            return Response.status(status)
                    .entity(Utils.getObjectMapper().writeValueAsString(errorResponse))
                    .type(MediaType.APPLICATION_JSON)
                    .build();
        } catch (JsonProcessingException e)
        {
            Map<String, String> response = new HashMap<>();
            response.put("message", ResponseMessages.ControllerInternalError.toString());
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(Utils.getObjectMapper().writeValueAsString(response))
                    .type(MediaType.APPLICATION_JSON)
                    .build();
        }
    }

    public static String formatContentType(String contentType) {
        if (contentType.contains("text/plain")) {
            return "csv";
        }
        else if (contentType.contains("application/xml")) {
            return "xml";
        }
        else if (contentType.contains("application/json")) {
            return "json";
        }
        return "";
    }
}
