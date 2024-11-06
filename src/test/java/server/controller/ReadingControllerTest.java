package server.controller;

import ace.database.DatabaseConnection;
import ace.model.classes.Customer;
import ace.model.classes.Reading;
import ace.model.interfaces.ICustomer;
import ace.model.interfaces.IReading;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import server.Server;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class ReadingControllerTest
{
    DatabaseConnection _connection;
    String _url = "http://0.0.0.0:8080/readings";
    String _customerUrl = "http://0.0.0.0:8080/customers";
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
        Server.startServer(" ");
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
    }

    @Test
    void addReadingWithId() throws IOException, InterruptedException
    {
        String jsonString = _objMapper.writeValueAsString(this._reading);
        Reading reading = _objMapper.readValue(jsonString, Reading.class);
        System.out.println(reading);
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(_url))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(jsonString))
                .build();
        HttpResponse<String> response = _httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(HttpStatus.CREATED.value(), response.statusCode(), "Should return status code 201 CREATED");
        assertEquals(jsonString, response.body(), "Should return the same object send in request");
        System.out.println();
    }

    @Test
    void addReadingWithoutId() throws IOException, InterruptedException
    {
        Reading readingWithoutId = new Reading();
        readingWithoutId.setComment("Level is over 9k >.>");
        readingWithoutId.setDateOfReading(LocalDate.of(2024, 5, 4));
        readingWithoutId.setKindOfMeter(IReading.KindOfMeter.STROM);
        readingWithoutId.setMeterCount(625197.7);
        readingWithoutId.setMeterId("X1D3-ABCD");
        readingWithoutId.setSubstitute(false);
        readingWithoutId.setCustomer(this._customer);

        String jsonString = _objMapper.writeValueAsString(readingWithoutId);
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(_url))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(jsonString))
                .build();

        HttpResponse<String> response = _httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        System.out.println(response.body());
        Reading reading = _objMapper.readValue(response.body(), Reading.class);


        assertEquals(response.statusCode(), HttpStatus.CREATED.value(), "Should return status code 201 CREATED");
        assertEquals(readingWithoutId.getComment(), reading.getComment(), "Comment should match");
        assertEquals(readingWithoutId.getDateOfReading(), reading.getDateOfReading(), "Date of reading should match");
        assertEquals(readingWithoutId.getKindOfMeter(), reading.getKindOfMeter(), "Kind of meter should match");
        assertEquals(readingWithoutId.getMeterCount(), reading.getMeterCount(), "Meter count should match");
        assertEquals(readingWithoutId.getMeterId(), reading.getMeterId(), "Meter ID should match");
        assertEquals(readingWithoutId.getSubstitute(), reading.getSubstitute(), "Substitute flag should match");
        assertNotEquals(readingWithoutId.getId(), reading.getId(), "ID should be set");
    }

    @Test
    void addReadingWithNewCustomer() throws IOException, InterruptedException
    {
        this._reading.setCustomer(this._customer);
        String jsonString = _objMapper.writeValueAsString(this._reading);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(_url))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(jsonString))
                .build();

        HttpResponse<String> response = _httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        Reading reading = _objMapper.readValue(response.body(), Reading.class);


        assertEquals(this._reading, reading, "Should return the same object");
//        TODO check if customer is added to db
    }

    @Test
    void addReadingWithExistingCustomer() throws IOException, InterruptedException
    {
        boolean customerAddSuccess = false;
        String customerJsonString = _objMapper.writeValueAsString(this._customer);
        HttpRequest customerRequest = HttpRequest.newBuilder()
                .uri(URI.create(this._customerUrl))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(customerJsonString))
                .build();
        HttpResponse<String> customerResponse = _httpClient.send(customerRequest, HttpResponse.BodyHandlers.ofString());
        if (customerResponse.statusCode() == 201)
        {
            customerAddSuccess = true;
            String readingJsonString = _objMapper.writeValueAsString(this._reading);
            HttpRequest readingRequest = HttpRequest.newBuilder()
                    .uri(URI.create(_url))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(readingJsonString))
                    .build();
            HttpResponse<String> readingResponse = _httpClient.send(readingRequest, HttpResponse.BodyHandlers.ofString());
            Reading reading = _objMapper.readValue(readingResponse.body(), Reading.class);
            assertEquals(this._reading, reading, "Should return the same object");
        }
        assertTrue(customerAddSuccess);
    }

    @Test
    void addReadingEmptyBody() throws IOException, InterruptedException
    {
        String jsonString = "{}";
        HttpRequest customerRequest = HttpRequest.newBuilder()
                .uri(URI.create(this._url))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(jsonString))
                .build();
        HttpResponse<String> response = _httpClient.send(customerRequest, HttpResponse.BodyHandlers.ofString());
        Map<String, Object> body = _objMapper.readValue(response.body(), new TypeReference<Map<String, Object>>() {});

        assertEquals(HttpStatus.BAD_REQUEST.value(), response.statusCode(), "Status code should be 400 BAD REQUEST");
        assertEquals("Invalid reading data provided", body.get("message"), "Message should be Invalid reading data provided");
    }

    @Test
    void addReadingInvalidObject() throws IOException, InterruptedException
    {
        String jsonString = "sdfnasdf3223{2#$//";
        HttpRequest customerRequest = HttpRequest.newBuilder()
                .uri(URI.create(this._url))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(jsonString))
                .build();
        HttpResponse<String> response = _httpClient.send(customerRequest, HttpResponse.BodyHandlers.ofString());
        Map<String, Object> body = _objMapper.readValue(response.body(), new TypeReference<Map<String, Object>>() {});

        assertEquals(HttpStatus.BAD_REQUEST.value(), response.statusCode(), "Status code should be 400 BAD REQUEST");
        assertEquals("Invalid reading data provided", body.get("message"), "Message should be Invalid reading data provided");
    }
}
