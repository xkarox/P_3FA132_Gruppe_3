package dev.hv;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import dev.hv.model.ICustomer;
import dev.hv.model.IId;
import dev.hv.model.IReading;
import dev.hv.model.classes.Customer;
import dev.hv.model.classes.Reading;
import org.checkerframework.checker.units.qual.C;
import org.junit.jupiter.api.Test;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

public class UtilsTest
{
    @Test
    void checkValueEqualsTest()
    {
        int item1 = 1;
        int item2 = 2;
        int item3 = 2;

        String resultString = "Expected: " + item1 + ", but got: "
                + item2 + " | " + ResponseMessages.SqlUpdate;

        Utils.checkValueEquals(item2, item3, ResponseMessages.SqlUpdate);

        boolean exceptionTriggert = false;
        try
        {
            Utils.checkValueEquals(item1, item2, ResponseMessages.SqlUpdate);
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

    @Test
    void mergeJsonStringTest() throws JsonProcessingException
    {
        Customer customer = new Customer();
        customer.setFirstName("Erik");
        customer.setLastName("Mielke");
        customer.setGender(ICustomer.Gender.M);
        customer.setBirthDate(LocalDate.now());

        Reading reading1 = new Reading();
        reading1.setCustomer(customer);
        reading1.setSubstitute(true);
        reading1.setDateOfReading(LocalDate.now());
        reading1.setKindOfMeter(IReading.KindOfMeter.STROM);
        reading1.setMeterCount(100);

        Reading reading2 = new Reading();
        reading2.setCustomer(customer);
        reading2.setSubstitute(true);
        reading2.setDateOfReading(LocalDate.now());
        reading2.setKindOfMeter(IReading.KindOfMeter.WASSER);
        reading2.setMeterCount(9999);

        List<Reading> readings = Arrays.asList(reading1, reading2);

        ObjectMapper _objMapper = Utils.getObjectMapper();
        String customerJsonString = Utils.packIntoJsonString(customer, Customer.class);
        String readingJsonString = Utils.packIntoJsonString(readings, Reading.class);

        String result = Utils.mergeJsonString(customerJsonString, readingJsonString);

        JsonNode resultNode = _objMapper.readTree(result);
        JsonNode customerNode = resultNode.get("customer");
        assertNotNull(customerNode, "Customer node should not be null");
        assertEquals("Erik", customerNode.get("firstName").asText(), "First name should be 'Erik'");
        assertEquals("Mielke", customerNode.get("lastName").asText(), "Last name should be 'Mielke'");
        assertEquals(LocalDate.now().toString(), customerNode.get("birthDate").asText(), "Birth date should match the current date");
        assertEquals("M", customerNode.get("gender").asText(), "Gender should be 'M'");

        JsonNode readingsNode = resultNode.get("readings");
        assertNotNull(readingsNode, "Readings node should not be null");
        assertTrue(readingsNode.isArray(), "Readings should be an array");
        assertEquals(2, readingsNode.size(), "There should be 2 readings");

        JsonNode reading1Node = readingsNode.get(0);
        assertEquals(LocalDate.now().toString(), reading1Node.get("dateOfReading").asText(), "Date of reading 1 should match the current date");
        assertEquals("STROM", reading1Node.get("kindOfMeter").asText(), "Kind of meter for reading 1 should be 'STROM'");
        assertEquals(100, reading1Node.get("meterCount").asInt(), "Meter count for reading 1 should be 100");
        assertTrue(reading1Node.get("substitute").asBoolean(), "Reading 1 should be a substitute");

        JsonNode reading2Node = readingsNode.get(1);
        assertEquals(LocalDate.now().toString(), reading2Node.get("dateOfReading").asText(), "Date of reading 2 should match the current date");
        assertEquals("WASSER", reading2Node.get("kindOfMeter").asText(), "Kind of meter for reading 2 should be 'WASSER'");
        assertEquals(9999, reading2Node.get("meterCount").asInt(), "Meter count for reading 2 should be 9999");
        assertTrue(reading2Node.get("substitute").asBoolean(), "Reading 2 should be a substitute");
    }

    @Test
    void unpackCollectionFromMergedJsonStringWithReadingsTest() throws JsonProcessingException
    {
        ObjectMapper _objMapper = Utils.getObjectMapper();

        Reading reading1 = new Reading();
        reading1.setMeterCount(100);
        reading1.setKindOfMeter(IReading.KindOfMeter.STROM);
        Reading reading2 = new Reading();
        reading2.setMeterCount(200);
        reading2.setKindOfMeter(IReading.KindOfMeter.WASSER);
        List<Reading> readings = Arrays.asList(reading1, reading2);
        String json = _objMapper.writeValueAsString(Map.of("readings", readings));

        Collection<? extends IId> result = Utils.unpackCollectionFromMergedJsonString(json, Reading.class);
        System.out.println("test");
    }

    @Test
    void unpackCollectionFromMergeJsonStringWithCustomerTest() {

    }

    @Test
    void unpackCollectionFromMergedJsonStringWithArrayOfReadings() {

    }

    @Test
    void unpackCollectionFromMergedJsonStringWithSingleReading() {

    }

    @Test
    void unpackCollectionFromMergedJsonStringWithInvalidJson() {

    }

    @Test
    void unpackCollectionFromMergedJsonStringWithUnsupportedClass() {

    }
}
