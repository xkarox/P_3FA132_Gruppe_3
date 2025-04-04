package dev.server.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.hv.ResponseMessages;
import dev.hv.database.services.AuthorisationService;
import dev.hv.database.services.ReadingService;
import dev.hv.model.classes.Reading;
import dev.provider.ServiceProvider;
import dev.hv.Utils;
import dev.hv.database.services.CustomerService;
import dev.hv.model.classes.Customer;
import com.fasterxml.jackson.core.JsonProcessingException;
import dev.server.Annotations.Secured;
import dev.server.validator.CustomerJsonSchemaValidatorService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.io.IOException;
import java.net.URI;
import java.sql.SQLException;
import java.util.*;

import static dev.hv.Utils.createErrorResponse;

@Secured
@Path("/customers")
public class CustomerController {

    private static final Logger logger = LoggerFactory.getLogger(CustomerController.class);

    private Response validateRequestData(String jsonString) throws JsonProcessingException {
        logger.debug("Validating request data: {}", jsonString);
        boolean invalidCustomer = CustomerJsonSchemaValidatorService.getInstance().validate(jsonString);
        if (invalidCustomer) {
            logger.warn("Invalid customer data: {}", jsonString);
            return createErrorResponse(Response.Status.BAD_REQUEST,
                    ResponseMessages.ControllerBadRequest.toString());
        }
        return null;
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response addCustomer(String customerJson) throws JsonProcessingException {
        logger.info("Received request to add customer: {}", customerJson);

        if (!AuthorisationService.IsUserAdmin())
            return createErrorResponse(Response.Status.UNAUTHORIZED, ResponseMessages.ControllerUnauthorized.toString());

        Response validationResponse = validateRequestData(customerJson);
        if (validationResponse != null) return validationResponse;

        try (CustomerService cs = ServiceProvider.Services.getCustomerService()) {
            customerJson = Utils.unpackFromJsonString(customerJson, Customer.class);
            Customer customer = Utils.getObjectMapper().readValue(customerJson, Customer.class);

            if (customer.getId() == null) {
                customer.setId(UUID.randomUUID());
            }

            customer = cs.add(customer);
            logger.info("Customer added successfully: {}", customer);
            return Response.status(Response.Status.CREATED)
                    .entity(Utils.packIntoJsonString(customer, Customer.class))
                    .type(MediaType.APPLICATION_JSON)
                    .location(URI.create("/customers/" + customer.getId()))
                    .build();
        } catch (JsonProcessingException | SQLException | RuntimeException e) {
            logger.error("Error adding customer: {}", e.getMessage(), e);
            return createErrorResponse(Response.Status.BAD_REQUEST,
                    ResponseMessages.ControllerBadRequest.toString());
        } catch (IOException e) {
            logger.error("Internal server error: {}", e.getMessage(), e);
            return createErrorResponse(Response.Status.INTERNAL_SERVER_ERROR,
                    ResponseMessages.ControllerInternalError.toString());
        }
    }


    @POST
    @Path("/batch")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response addCustomerBatch(String batchCustomers) throws JsonProcessingException
    {
        if (!AuthorisationService.IsUserAdmin()) {
            return createErrorResponse(Response.Status.UNAUTHORIZED, ResponseMessages.ControllerUnauthorized.toString());
        }

        logger.info("Received request to add customers: {}", batchCustomers);
        try (CustomerService cs = ServiceProvider.Services.getCustomerService())
        {
            ObjectMapper mapper = new ObjectMapper();
            List<Customer> objectList = mapper.readValue(batchCustomers, new TypeReference<List<Customer>>() {});

            for (Customer customer : objectList)
            {
                if (customer.getId() == null)
                {
                    customer.setId(UUID.randomUUID());
                }
            }
            cs.addBatch(objectList);

            return Response.status(Response.Status.CREATED)
                    .type(MediaType.APPLICATION_JSON)
                    .build();
        }
        catch (JsonProcessingException | SQLException | RuntimeException e)
        {
            logger.error("Error adding customers: {}", e.getMessage(), e);
            return createErrorResponse(Response.Status.BAD_REQUEST,
                    ResponseMessages.ControllerBadRequest.toString());
        }
        catch (IOException e)
        {
            logger.error("Internal server error: {}", e.getMessage(), e);
            return createErrorResponse(Response.Status.INTERNAL_SERVER_ERROR,
                    ResponseMessages.ControllerInternalError.toString());
        }
    }

    @GET
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getCustomer(@PathParam("id") UUID id) throws JsonProcessingException {
        logger.info("Received request to get customer with ID: {}", id);

        if (!AuthorisationService.CanUserAccessResource(id)) {
            return createErrorResponse(Response.Status.UNAUTHORIZED, ResponseMessages.ControllerUnauthorized.toString());
        }

        try (CustomerService cs = ServiceProvider.Services.getCustomerService()) {
            Customer customer = cs.getById(id);
            logger.info("Customer retrieved successfully: {}", customer);
            return Response.status(Response.Status.OK)
                    .entity(Utils.packIntoJsonString(customer, Customer.class))
                    .type(MediaType.APPLICATION_JSON)
                    .build();
        } catch (IOException | ReflectiveOperationException | SQLException e) {
            logger.error("Error retrieving customer: {}", e.getMessage(), e);
            return createErrorResponse(Response.Status.INTERNAL_SERVER_ERROR,
                    ResponseMessages.ControllerInternalError.toString());
        }
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getCustomers() throws JsonProcessingException {
        logger.info("Received request to get all customers");

        if (!AuthorisationService.IsUserAdmin())
            return createErrorResponse(Response.Status.UNAUTHORIZED, ResponseMessages.ControllerUnauthorized.toString());

        try (CustomerService cs = ServiceProvider.Services.getCustomerService()) {
            Collection<Customer> customers = cs.getAll();
            logger.info("Customers retrieved successfully");
            return Response.status(Response.Status.OK)
                    .entity(Utils.packIntoJsonString(customers, Customer.class))
                    .build();
        } catch (IOException | ReflectiveOperationException | SQLException e) {
            logger.error("Error retrieving customers: {}", e.getMessage(), e);
            return createErrorResponse(Response.Status.INTERNAL_SERVER_ERROR,
                    ResponseMessages.ControllerInternalError.toString());
        }
    }

    @PUT
    @Produces(MediaType.APPLICATION_JSON)
    public Response updateCustomer(String customerJson) throws JsonProcessingException {
        logger.info("Received request to update customer: {}", customerJson);
        Response invalid = this.validateRequestData(customerJson);
        if (invalid != null) {
            return invalid;
        }
        try (CustomerService cs = ServiceProvider.Services.getCustomerService()) {
            customerJson = Utils.unpackFromJsonString(customerJson, Customer.class);
            Customer customer = Utils.getObjectMapper().readValue(customerJson, Customer.class);
            Customer dbCustomer = cs.getById(customer.getId());
            if (dbCustomer == null) {
                logger.warn("Customer not found: {}", customer.getId());
                return createErrorResponse(Response.Status.NOT_FOUND, ResponseMessages.ControllerNotFound.toString());
            }

            if (!AuthorisationService.CanUserAccessResource(dbCustomer.getId())) {
                return createErrorResponse(Response.Status.UNAUTHORIZED, ResponseMessages.ControllerUnauthorized.toString());
            }

            cs.update(customer);
            logger.info("Customer updated successfully: {}", customer);
            return Response.status(Response.Status.OK)
                    .entity(ResponseMessages.ControllerUpdateSuccess.toString())
                    .type(MediaType.APPLICATION_JSON)
                    .build();
        } catch (JsonProcessingException | ReflectiveOperationException | SQLException e) {
            logger.error("Error updating customer: {}", e.getMessage(), e);
            return createErrorResponse(Response.Status.BAD_REQUEST, ResponseMessages.ControllerBadRequest.toString());
        } catch (IOException e) {
            logger.error("Internal server error: {}", e.getMessage(), e);
            return createErrorResponse(Response.Status.INTERNAL_SERVER_ERROR,
                    ResponseMessages.ControllerInternalError.toString());
        }
    }

    @DELETE
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response deleteCustomer(@PathParam("id") UUID id) throws JsonProcessingException {
        logger.info("Received request to delete customer with ID: {}", id);

        if (!AuthorisationService.CanUserAccessResource(id)) {
            return createErrorResponse(Response.Status.UNAUTHORIZED, ResponseMessages.ControllerUnauthorized.toString());
        }

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

            logger.info("Customer deleted successfully: {}", customer);
            return Response.status(Response.Status.OK)
                    .entity(Utils.packIntoJsonString(response))
                    .type(MediaType.APPLICATION_JSON)
                    .build();
        } catch (IOException | ReflectiveOperationException | SQLException e) {
            logger.error("Error deleting customer: {}", e.getMessage(), e);
            return createErrorResponse(Response.Status.INTERNAL_SERVER_ERROR,
                    ResponseMessages.ControllerInternalError.toString());
        }
    }
}