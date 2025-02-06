package dev.server.controller;

import dev.hv.csv.CsvParser;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.Response;

import java.io.IOException;
import java.util.Dictionary;
import java.util.List;
import java.util.Map;

@Path("csv")
public class CsvController
{

    @POST
    @Path("/values")
    public Response formatValues(String csvContent) throws IOException
    {
        CsvParser parser = new CsvParser(csvContent);
        Iterable<List<String>> values = parser.getValues();

        return Response.ok(values).build();
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
