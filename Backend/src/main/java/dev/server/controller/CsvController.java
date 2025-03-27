package dev.server.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.hv.Utils;
import dev.hv.csv.CsvFormatter;
import dev.hv.csv.CsvParser;
import dev.hv.model.classes.Customer;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.io.IOException;
import java.util.*;

@Path("/csv")
public class CsvController
{

    @POST
    @Path("/validateCsv")
    @Consumes(MediaType.TEXT_PLAIN)
    @Produces(MediaType.APPLICATION_JSON)
    public Response validate(String csvContent) throws IOException
    {
        String[] defaultReadingHeaderWater = {"Datum", "Zählerstand in m³", "Kommentar"};
        String[] defaultReadingHeaderElectricity = {"Datum", "Zählerstand in kWh", "Kommentar"};
        String[] defaultReadingHeaderHeat = {"Datum", "Zählerstand in MWh", "Kommentar"};

        String[] defaultCustomerHeader = {"UUID", "Anrede", "Vorname", "Nachname", "Geburtsdatum"};

        String[] customReadingHeader = {"Datum", "Zählerstand", "Kommentar", "KundenId", "Zählerart", "ZählerstandId", "Ersatz"};
        String[] customCustomerHeader = {"UUID", "Anrede", "Vorname", "Nachname", "Geburtsdatum"};


        CsvParser parser = new CsvParser();
        parser.setCsvContent(csvContent);
        List<String> csvReadingHeader = List.copyOf((java.util.Collection<? extends String>) parser.getReadingHeader());
        List<String> csvCustomerHeader = List.copyOf((java.util.Collection<? extends String>) parser.getCustomerHeader());

        Map<String, String> successData = new HashMap<>();

        if (Arrays.equals(csvReadingHeader.toArray(), defaultReadingHeaderWater)) {
            successData.put("class", "reading");
            successData.put("type", "default");
            successData.put("meter", "water");
        } else if (Arrays.equals(csvReadingHeader.toArray(), defaultReadingHeaderElectricity)) {
            successData.put("class", "reading");
            successData.put("type", "default");
            successData.put("meter", "electricity");
        } else if (Arrays.equals(csvReadingHeader.toArray(), defaultReadingHeaderHeat)) {
            successData.put("class", "reading");
            successData.put("type", "default");
            successData.put("meter", "heat");
        }
        else if (Arrays.equals(csvCustomerHeader.toArray(), defaultCustomerHeader)) {
            successData.put("class", "customer");
            successData.put("type", "default");
            successData.put("meter", "unknown");
        }
        else if (Arrays.equals(csvReadingHeader.toArray(), customReadingHeader)) {
            successData.put("class", "reading");
            successData.put("type", "custom");
            successData.put("meter", "unknown");
        }
        else if (Arrays.equals(csvCustomerHeader.toArray(), customCustomerHeader)) {
            successData.put("class", "customer");
            successData.put("type", "custom");
            successData.put("meter", "unknown");
        }else {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("{\"error\": \"CSV-Header passt nicht zu einem bekannten Typ.\"}")
                    .build();
        }
        ObjectMapper objectMapper = new ObjectMapper();
        String jsonResponse = objectMapper.writeValueAsString(Collections.singletonMap("success", successData));

        return Response.ok(jsonResponse).build();
    }


    @POST
    @Path("/readingValues")
    @Consumes(MediaType.TEXT_PLAIN)
    @Produces(MediaType.APPLICATION_JSON)
    public Response formatReadingValues(String csvContent)
    {
        try
        {
            CsvParser parser = new CsvParser();
            CsvFormatter formatter = new CsvFormatter();
            parser.setCsvContent(formatter.formatReadingCsv(csvContent));
            Iterable<List<String>> values = parser.getReadingValues();

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
    @Path("/customerValues")
    @Consumes(MediaType.TEXT_PLAIN)
    @Produces(MediaType.APPLICATION_JSON)
    public Response formatCustomerValues(String csvContent)
    {
        try
        {
            CsvParser parser = new CsvParser();
            CsvFormatter formatter = new CsvFormatter();
            parser.setCsvContent(formatter.formatCustomerCsv(csvContent));
            Iterable<List<String>> values = parser.getCustomerValues();

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
            parser.setCsvContent(formatter.formatReadingCsv(csvContent));
            Iterable<String> values = parser.getReadingHeader();

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
            parser.setCsvContent(formatter.formatReadingCsv(csvContent));
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
        try
        {
            customerJson = Utils.unpackFromJsonString(customerJson, Customer.class);
            CsvParser parser = new CsvParser();
            Customer customer = Utils.getObjectMapper().readValue(customerJson, Customer.class);
            String csvData = parser.createReadingsCsvFromCustomer(customer);
            return Response.status(Response.Status.OK)
                    .type(MediaType.TEXT_PLAIN)
                    .entity(csvData)
                    .build();
        } catch (Exception e)
        {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("An error occurred while processing the CSV file: " + e.getMessage())
                    .build();
        }
    }

}
