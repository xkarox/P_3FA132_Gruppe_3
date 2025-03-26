package dev.server.controller;

import dev.hv.ResponseMessages;
import dev.hv.Utils;
import dev.hv.database.services.AuthorisationService;
import dev.hv.model.enums.UserPermissions;
import dev.hv.model.interfaces.IReading;
import dev.provider.ServiceProvider;
import dev.hv.database.services.ReadingService;
import dev.hv.model.classes.Reading;
import com.fasterxml.jackson.core.JsonProcessingException;
import dev.server.Annotations.Secured;
import dev.server.validator.ReadingJsonSchemaValidationService;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.*;

import static dev.hv.Utils.createErrorResponse;

@Secured
@Path("/readings")
public class ReadingController {

    private static final Logger logger = LoggerFactory.getLogger(ReadingController.class);

    private Response validateRequestData(String jsonString) throws JsonProcessingException {
        logger.debug("Validating request data: {}", jsonString);
        boolean invalidReading = ReadingJsonSchemaValidationService.getInstance().validate(jsonString);
        if (invalidReading) {
            logger.warn("Invalid reading data: {}", jsonString);
            return createErrorResponse(Response.Status.BAD_REQUEST,
                    ResponseMessages.ControllerBadRequest.toString());
        }
        return null;
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response addReading(String readingJson) throws JsonProcessingException {
        logger.info("Received request to add reading: {}", readingJson);
        Response invalid = this.validateRequestData(readingJson);
        if (invalid != null) {
            return invalid;
        }

        try (ReadingService rs = ServiceProvider.Services.getReadingService()) {
            readingJson = Utils.unpackFromJsonString(readingJson, Reading.class);
            Reading reading = Utils.getObjectMapper().readValue(readingJson, Reading.class);

            if (AuthorisationService.CanUserAccessResource(reading.getCustomerId())) {
                return createErrorResponse(Response.Status.UNAUTHORIZED, ResponseMessages.ControllerUnauthorized.toString());
            }

            if (reading.getId() == null) {
                reading.setId(UUID.randomUUID());
            }
            reading = rs.add(reading);
            logger.info("Reading added successfully: {}", reading);
            return Response.status(Response.Status.CREATED)
                    .entity(Utils.packIntoJsonString(reading, Reading.class))
                    .type(MediaType.APPLICATION_JSON)
                    .build();
        } catch (JsonProcessingException | SQLException | ReflectiveOperationException e) {
            logger.error("Error adding reading: {}", e.getMessage(), e);
            return createErrorResponse(Response.Status.BAD_REQUEST,
                    ResponseMessages.ControllerBadRequest.toString());
        } catch (IOException e) {
            logger.error("Internal server error: {}", e.getMessage(), e);
            return createErrorResponse(Response.Status.INTERNAL_SERVER_ERROR,
                    ResponseMessages.ControllerInternalError.toString());
        }
    }

    @PUT
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response updateReading(String readingJson) throws JsonProcessingException {
        logger.info("Received request to update reading: {}", readingJson);
        Response invalid = this.validateRequestData(readingJson);
        if (invalid != null) {
            return invalid;
        }
        try (ReadingService rs = ServiceProvider.Services.getReadingService()) {
            readingJson = Utils.unpackFromJsonString(readingJson, Reading.class);
            Reading reading = Utils.getObjectMapper().readValue(readingJson, Reading.class);
            if (rs.getById(reading.getId()) == null) {
                logger.warn("Reading not found: {}", reading.getId());
                return createErrorResponse(Response.Status.NOT_FOUND,
                        ResponseMessages.ControllerNotFound.toString());
            }

            if (AuthorisationService.CanUserAccessResource(reading.getId())) {
                return createErrorResponse(Response.Status.UNAUTHORIZED, ResponseMessages.ControllerUnauthorized.toString());
            }

            rs.update(reading);
            logger.info("Reading updated successfully: {}", reading);
            return Response.status(Response.Status.OK)
                    .entity(Utils.packIntoJsonString(reading, Reading.class))
                    .type(MediaType.APPLICATION_JSON)
                    .build();
        } catch (JsonProcessingException | ReflectiveOperationException | SQLException e) {
            logger.error("Error updating reading: {}", e.getMessage(), e);
            return createErrorResponse(Response.Status.BAD_REQUEST,
                    ResponseMessages.ControllerBadRequest.toString());
        } catch (IOException e) {
            logger.error("Internal server error: {}", e.getMessage(), e);
            return createErrorResponse(Response.Status.INTERNAL_SERVER_ERROR,
                    ResponseMessages.ControllerInternalError.toString());
        }
    }

    @GET
    @Path("/{id}")
    public Response getReading(@PathParam("id") UUID id) throws JsonProcessingException {
        logger.info("Received request to get reading with ID: {}", id);

        if (AuthorisationService.CanUserAccessResource(id)) {
            return createErrorResponse(Response.Status.UNAUTHORIZED, ResponseMessages.ControllerUnauthorized.toString());
        }

        try (ReadingService rs = ServiceProvider.Services.getReadingService()) {
            Reading reading = rs.getById(id);
            logger.info("Reading retrieved successfully: {}", reading);
            return Response.status(Response.Status.OK)
                    .entity(Utils.packIntoJsonString(reading, Reading.class))
                    .type(MediaType.APPLICATION_JSON)
                    .build();
        } catch (SQLException | ReflectiveOperationException e) {
            logger.error("Error retrieving reading: {}", e.getMessage(), e);
            return createErrorResponse(Response.Status.BAD_REQUEST,
                    ResponseMessages.ControllerBadRequest.toString());
        } catch (IOException e) {
            logger.error("Internal server error: {}", e.getMessage(), e);
            return createErrorResponse(Response.Status.INTERNAL_SERVER_ERROR,
                    ResponseMessages.ControllerInternalError.toString());
        }
    }

    @DELETE
    @Path("/{id}")
    public Response deleteReading(@PathParam("id") UUID id) throws JsonProcessingException {
        logger.info("Received request to delete reading with ID: {}", id);

        if (AuthorisationService.CanUserAccessResource(id)) {
            return createErrorResponse(Response.Status.UNAUTHORIZED, ResponseMessages.ControllerUnauthorized.toString());
        }

        try (ReadingService rs = ServiceProvider.Services.getReadingService()) {
            Reading reading = rs.getById(id);
            rs.remove(reading);
            logger.info("Reading deleted successfully: {}", reading);
            return Response.status(Response.Status.OK)
                    .entity(Utils.packIntoJsonString(reading, Reading.class))
                    .type(MediaType.APPLICATION_JSON)
                    .build();
        } catch (SQLException | ReflectiveOperationException e) {
            logger.error("Error deleting reading: {}", e.getMessage(), e);
            return createErrorResponse(Response.Status.BAD_REQUEST,
                    ResponseMessages.ControllerBadRequest.toString());
        } catch (IOException e) {
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
                                @QueryParam("kindOfMeter") Integer kindOfMeter) throws JsonProcessingException {
        logger.info("Received request to get readings with parameters - customer: {}, start: {}, end: {}, kindOfMeter: {}",
                customerId, startDate, endDate, kindOfMeter);

        if (!AuthorisationService.IsUserAdmin())
            return createErrorResponse(Response.Status.UNAUTHORIZED, ResponseMessages.ControllerUnauthorized.toString());

        try {
            UUID id = customerId != null ? UUID.fromString(customerId) : null;

            LocalDate start = null;
            if (startDate != null) {
                if (!startDate.matches("\\d{4}-\\d{2}-\\d{2}")) {
                    logger.warn("Invalid start date format: {}", startDate);
                    return createErrorResponse(Response.Status.BAD_REQUEST,
                            ResponseMessages.InvalidDateFormatProvided.toString());
                }
                start = LocalDate.parse(startDate);
            }
            LocalDate end = null;
            if (endDate != null) {
                if (!endDate.matches("\\d{4}-\\d{2}-\\d{2}")) {
                    logger.warn("Invalid end date format: {}", endDate);
                    return createErrorResponse(Response.Status.BAD_REQUEST,
                            ResponseMessages.InvalidDateFormatProvided.toString());
                }
                end = LocalDate.parse(endDate);
            }
            IReading.KindOfMeter meterType = null;
            if (kindOfMeter != null) {
                if (kindOfMeter >= 0 && kindOfMeter < IReading.KindOfMeter.values().length) {
                    meterType = IReading.KindOfMeter.values()[kindOfMeter];
                } else {
                    logger.warn("Invalid kind of meter provided: {}", kindOfMeter);
                    return createErrorResponse(Response.Status.BAD_REQUEST,
                            ResponseMessages.InvalidKindOfMeterProvided.toString());
                }
            }
            try (ReadingService rs = ServiceProvider.Services.getReadingService()) {
                Collection<Reading> queryResults = rs.queryReadings(Optional.ofNullable(id), Optional.ofNullable(start),
                        Optional.ofNullable(end), Optional.ofNullable(meterType));
                logger.info("Readings retrieved successfully");
                return Response.status(Response.Status.OK)
                        .entity(Utils.packIntoJsonString(queryResults, Reading.class))
                        .build();
            }
        } catch (SQLException | IOException | ReflectiveOperationException e) {
            logger.error("Error retrieving readings: {}", e.getMessage(), e);
            return createErrorResponse(Response.Status.INTERNAL_SERVER_ERROR,
                    ResponseMessages.ControllerInternalError.toString());
        }
    }
}