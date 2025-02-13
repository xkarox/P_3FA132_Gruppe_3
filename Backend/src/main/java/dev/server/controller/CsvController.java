package dev.server.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.hv.Utils;
import dev.hv.csv.CsvFormatter;
import dev.hv.csv.CsvParser;
import dev.hv.database.services.ReadingService;
import dev.hv.model.classes.Customer;
import dev.hv.model.classes.Reading;
import dev.provider.ServiceProvider;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Dictionary;
import java.util.List;
import java.util.Map;

@Path("/csv")
public class CsvController
{

    @POST
    @Path("/values")
    @Consumes(MediaType.TEXT_PLAIN)
    @Produces(MediaType.APPLICATION_JSON)
    public Response formatValues(String csvContent)
    {
        try
        {
            CsvParser parser = new CsvParser();
            CsvFormatter formatter = new CsvFormatter();
            formatter.formatFile(csvContent);
            Iterable<List<String>> values = parser.getValues();

            ObjectMapper objectMapper = new ObjectMapper();
            String json = objectMapper.writeValueAsString(values);
            System.out.print(json);

            return Response.status(Response.Status.OK)
                    .type(MediaType.APPLICATION_JSON)
                    .entity(json)
                    .build();
        } catch (IOException e)
        {

            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("An error occurred while processing the CSV file: " + e.getMessage())
                    .build();
        }
    }

    @POST
    @Path("/header")
    @Consumes(MediaType.TEXT_PLAIN)
    @Produces(MediaType.APPLICATION_JSON)
    public Response formatHeader(String csvContent)
    {
        try
        {
            CsvParser parser = new CsvParser();
            CsvFormatter formatter = new CsvFormatter();
            formatter.formatFile(csvContent);
            Iterable<String> values = parser.getHeader();

            ObjectMapper objectMapper = new ObjectMapper();
            String json = objectMapper.writeValueAsString(values);
            System.out.print(json);

            return Response.status(Response.Status.OK)
                    .type(MediaType.APPLICATION_JSON)
                    .entity(json)
                    .build();
        } catch (IOException e)
        {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("An error occurred while processing the CSV file: " + e.getMessage())
                    .build();
        }

    }

    @POST
    @Path("/metaData")
    @Consumes(MediaType.TEXT_PLAIN)
    @Produces(MediaType.APPLICATION_JSON)
    public Response formatMetaData(String csvContent)
    {
        try
        {
            CsvParser parser = new CsvParser();
            CsvFormatter formatter = new CsvFormatter();
            formatter.formatFile(csvContent);
            Iterable<Map<String, String>> values = parser.getMetaData();

            ObjectMapper objectMapper = new ObjectMapper();
            String json = objectMapper.writeValueAsString(values);
            System.out.print(json);

            return Response.status(Response.Status.OK)
                    .type(MediaType.APPLICATION_JSON)
                    .entity(json)
                    .build();
        } catch (IOException e)
        {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("An error occurred while processing the CSV file: " + e.getMessage())
                    .build();
        }

    }

    @POST
    @Path("/createReadingCsvFromCustomer")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.TEXT_PLAIN)
    public Response createReadingCsvFromCustomer(String customerJson)
    {
        try {
            customerJson = Utils.unpackFromJsonString(customerJson, Customer.class);
            CsvParser parser = new CsvParser();
            Customer customer = Utils.getObjectMapper().readValue(customerJson, Customer.class);
            String csvData = parser.createReadingsCsvFromCustomer(customer);
            return Response.status(Response.Status.OK)
                    .type(MediaType.TEXT_PLAIN)
                    .entity(csvData)
                    .build();
        }
        catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("An error occurred while processing the CSV file: " + e.getMessage())
                    .build();
        }
    }


}
