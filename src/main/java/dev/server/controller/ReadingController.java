package dev.server.controller;

import dev.hv.ResponseMessages;
import dev.hv.Utils;
import dev.hv.model.IReading;
import dev.provider.ServiceProvider;
import dev.hv.database.services.ReadingService;
import dev.hv.model.classes.Reading;
import com.fasterxml.jackson.core.JsonProcessingException;
import dev.server.validator.ReadingJsonSchemaValidationService;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import javax.print.attribute.standard.Media;
import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.*;


@Path("/readings")
public class ReadingController
{
    private Response createErrorResponse(Response.Status status, String message) throws JsonProcessingException
    {
        Map<String, String> errorResponse = new HashMap<>();
        errorResponse.put("message", message);
        try
        {
            return Response.status(status)
                    .entity(Utils.getObjectMapper().writeValueAsString(errorResponse))
                    .type(MediaType.APPLICATION_JSON)
                    .build();
        }
        catch (JsonProcessingException e)
        {
            Map<String, String> response = new HashMap<>();
            response.put("message", ResponseMessages.ControllerInternalError.toString());
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(Utils.getObjectMapper().writeValueAsString(response))
                    .type(MediaType.APPLICATION_JSON)
                    .build();
        }
    }

    private Response validateRequestData(String jsonString) throws JsonProcessingException
    {
        boolean invalidReading = ReadingJsonSchemaValidationService.getInstance().validate(jsonString);
        if ( invalidReading )
        {
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
        Response invalid = this.validateRequestData(readingJson);
        if (invalid != null) {
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
            return Response.status(Response.Status.CREATED)
                    .entity(Utils.packIntoJsonString(reading, Reading.class))
                    .type(MediaType.APPLICATION_JSON)
                    .build();
        }
        catch (JsonProcessingException | SQLException | ReflectiveOperationException e)
        {
            return createErrorResponse(Response.Status.BAD_REQUEST,
                    ResponseMessages.ControllerBadRequest.toString());
        }
        catch (IOException e)
        {
            return createErrorResponse(Response.Status.INTERNAL_SERVER_ERROR,
                    ResponseMessages.ControllerInternalError.toString());
        }
    }

    @PUT
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response updateReading(String readingJson) throws JsonProcessingException
    {
        Response invalid = this.validateRequestData(readingJson);
        if (invalid != null) {
            return invalid;
        }
        try (ReadingService rs = ServiceProvider.Services.getReadingService())
        {
            readingJson = Utils.unpackFromJsonString(readingJson, Reading.class);
            Reading reading = Utils.getObjectMapper().readValue(readingJson, Reading.class);
            if (rs.getById(reading.getId()) == null)
            {
                return createErrorResponse(Response.Status.NOT_FOUND,
                        ResponseMessages.ControllerNotFound.toString());
            }
            rs.update(reading);
            return Response.status(Response.Status.OK)
                    .entity(Utils.packIntoJsonString(reading, Reading.class))
                    .type(MediaType.APPLICATION_JSON)
                    .build();
        }
        catch (JsonProcessingException | ReflectiveOperationException | SQLException e)
        {
            return createErrorResponse(Response.Status.BAD_REQUEST,
                    ResponseMessages.ControllerBadRequest.toString());
        }
        catch (IOException e)
        {
            return createErrorResponse(Response.Status.INTERNAL_SERVER_ERROR,
                    ResponseMessages.ControllerInternalError.toString());
        }
    }

    @GET
    @Path("/{id}")
    public Response getReading(@PathParam("id") UUID id) throws JsonProcessingException
    {
        try
        {
            try (ReadingService rs = ServiceProvider.Services.getReadingService())
            {
                Reading reading = rs.getById(id);
                return Response.status(Response.Status.OK)
                        .entity(Utils.packIntoJsonString(reading, Reading.class))
                        .type(MediaType.APPLICATION_JSON)
                        .build();
            }
        }
        catch (SQLException | ReflectiveOperationException e)
        {
            return createErrorResponse(Response.Status.BAD_REQUEST,
                    ResponseMessages.ControllerBadRequest.toString());
        }
        catch (IOException e)
        {
            return createErrorResponse(Response.Status.INTERNAL_SERVER_ERROR,
                    ResponseMessages.ControllerInternalError.toString());
        }
    }

    @DELETE
    @Path("/{id}")
    public Response deleteReading(@PathParam("id") UUID id) throws JsonProcessingException
    {
        try
        {
            try (ReadingService rs = ServiceProvider.Services.getReadingService())
            {
                Reading reading = rs.getById(id);
                ServiceProvider.Services.getReadingService().remove(reading);
                return Response.status(Response.Status.OK)
                    .entity(Utils.packIntoJsonString(reading, Reading.class))
                    .type(MediaType.APPLICATION_JSON)
                    .build();
            }
        }
        catch (SQLException | ReflectiveOperationException e)
        {
            return createErrorResponse(Response.Status.BAD_REQUEST,
                    ResponseMessages.ControllerBadRequest.toString());
        }
        catch (IOException e)
        {
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
        try {
            UUID id = customerId != null ? UUID.fromString(customerId) : null;

            LocalDate start = null;
            if (startDate != null)
            {
                if (!startDate.matches("\\d{4}-\\d{2}-\\d{2}"))
                {
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
                    return createErrorResponse(Response.Status.BAD_REQUEST,
                            ResponseMessages.InvalidKindOfMeterProvided.toString());
                }
            }
            String returnString = null;
            try (ReadingService rs = ServiceProvider.Services.getReadingService())
            {
                Collection<Reading> queryResults = rs.queryReadings(Optional.ofNullable(id), Optional.ofNullable(start),
                        Optional.ofNullable(end), Optional.ofNullable(meterType));

                return Response.status(Response.Status.OK)
                        .entity(Utils.packIntoJsonString(queryResults, Reading.class))
                        .build();
            }
        } catch (SQLException | IOException | ReflectiveOperationException e )
        {
            return createErrorResponse(Response.Status.INTERNAL_SERVER_ERROR,
                    ResponseMessages.ControllerInternalError.toString());
        }
    }
}
