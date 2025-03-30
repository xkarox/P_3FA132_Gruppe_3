package dev.server.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.jna.platform.win32.Guid;
import dev.hv.ResponseMessages;
import dev.hv.Utils;
import dev.hv.csv.CsvFormatter;
import dev.hv.csv.CsvParser;
import dev.hv.database.services.CustomerService;
import dev.hv.database.services.ReadingService;
import dev.hv.model.ICustomer;
import dev.hv.model.IReading;
import dev.hv.model.classes.Customer;
import dev.hv.model.classes.CustomerWrapper;
import dev.hv.model.classes.Reading;
import dev.hv.model.classes.ReadingWrapper;
import dev.provider.ServiceProvider;
import dev.server.validator.CustomerJsonSchemaValidatorService;
import dev.server.validator.ReadingJsonSchemaValidationService;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBException;
import jakarta.xml.bind.Unmarshaller;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;

import java.io.IOException;
import java.io.StringReader;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static dev.hv.Utils.createErrorResponse;

@Path("/export")
public class ExportController
{
    private static final Logger logger = LoggerFactory.getLogger(ExportController.class);

    private Response validateRequestData(String jsonString, String rootElement) throws JsonProcessingException
    {
        logger.debug("Validating request data: {}", jsonString);
        if (rootElement.equals("customers"))
        {
            CustomerJsonSchemaValidatorService cs = new CustomerJsonSchemaValidatorService();
            cs.setJsonSchemaPath("schemas/customers.json");
            cs.loadSchema(CustomerJsonSchemaValidatorService.class);
            boolean invalidCustomer = cs.validate(jsonString);

            if (invalidCustomer)
            {
                logger.warn("Invalid customer data: {}", jsonString);
                return createErrorResponse(Response.Status.BAD_REQUEST,
                        ResponseMessages.ControllerBadRequest.toString());
            }
            return null;
        } else if (rootElement.equals("readings"))
        {
            ReadingJsonSchemaValidationService cs = new ReadingJsonSchemaValidationService();
            cs.setJsonSchemaPath("schemas/readings.json");
            cs.loadSchema(ReadingJsonSchemaValidationService.class);
            boolean invalidReading = cs.validate(jsonString);

            if (invalidReading)
            {
                logger.warn("Invalid reading data: {}", jsonString);
                return createErrorResponse(Response.Status.BAD_REQUEST,
                        ResponseMessages.ControllerBadRequest.toString());
            }
            return null;
        }

        return null;
    }

