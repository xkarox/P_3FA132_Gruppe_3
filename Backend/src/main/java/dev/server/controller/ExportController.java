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

            List<Reading> readings = parser.createDefaultReadingsFromCsv(heat, water, electricity);

            return Utils.packIntoJsonString(readings, Reading.class);
        }
        else if (isCustomReading) {
            List<Reading> readings = parser.createCustomReadingsFromCsv();
            return Utils.packIntoJsonString(readings, Reading.class);
        }

        if (isCustomer)
        {
            List<Customer> customers = parser.createCustomerFromCsv();
            return Utils.packIntoJsonString(customers, Customer.class);
        }

        return csvContent;
    }
}
