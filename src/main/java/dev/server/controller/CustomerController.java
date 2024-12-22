package dev.server.controller;

import dev.hv.ResponseMessages;
import dev.hv.database.services.ReadingService;
import dev.hv.model.classes.Reading;
import dev.provider.ServiceProvider;
import dev.hv.Utils;
import dev.hv.database.services.CustomerService;
import dev.hv.model.classes.Customer;
import com.fasterxml.jackson.core.JsonProcessingException;
import dev.server.validator.CustomerJsonSchemaValidatorService;

import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Application;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.io.IOException;
import java.net.URI;
import java.sql.SQLException;
import java.util.*;

import static dev.hv.Utils.createErrorResponse;

@Path("/customers")
public class CustomerController {

    private Response validateRequestData(String jsonString) throws JsonProcessingException
    {
        boolean invalidCustomer = CustomerJsonSchemaValidatorService.getInstance().validate(jsonString);
        if (invalidCustomer) {
            return createErrorResponse(Response.Status.BAD_REQUEST,
                    ResponseMessages.ControllerBadRequest.toString());
        }
        return null;
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response addCustomer(String customerJson) throws JsonProcessingException
    {
        Response validationResponse = validateRequestData(customerJson);
        if (validationResponse != null) return validationResponse;

        try (CustomerService cs = ServiceProvider.Services.getCustomerService()) {
            customerJson = Utils.unpackFromJsonString(customerJson, Customer.class);
            Customer customer = Utils.getObjectMapper().readValue(customerJson, Customer.class);

            if (customer.getId() == null) {
                customer.setId(UUID.randomUUID());
            }

            customer = cs.add(customer);
            return Response.status(Response.Status.CREATED)
                    .entity(Utils.packIntoJsonString(customer, Customer.class))
                    .type(MediaType.APPLICATION_JSON)
                    .location(URI.create("/customers/" + customer.getId()))
                    .build();
        } catch (JsonProcessingException | SQLException | RuntimeException e) {
            return createErrorResponse(Response.Status.BAD_REQUEST,
                    ResponseMessages.ControllerBadRequest.toString());
        } catch (IOException e) {
            return createErrorResponse(Response.Status.INTERNAL_SERVER_ERROR,
                    ResponseMessages.ControllerInternalError.toString());
        }
    }

    @GET
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getCustomer(@PathParam("id") UUID id) throws JsonProcessingException
    {
        try (CustomerService cs = ServiceProvider.Services.getCustomerService()) {
            Customer customer = cs.getById(id);
            return Response.status(Response.Status.OK)
                    .entity(Utils.packIntoJsonString(customer, Customer.class))
                    .type(MediaType.APPLICATION_JSON)
                    .build();
        } catch (IOException | ReflectiveOperationException | SQLException e) {
            return createErrorResponse(Response.Status.INTERNAL_SERVER_ERROR,
                    ResponseMessages.ControllerInternalError.toString());
        }
    }
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getCustomers() throws JsonProcessingException
    {
        try (CustomerService cs = ServiceProvider.Services.getCustomerService())
        {
            Collection<Customer> customer = cs.getAll();
            return Response.status(Response.Status.OK)
                    .entity(Utils.packIntoJsonString(customer, Customer.class))
                    .build();

        } catch (IOException | ReflectiveOperationException | SQLException e)
        {
            return createErrorResponse(Response.Status.INTERNAL_SERVER_ERROR, ResponseMessages.ControllerInternalError.toString());
        }
    }

    @PUT
    @Produces(MediaType.APPLICATION_JSON)
    public Response updateCustomer(String customerJson) throws JsonProcessingException
    {
        Response invalid = this.validateRequestData(customerJson);
        if (invalid != null) {
            return invalid;
        }
        try (CustomerService cs = ServiceProvider.Services.getCustomerService())
        {
            customerJson = Utils.unpackFromJsonString(customerJson, Customer.class);
            Customer customer = Utils.getObjectMapper().readValue(customerJson, Customer.class);
            Customer dbCustomer = cs.getById(customer.getId());
            if (dbCustomer == null)
            {
                return createErrorResponse(Response.Status.NOT_FOUND, ResponseMessages.ControllerNotFound.toString());
            }
            cs.update(customer);
            Map<String, String> responseBody = new HashMap<>();
            return Response.status(Response.Status.OK)
                    .entity(ResponseMessages.ControllerUpdateSuccess.toString())
                    .type(MediaType.APPLICATION_JSON)
                    .build();
        }
        catch (JsonProcessingException | ReflectiveOperationException | SQLException e)
        {
            return createErrorResponse(Response.Status.BAD_REQUEST, ResponseMessages.ControllerBadRequest.toString());
        }
        catch (IOException e)
        {
            return createErrorResponse(Response.Status.INTERNAL_SERVER_ERROR,
                    ResponseMessages.ControllerInternalError.toString());
        }
    }

    @DELETE
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response deleteCustomer(@PathParam("id") UUID id) throws JsonProcessingException
    {
        try (CustomerService cs = ServiceProvider.Services.getCustomerService();
             ReadingService rs = ServiceProvider.Services.getReadingService()) {

            Customer customer = cs.getById(id);

            Collection<Reading> readings = rs.getReadingsByCustomerId(customer.getId());
            cs.remove(customer);
            for (Reading reading : readings) {
                reading.setCustomer(null);
            }

            Map<String, Object> response = new LinkedHashMap<>();
            response.put("customer", customer);
            response.put("readings", readings);

            return Response.status(Response.Status.OK)
                    .entity(Utils.packIntoJsonString(response))
                    .type(MediaType.APPLICATION_JSON)
                    .build();
        } catch (IOException | ReflectiveOperationException | SQLException e) {
            return createErrorResponse(Response.Status.INTERNAL_SERVER_ERROR,
                    ResponseMessages.ControllerInternalError.toString());
        }
    }
}
