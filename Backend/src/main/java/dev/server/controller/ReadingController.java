package dev.server.controller;

import dev.hv.ResponseMessages;
import dev.hv.Serializer;
import dev.hv.Utils;
import dev.hv.csv.CsvParser;
import dev.hv.model.IReading;
import dev.hv.model.classes.Customer;
import dev.hv.model.classes.ReadingWrapper;
import dev.provider.ServiceProvider;
import dev.hv.database.services.ReadingService;
import dev.hv.model.classes.Reading;
import com.fasterxml.jackson.core.JsonProcessingException;
import dev.server.validator.ReadingJsonSchemaValidationService;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBException;
import jakarta.xml.bind.Marshaller;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.print.attribute.standard.Media;
import java.io.IOException;
import java.io.StringWriter;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.*;

import static dev.hv.Utils.createErrorResponse;

@Path("/readings")
public class ReadingController
{

    private static final Logger logger = LoggerFactory.getLogger(ReadingController.class);

    private Response validateRequestData(String jsonString) throws JsonProcessingException
    {
        logger.debug("Validating request data: {}", jsonString);
        boolean invalidReading = ReadingJsonSchemaValidationService.getInstance().validate(jsonString);
        if (invalidReading)
        {
            logger.warn("Invalid reading data: {}", jsonString);
            return createErrorResponse(Response.Status.BAD_REQUEST,
                    ResponseMessages.ControllerBadRequest.toString());
        }
        return null;
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response addReading(String readingJson) throws JsonProcessingException
    {
        logger.info("Received request to add reading: {}", readingJson);
        Response invalid = this.validateRequestData(readingJson);
        if (invalid != null)
        {
            return invalid;
        }
        try (ReadingService rs = ServiceProvider.Services.getReadingService())
        {
            readingJson = Utils.unpackFromJsonString(readingJson, Reading.class);
            Reading reading = Utils.getObjectMapper().readValue(readingJson, Reading.class);
            if (reading.getId() == null)
            {
                reading.setId(UUID.randomUUID());
            }
            reading = rs.add(reading);
            logger.info("Reading added successfully: {}", reading);
            return Response.status(Response.Status.CREATED)
                    .entity(Utils.packIntoJsonString(reading, Reading.class))
                    .type(MediaType.APPLICATION_JSON)
                    .build();
        } catch (JsonProcessingException | SQLException | ReflectiveOperationException e)
        {
            logger.error("Error adding reading: {}", e.getMessage(), e);
            return createErrorResponse(Response.Status.BAD_REQUEST,
                    ResponseMessages.ControllerBadRequest.toString());
        } catch (IOException e)
        {
            logger.error("Internal server error: {}", e.getMessage(), e);
            return createErrorResponse(Response.Status.INTERNAL_SERVER_ERROR,
                    ResponseMessages.ControllerInternalError.toString());
        }
    }

    @PUT
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response updateReading(String readingJson) throws JsonProcessingException
    {
        logger.info("Received request to update reading: {}", readingJson);
        Response invalid = this.validateRequestData(readingJson);
        if (invalid != null)
        {
            return invalid;
        }
        try (ReadingService rs = ServiceProvider.Services.getReadingService())
        {
            readingJson = Utils.unpackFromJsonString(readingJson, Reading.class);
            Reading reading = Utils.getObjectMapper().readValue(readingJson, Reading.class);
            if (rs.getById(reading.getId()) == null)
            {
                logger.warn("Reading not found: {}", reading.getId());
                return createErrorResponse(Response.Status.NOT_FOUND,
                        ResponseMessages.ControllerNotFound.toString());
            }
            rs.update(reading);
            logger.info("Reading updated successfully: {}", reading);
            return Response.status(Response.Status.OK)
                    .entity(Utils.packIntoJsonString(reading, Reading.class))
                    .type(MediaType.APPLICATION_JSON)
                    .build();
        } catch (JsonProcessingException | ReflectiveOperationException | SQLException e)
        {
            logger.error("Error updating reading: {}", e.getMessage(), e);
            return createErrorResponse(Response.Status.BAD_REQUEST,
                    ResponseMessages.ControllerBadRequest.toString());
        } catch (IOException e)
        {
            logger.error("Internal server error: {}", e.getMessage(), e);
            return createErrorResponse(Response.Status.INTERNAL_SERVER_ERROR,
                    ResponseMessages.ControllerInternalError.toString());
        }
    }

    @GET
    @Path("/{id}")
    public Response getReading(@PathParam("id") UUID id) throws JsonProcessingException
    {
        logger.info("Received request to get reading with ID: {}", id);
        try (ReadingService rs = ServiceProvider.Services.getReadingService())
        {
            Reading reading = rs.getById(id);
            logger.info("Reading retrieved successfully: {}", reading);
            return Response.status(Response.Status.OK)
                    .entity(Utils.packIntoJsonString(reading, Reading.class))
                    .type(MediaType.APPLICATION_JSON)
                    .build();
        } catch (SQLException | ReflectiveOperationException e)
        {
            logger.error("Error retrieving reading: {}", e.getMessage(), e);
            return createErrorResponse(Response.Status.BAD_REQUEST,
                    ResponseMessages.ControllerBadRequest.toString());
        } catch (IOException e)
        {
            logger.error("Internal server error: {}", e.getMessage(), e);
            return createErrorResponse(Response.Status.INTERNAL_SERVER_ERROR,
                    ResponseMessages.ControllerInternalError.toString());
        }
    }

    @DELETE
    @Path("/{id}")
    public Response deleteReading(@PathParam("id") UUID id) throws JsonProcessingException
    {
        logger.info("Received request to delete reading with ID: {}", id);
        try (ReadingService rs = ServiceProvider.Services.getReadingService())
        {
            Reading reading = rs.getById(id);
            rs.remove(reading);
            logger.info("Reading deleted successfully: {}", reading);
            return Response.status(Response.Status.OK)
                    .entity(Utils.packIntoJsonString(reading, Reading.class))
                    .type(MediaType.APPLICATION_JSON)
                    .build();
        } catch (SQLException | ReflectiveOperationException e)
        {
            logger.error("Error deleting reading: {}", e.getMessage(), e);
            return createErrorResponse(Response.Status.BAD_REQUEST,
                    ResponseMessages.ControllerBadRequest.toString());
        } catch (IOException e)
        {
            logger.error("Internal server error: {}", e.getMessage(), e);
            return createErrorResponse(Response.Status.INTERNAL_SERVER_ERROR,
                    ResponseMessages.ControllerInternalError.toString());
        }
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getReadings(@QueryParam("customer") String customerId,
                                @QueryParam("start") String startDate,
                                @QueryParam("end") String endDate,
                                @QueryParam("kindOfMeter") Integer kindOfMeter) throws JsonProcessingException
    {
        logger.info("Received request to get readings with parameters - customer: {}, start: {}, end: {}, kindOfMeter: {}",
                customerId, startDate, endDate, kindOfMeter);
        try
        {
            UUID id = customerId != null ? UUID.fromString(customerId) : null;

            LocalDate start = null;
            if (startDate != null)
            {
                if (!startDate.matches("\\d{4}-\\d{2}-\\d{2}"))
                {
                    logger.warn("Invalid start date format: {}", startDate);
                    return createErrorResponse(Response.Status.BAD_REQUEST,
                            ResponseMessages.InvalidDateFormatProvided.toString());
                }
                start = LocalDate.parse(startDate);
            }
            LocalDate end = null;
            if (endDate != null)
            {
                if (!endDate.matches("\\d{4}-\\d{2}-\\d{2}"))
                {
                    logger.warn("Invalid end date format: {}", endDate);
                    return createErrorResponse(Response.Status.BAD_REQUEST,
                            ResponseMessages.InvalidDateFormatProvided.toString());
                }
                end = LocalDate.parse(endDate);
            }
            IReading.KindOfMeter meterType = null;
            if (kindOfMeter != null)
            {
                if (kindOfMeter >= 0 && kindOfMeter < IReading.KindOfMeter.values().length)
                {
                    meterType = IReading.KindOfMeter.values()[kindOfMeter];
                } else
                {
                    logger.warn("Invalid kind of meter provided: {}", kindOfMeter);
                    return createErrorResponse(Response.Status.BAD_REQUEST,
                            ResponseMessages.InvalidKindOfMeterProvided.toString());
                }
            }
            try (ReadingService rs = ServiceProvider.Services.getReadingService())
            {
                Collection<Reading> queryResults = rs.queryReadings(Optional.ofNullable(id), Optional.ofNullable(start),
                        Optional.ofNullable(end), Optional.ofNullable(meterType));
                logger.info("Readings retrieved successfully");
                return Response.status(Response.Status.OK)
                        .entity(Utils.packIntoJsonString(queryResults, Reading.class))
                        .build();
            }
        } catch (SQLException | IOException | ReflectiveOperationException e)
        {
            logger.error("Error retrieving readings: {}", e.getMessage(), e);
            return createErrorResponse(Response.Status.INTERNAL_SERVER_ERROR,
                    ResponseMessages.ControllerInternalError.toString());
        }
    }

    @GET
    @Path("/exportReadings")
    @Produces({MediaType.TEXT_PLAIN, MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response exportReadings(@QueryParam("kindOfMeter") IReading.KindOfMeter kindOfMeter, @QueryParam("fileType") String fileType)
    {
        try
        {
            if (fileType == null || fileType.isEmpty())
            {
                return Response.status(Response.Status.BAD_REQUEST).entity("Missing Content-Type header").build();
            }
            if (kindOfMeter == null)
            {
                return Response.status(Response.Status.BAD_REQUEST).entity("Missing kindOfMeter").build();
            }

            switch (fileType)
            {
                case "xml":
                {
                    String readingXmlString = getReadingXmlData(kindOfMeter);
                    return Response.status(Response.Status.OK)
                            .type(MediaType.APPLICATION_XML)
                            .entity(readingXmlString)
                            .build();

                }
                case "csv": {
                    String readingCsvString = getReadingCsvData(kindOfMeter);
                    return Response.status(Response.Status.OK)
                            .type(MediaType.APPLICATION_XML)
                            .entity(readingCsvString)
                            .build();
                }
                case "json": {
                    String readingJsonString = getReadingJsonData(kindOfMeter);
                    return Response.status(Response.Status.OK)
                            .type(MediaType.APPLICATION_XML)
                            .entity(readingJsonString)
                            .build();
                }
                default: {
                    return Response.status(Response.Status.UNSUPPORTED_MEDIA_TYPE)
                            .entity("Unsupported Content-Type: " + fileType)
                            .build();
                }
            }
        } catch (Exception e)
        {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("An error occurred: " + e.getMessage())
                    .build();
        }
    }

    private String getReadingJsonData(IReading.KindOfMeter kindOfMeter) throws IOException, ReflectiveOperationException, SQLException
    {
        ReadingService rs = ServiceProvider.Services.getReadingService();
        List<Reading> allReadings = rs.getAll();
        List<Reading> typeReadings = allReadings.stream().filter(e -> e.getKindOfMeter() == kindOfMeter).toList();
        return Utils.packIntoJsonString(typeReadings, Reading.class);
    }

    private String getReadingXmlData(IReading.KindOfMeter kindOfMeter) throws JAXBException, ReflectiveOperationException, SQLException, IOException
    {
        ReadingService rs = ServiceProvider.Services.getReadingService();
        List<Reading> allReadings = rs.getAll();
        List<Reading> typeReadings = allReadings.stream().filter(e -> e.getKindOfMeter() == kindOfMeter).toList();

        return Serializer.serializeIntoXml(typeReadings);
    }

    private String getReadingCsvData(IReading.KindOfMeter kindOfMeter) throws IOException, ReflectiveOperationException, SQLException
    {
        CsvParser parser = new CsvParser();
        return parser.createReadingsByKindOfMeter(kindOfMeter);
    }


    @POST
    @Path("/validateReadings")
    @Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML, MediaType.TEXT_PLAIN})
    @Produces(MediaType.APPLICATION_JSON)
    public Response validateReadings(@HeaderParam("Content-Type") String contentType, String fileContent) throws IOException, JAXBException, ReflectiveOperationException, SQLException
    {
        if (contentType.trim().isEmpty() || fileContent.trim().isEmpty()) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(ResponseMessages.ControllerBadRequest.toString()).build();
        }
        String fileType = Utils.formatContentType(contentType);
        String jsonContent = "";

        if (fileType.equals("json") || fileType.equals("xml") || fileType.equals("csv"))
        {
            List<Reading> readings = (List<Reading>) Serializer.deserializeFile(contentType, fileContent, Reading.class);
            jsonContent = Utils.packIntoJsonString(readings, Reading.class);
        }

        return Response.ok(jsonContent).build();
    }

}