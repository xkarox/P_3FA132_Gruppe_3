package dev.server.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import dev.hv.database.DatabaseConnection;
import dev.hv.database.provider.InternalServiceProvider;
import dev.hv.model.interfaces.ICustomer;
import dev.hv.model.interfaces.IReading;
import dev.hv.model.classes.Customer;
import dev.hv.model.classes.Reading;
import dev.provider.ServiceProvider;
import dev.server.Server;
import jakarta.ws.rs.core.Response;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

public class DatabaseControllerTest
{
    DatabaseConnection _connection;
    String _url = "http://0.0.0.0:8080/setupDB";
    HttpClient _httpClient;
    Customer _customer;
    Reading _reading;
    ObjectMapper _objMapper;

    Customer getTestCustomer()
    {
        Customer customer = new Customer(null);
        customer.setFirstName("Latten");
        customer.setLastName("Sep");
        customer.setBirthDate(LocalDate.of(1995, 5, 6));
        customer.setGender(ICustomer.Gender.M);
        return customer;
    }

    Reading getTestReading()
    {
        Reading reading = new Reading(null);
        reading.setComment("Level is over 9k >.>");
        reading.setDateOfReading(LocalDate.of(2024, 5, 4));
        reading.setKindOfMeter(IReading.KindOfMeter.STROM);
        reading.setMeterCount(625197.7);
        reading.setMeterId("X1D3-ABCD");
        reading.setSubstitute(false);
        return reading;
    }

    @BeforeEach
    void setUp() throws IOException, SQLException
    {
        Server.startServer("http://localhost:8080/");
        this._httpClient = HttpClient.newHttpClient();

        if(_connection == null)
        {
            this._connection = new DatabaseConnection();
            this._connection.openConnection();
            this._connection.truncateAllTables();
            this._connection.createAllTables();
        }
        else
        {
            this._connection.truncateAllTables();
        }

        this._objMapper = new ObjectMapper();
        this._objMapper.registerModule(new JavaTimeModule());
        this._objMapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
        this._objMapper.setDateFormat(new SimpleDateFormat("yyyy-MM-dd"));

        this._customer = this.getTestCustomer();
        this._reading = this.getTestReading();
        this._reading.setCustomer(this._customer);
    }

    @AfterEach
    void tearDown()
    {
        Server.stopServer();
        ServiceProvider.Services = new InternalServiceProvider(100, 10, 10);;
    }

    @Test
    void testSetupDatabase() throws IOException, InterruptedException, SQLException
    {
        ServiceProvider.Services = mock(InternalServiceProvider.class);
        DatabaseConnection mockDbCon = mock(DatabaseConnection.class);
        doNothing().when(mockDbCon).removeAllTables();
        doNothing().when(mockDbCon).createAllTables();
        when(ServiceProvider.Services.getDatabaseConnection()).thenReturn(mockDbCon);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(_url))
                .header("Content-Type", "application/json")
                .DELETE()
                .build();

        HttpResponse<String> response = _httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(Response.Status.OK.getStatusCode(), response.statusCode(), "Should return status code 200 OK");
    }

    @Test
    void testSetupDatabaseThrowsException() throws IOException, InterruptedException, SQLException
    {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(_url))
                .header("Content-Type", "application/json")
                .DELETE()
                .build();

        ServiceProvider.Services = mock(InternalServiceProvider.class);
        when(ServiceProvider.Services.getDatabaseConnection()).thenThrow(new SQLException());
        HttpResponse<String> response = _httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(), response.statusCode(), "Should return status code 500 Internal Server Error");

        ServiceProvider.Services = mock(InternalServiceProvider.class);
        when(ServiceProvider.Services.getDatabaseConnection()).thenThrow(new IOException());
        response = _httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(), response.statusCode(), "Should return status code 500 Internal Server Error");
    }
}
