package dev.hv;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import dev.hv.model.IId;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import dev.hv.model.classes.Customer;
import dev.hv.model.classes.Reading;

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

    public static Collection<? extends IId> unpackCollectionFromMergedJsonString(String json) throws JsonProcessingException
    {
        ObjectMapper _objMapper = Utils.getObjectMapper();
        JsonNode rootNode = _objMapper.readTree(json);
        Collection<IId> result = new ArrayList<>();

        JsonNode readingsNode = rootNode.get("readings");
        if (readingsNode != null && readingsNode.isArray())
        {
            result.addAll(_objMapper.readValue(readingsNode.toString(), new TypeReference<Collection<Reading>>()
            {
            }));
        } else if (rootNode.isArray())
        {
            result.addAll(_objMapper.readValue(rootNode.toString(), new TypeReference<Collection<Reading>>()
            {
            }));
        } else if (rootNode.isObject())
        {
            Reading reading = _objMapper.treeToValue(rootNode, Reading.class);
            if (reading != null)
            {
                result.add(reading);
            }
        }

        JsonNode customerNode = rootNode.get("customer");
        if (customerNode != null && customerNode.isObject())
        {
            Customer customer = _objMapper.treeToValue(customerNode, Customer.class);
            if (customer != null)
            {
                result.add(customer);
            }
        } else if (rootNode.isObject())
        {
            Customer customer = _objMapper.treeToValue(rootNode, Customer.class);
            if (customer != null)
            {
                result.add(customer);
            }
        }

        return result;
    }

    public static String packIntoJsonString(IId object, Class classType) throws JsonProcessingException
    {
        String key = Utils.getLastPartAfterDot(classType.toString().toLowerCase());
        ObjectMapper _objMapper = Utils.getObjectMapper();
        Map<String, Object> responseCustomer = new HashMap<>();
        responseCustomer.put(key, object);
        return _objMapper.writeValueAsString(responseCustomer);
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

}
