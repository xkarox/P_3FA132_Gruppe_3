package dev.server.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.hv.ResponseMessages;
import dev.hv.Serializer;
import dev.hv.Utils;
import dev.hv.model.classes.Customer;
import dev.hv.model.classes.CustomerWrapper;
import dev.hv.model.classes.Reading;
import dev.hv.model.classes.ReadingWrapper;
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
import java.util.*;

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
        List<?> objects = Serializer.deserializeCsv(csvContent);
        if (!objects.isEmpty()) {
            Object firstItem = objects.getFirst();

            if (firstItem instanceof Reading) {
                List<Reading> readings = (List<Reading>) objects;
                return Utils.packIntoJsonString(readings, Reading.class);
            }
            else if (firstItem instanceof Customer) {
                List<Customer> customers = (List<Customer>) objects;
                return Utils.packIntoJsonString(customers, Customer.class);
            }
        }

        return csvContent;
    }
}
