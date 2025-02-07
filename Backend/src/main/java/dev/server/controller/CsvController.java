package dev.server.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.hv.csv.CsvParser;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.io.IOException;
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
    public Response formatValues(String csvContent) {
        try {
            CsvParser parser = new CsvParser(csvContent);
            Iterable<List<String>> values = parser.getValues();

            ObjectMapper objectMapper = new ObjectMapper();
            String json = objectMapper.writeValueAsString(values);
            System.out.print(json);

            return Response.status(Response.Status.OK)
                    .type(MediaType.APPLICATION_JSON)
                    .entity(json)
                    .build();
        } catch (IOException e) {

            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("An error occurred while processing the CSV file: " + e.getMessage())
                    .build();
        }
    }

    @POST
    @Path("/header")
    public Response formatHeader(String csvContent) throws IOException
    {
        CsvParser parser = new CsvParser(csvContent);
        Iterable<String> values = parser.getHeader();

        return Response.ok(values).build();
    }

    @POST
    @Path("/metaData")
    public Response formatMetaData(String csvContent) throws IOException
    {
        CsvParser parser = new CsvParser(csvContent);
        Iterable<Map<String, String>> values = parser.getMetaData();

        return Response.ok(values).build();
    }

}
