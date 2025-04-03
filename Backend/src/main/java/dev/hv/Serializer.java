package dev.hv;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.hv.csv.CsvParser;
import dev.hv.model.classes.Customer;
import dev.hv.model.classes.CustomerWrapper;
import dev.hv.model.classes.Reading;
import dev.hv.model.classes.ReadingWrapper;
import dev.hv.model.interfaces.IDbItem;
import jakarta.ws.rs.core.MediaType;
import jakarta.xml.bind.*;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.sql.SQLException;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class Serializer
{
    public static String serializeIntoCsv(List<?> items)
    {
        if (items.isEmpty())
        {
            return null;
        }
        Object firstItem = items.get(0);
        StringBuilder csvContent = new StringBuilder();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy");
        if (firstItem instanceof Reading)
        {
            List<Reading> readings = (List<Reading>) items;

            for (Reading reading : readings)
            {
                csvContent.append(reading.getDateOfReading().format(formatter)).append(";");
                csvContent.append(reading.getMeterCount()).append(";");
                csvContent.append(reading.getComment()).append(";");
                csvContent.append(reading.getCustomer().getId()).append(";");
                csvContent.append(reading.getKindOfMeter()).append(";");
                csvContent.append(reading.getMeterId()).append(";");
                csvContent.append(reading.getSubstitute());
                csvContent.append("\n");
            }
        } else if (firstItem instanceof Customer)
        {
            List<Customer> customers = (List<Customer>) items;

            for (Customer customer : customers)
            {
                csvContent.append(customer.getId() != null ? customer.getId() : "null").append(";");
                csvContent.append(customer.getGender() != null ? customer.getGender() : "null").append(";");
                csvContent.append(customer.getFirstName() != null ? customer.getFirstName() : "null").append(";");
                csvContent.append(customer.getLastName() != null ? customer.getLastName() : "null").append(";");
                csvContent.append(customer.getBirthDate() != null ? customer.getBirthDate().format(formatter) : "null").append(";");
                csvContent.append("\n");
            }
        }
        return csvContent.toString();
    }

    public static String serializeIntoXml(List<? extends IDbItem> objects) throws JAXBException
    {
        Object firstItem = objects.get(0);
        if (firstItem instanceof Customer) {
            JAXBContext objToConvert = JAXBContext.newInstance(CustomerWrapper.class);
            Marshaller marshallerObj = objToConvert.createMarshaller();
            marshallerObj.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);

            CustomerWrapper customerWrapper = new CustomerWrapper((List<Customer>) objects);
            StringWriter xmlWriter = new StringWriter();
            marshallerObj.marshal(customerWrapper, xmlWriter);
            return xmlWriter.toString();
        }
        else if (firstItem instanceof Reading) {
            JAXBContext objToConvert = JAXBContext.newInstance(ReadingWrapper.class);
            Marshaller marshallerObj = objToConvert.createMarshaller();
            marshallerObj.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);

            ReadingWrapper readingWrapper = new ReadingWrapper((List<Reading>) objects);
            StringWriter xmlWriter = new StringWriter();
            marshallerObj.marshal(readingWrapper, xmlWriter);
            return xmlWriter.toString();
        }
        return null;
    }

    public static List<? extends IDbItem> deserializeFile(String fileType, String fileContent, Class objectType) throws IOException, JAXBException, ReflectiveOperationException, SQLException
    {
        String mainContentType = fileType.split(";")[0].trim();

        switch (mainContentType)
        {
            case MediaType.APPLICATION_XML ->
            {
                return deserializeXml(fileContent, objectType);
            }
            case MediaType.TEXT_PLAIN ->
            {
                return deserializeCsv(fileContent, objectType);
            }
            case MediaType.APPLICATION_JSON -> {
                return deserializeJson(fileContent, objectType);
            }
        }
        return List.of();
    }

    private static List<? extends IDbItem> deserializeCsv(String csv, Class classType) throws IOException, ReflectiveOperationException, SQLException
    {
        CsvParser parser = new CsvParser();
        parser.setCsvContent(csv);

        boolean isDefaultReading = false;
        boolean isCustomReading = false;
        boolean isCustomer = false;

        boolean water = false;
        boolean heat = false;
        boolean electricity = false;

        String[] customerHeader = {"UUID", "Anrede", "Vorname", "Nachname", "Geburtsdatum"};

        String[] defaultReadingHeaderWater = {"Datum", "Zählerstand in m³", "Kommentar"};
        String[] defaultReadingHeaderElectricity = {"Datum", "Zählerstand in kWh", "Kommentar"};
        String[] defaultReadingHeaderHeat = {"Datum", "Zählerstand in MWh", "Kommentar"};

        String[] customReadingHeader = {"Datum", "Zählerstand", "Kommentar", "KundenId", "Zählerart", "ZählerstandId", "Ersatz"};

        if (Reading.class.isAssignableFrom(classType))
        {
            List<String> csvReadingHeader = List.copyOf((java.util.Collection<? extends String>) parser.getReadingHeader());
            List<String> csvCustomReadingHeader = List.copyOf((java.util.Collection<? extends String>) parser.getCustomReadingHeader());

            if (Arrays.equals(csvReadingHeader.toArray(), defaultReadingHeaderWater))
            {
                isDefaultReading = true;
                water = true;
            } else if (Arrays.equals(csvReadingHeader.toArray(), defaultReadingHeaderElectricity))
            {
                isDefaultReading = true;
                electricity = true;
            } else if (Arrays.equals(csvReadingHeader.toArray(), defaultReadingHeaderHeat))
            {
                isDefaultReading = true;
                heat = true;
            } else if (Arrays.equals(csvCustomReadingHeader.toArray(), customReadingHeader))
            {
                isCustomReading = true;
            }

            if (isDefaultReading)
            {
                List<Reading> readings = parser.createDefaultReadingsFromCsv(heat, water, electricity);
                return readings;
            } else if (isCustomReading)
            {
                List<Reading> readings = parser.createCustomReadingsFromCsv();
                return readings;
            }
        } else if (Customer.class.isAssignableFrom(classType))
        {
            List<String> csvCustomerHeader = List.copyOf((java.util.Collection<? extends String>) parser.getCustomerHeader());

            if (Arrays.equals(csvCustomerHeader.toArray(), customerHeader))
            {
                isCustomer = true;
            }

            if (isCustomer)
            {
                List<Customer> customers = parser.createCustomerFromCsv();
                return customers;
            }
        }
        return null;
    }

    private static List<? extends IDbItem> deserializeXml(String xmlContent, Class classType) throws JAXBException
    {
        if (Customer.class.isAssignableFrom(classType))
        {
            JAXBContext context = JAXBContext.newInstance(CustomerWrapper.class);
            Unmarshaller unmarshaller = context.createUnmarshaller();
            CustomerWrapper wrapper = (CustomerWrapper) unmarshaller.unmarshal(new StringReader(xmlContent));
            return wrapper.getCustomers();

        } else if (Reading.class.isAssignableFrom(classType))
        {
            JAXBContext context = JAXBContext.newInstance(ReadingWrapper.class);
            Unmarshaller unmarshaller = context.createUnmarshaller();
            ReadingWrapper wrapper = (ReadingWrapper) unmarshaller.unmarshal(new StringReader(xmlContent));
            return wrapper.getReadings();

        }
        return List.of();
    }

    private static List<? extends IDbItem> deserializeJson(String jsonContent, Class classType) throws JsonProcessingException
    {
        return (List<? extends IDbItem>) Utils.unpackCollectionFromJsonString(jsonContent, classType);
    }
}