    @Path("/exportFile")
    @POST
    @Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML, MediaType.TEXT_PLAIN})
    @Produces(MediaType.APPLICATION_JSON)
    public Response uploadFile(@HeaderParam("Content-Type") String contentType, String fileContent) throws IOException, JAXBException, ParserConfigurationException, SAXException, ReflectiveOperationException, SQLException
    {
        try
        {
            String jsonResponse = "";
            String mainContentType = contentType.split(";")[0].trim();

            switch (mainContentType)
            {
                case MediaType.APPLICATION_JSON -> jsonResponse = handleJson(fileContent);
                case MediaType.APPLICATION_XML -> jsonResponse = handleXml(fileContent);
                case MediaType.TEXT_PLAIN -> jsonResponse = handleCsv(fileContent);
            }
            return Response.ok(jsonResponse).build();
        } finally
        {

        }

    }

    private String handleJson(String jsonContent) throws IOException
    {
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode rootNode = objectMapper.readTree(jsonContent);
        String rootElementName = rootNode.fieldNames().next();

        Response validationResponse = validateRequestData(jsonContent, rootElementName);
        if (validationResponse != null)
        {
            return "";
        }

        return jsonContent;
    }

    private String handleXml(String xmlContent) throws JAXBException, IOException
    {
        if (xmlContent.contains("<CustomerWrapper>"))
        {
            JAXBContext context = JAXBContext.newInstance(CustomerWrapper.class);
            Unmarshaller unmarshaller = context.createUnmarshaller();

            CustomerWrapper wrapper = (CustomerWrapper) unmarshaller.unmarshal(new StringReader(xmlContent));
            Collection<Customer> customer = wrapper.getCustomers();
            String customersJsonString = Utils.packIntoJsonString(customer, Customer.class);

            Response validationResponse = validateRequestData(customersJsonString, "customers");
            if (validationResponse != null)
            {
                return "";
            }

            return customersJsonString;
        } else if (xmlContent.contains("<ReadingWrapper>"))
        {
            JAXBContext context = JAXBContext.newInstance(ReadingWrapper.class);
            Unmarshaller unmarshaller = context.createUnmarshaller();

            ReadingWrapper wrapper = (ReadingWrapper) unmarshaller.unmarshal(new StringReader(xmlContent));
            Collection<Reading> readings = wrapper.getReadings();
            String readingsJsonString = Utils.packIntoJsonString(readings, Reading.class);

            Response validationResponse = validateRequestData(readingsJsonString, "readings");
            if (validationResponse != null)
            {
                return "";
            }

            return readingsJsonString;
        }


        return xmlContent;
    }

    private String handleCsv(String csvContent) throws IOException, SQLException, ReflectiveOperationException
    {
        CustomerService cs = ServiceProvider.Services.getCustomerService();

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


        CsvParser parser = new CsvParser();
        CsvFormatter formatter = new CsvFormatter();
        parser.setCsvContent(csvContent);
        List<String> csvCustomerHeader = List.copyOf((java.util.Collection<? extends String>) parser.getCustomerHeader());

        List<String> csvReadingHeader = List.copyOf((java.util.Collection<? extends String>) parser.getReadingHeader());

        if (Arrays.equals(csvCustomerHeader.toArray(), customerHeader))
        {
            isCustomer = true;
        }
        else if (Arrays.equals(csvReadingHeader.toArray(), defaultReadingHeaderWater)) {
            isDefaultReading = true;
            water = true;
        }
        else if (Arrays.equals(csvReadingHeader.toArray(), defaultReadingHeaderElectricity)) {
            isDefaultReading = true;
            electricity = true;
        }
        else if (Arrays.equals(csvReadingHeader.toArray(), defaultReadingHeaderHeat)) {
            isDefaultReading = true;
            heat = true;
        }
        else if (Arrays.equals(csvReadingHeader.toArray(), customReadingHeader)) {
            isCustomReading = true;
        }
        if (isDefaultReading) {
            List<Reading> readings = new ArrayList<>();
            Iterable<List<String>> defaultReadingValues = parser.getDefaultReadingValues();
            Iterable<Map<String, String>> metaData = parser.getMetaData();
            String meterId = "";

            Iterator<Map<String, String>> iterator = metaData.iterator();
            Map<String, String> customerMetadata = new HashMap<>();
            Map<String, String> meterIdMetaData = new HashMap<>();

            if (iterator.hasNext()) {
                customerMetadata = iterator.next();

            }

            if (iterator.hasNext()) {
                meterIdMetaData = iterator.next();
                meterId = meterIdMetaData.get("Zählernummer");

            }

            for (List<String> defaultReadingList : defaultReadingValues) {
                Reading reading = new Reading();
                reading.setCustomer(cs.getById(UUID.fromString(customerMetadata.get("Kunde"))));
                reading.setMeterId(meterIdMetaData.get("Zählernummer"));
                reading.setSubstitute(false);

                if (defaultReadingList.size() > 0)
                {
                    DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy");
                    reading.setDateOfReading(LocalDate.parse(defaultReadingList.getFirst(), dateTimeFormatter));
                }
                if (defaultReadingList.size() > 1) {
                    if (water) {
                        reading.setKindOfMeter(IReading.KindOfMeter.WASSER);
                    }
                    else if (heat) {
                        reading.setKindOfMeter(IReading.KindOfMeter.HEIZUNG);
                    }
                    else if (electricity) {
                        reading.setKindOfMeter(IReading.KindOfMeter.STROM);
                    }
                    else {
                        reading.setKindOfMeter(IReading.KindOfMeter.UNBEKANNT);
                    }
                    reading.setMeterCount(Double.parseDouble(defaultReadingList.get(1)));
                }
                if (defaultReadingList.size() > 2)
                {
                    Pattern pattern = Pattern.compile("Nummer\\s+(\\S+)");
                    Matcher matcher = pattern.matcher(defaultReadingList.get(2));

                    if (matcher.find()) {
                        meterId = matcher.group(1);
                    }

                    reading.setComment(defaultReadingList.get(2));
                }
                reading.setMeterId(meterId);
                readings.add(reading);
            }
            return Utils.packIntoJsonString(readings, Reading.class);
        }
        else if (isCustomReading) {
            List<Reading> readings = new ArrayList<>();
            Iterable<List<String>> customReadingValues = parser.getCustomReadingValues();

            for (List<String> customReadingList : customReadingValues) {
                Reading reading = new Reading();

                if (customReadingList.size() > 0) {
                    DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy");
                    reading.setDateOfReading(LocalDate.parse(customReadingList.getFirst(), dateTimeFormatter));
                }
                if (customReadingList.size() > 1) {
                    reading.setMeterCount(Double.parseDouble(customReadingList.get(1)));
                }
                if (customReadingList.size() > 2) {
                    reading.setComment(customReadingList.get(2));
                }
                if (customReadingList.size() > 3) {
                    reading.setCustomer(cs.getById(UUID.fromString(customReadingList.get(3))));
                }
                if (customReadingList.size() > 4) {
                    switch (customReadingList.get(4)) {
                        case "STROM":
                            reading.setKindOfMeter(IReading.KindOfMeter.STROM);
                            break;
                        case "HEIZUNG":
                            reading.setKindOfMeter(IReading.KindOfMeter.HEIZUNG);
                            break;
                        case "WASSER":
                            reading.setKindOfMeter(IReading.KindOfMeter.WASSER);
                            break;
                        default:
                            reading.setKindOfMeter(IReading.KindOfMeter.UNBEKANNT);
                            break;
                    }
                }
                if (customReadingList.size() > 5) {
                    reading.setMeterId(customReadingList.get(5));
                }
                if (customReadingList.size() > 6) {
                    reading.setSubstitute(Boolean.parseBoolean(customReadingList.get(5)));
                }
                readings.add(reading);
            }
            return Utils.packIntoJsonString(readings, Reading.class);
        }

        if (isCustomer)
        {
            List<Customer> customers = new ArrayList<>();
            Iterable<List<String>> customerValues = parser.getCustomerValues();
            for (List<String> customerList : customerValues)
            {
                Customer customer = new Customer();
                if (customerList.size() > 0)
                {
                    customer.setId(UUID.fromString(customerList.getFirst()));
                }
                if (customerList.size() > 1)
                {
                    switch(customerList.get(1)) {
                        case "Herr":
                            customer.setGender(ICustomer.Gender.M);
                            break;
                        case "Frau":
                            customer.setGender(ICustomer.Gender.W);
                            break;
                        case "k.A.":
                            customer.setGender(ICustomer.Gender.U);
                            break;
                    }
                }
                if (customerList.size() > 2)
                {
                    customer.setFirstName(customerList.get(2));
                }
                if (customerList.size() > 3)
                {
                    customer.setLastName(customerList.get(3));
                }
                if (customerList.size() > 4)
                {
                    DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy");
                    customer.setBirthDate(LocalDate.parse(customerList.get(4), dateTimeFormatter));
                } else
                {
                    customer.setBirthDate(null);
                }

                customers.add(customer);
            }
            return Utils.packIntoJsonString(customers, Customer.class);
        }

        return csvContent;
    }
}
