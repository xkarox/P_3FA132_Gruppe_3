package dev.server.controller;

import dev.hv.database.services.CustomerService;
import dev.hv.database.services.ReadingService;
import dev.hv.model.IId;
import dev.provider.ServiceProvider;
import dev.hv.Utils;
import dev.hv.database.DatabaseConnection;
import dev.hv.database.provider.InternalServiceProvider;
import dev.hv.model.classes.Customer;
import dev.hv.model.classes.Reading;
import dev.hv.model.ICustomer;
import dev.hv.model.IReading;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import dev.server.controller.ReadingController;
import org.apache.tomcat.util.http.parser.HttpParser;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import dev.server.Server;

import java.io.IOException;
import java.lang.reflect.Field;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.Collection;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

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

    void addReading(Reading reading) throws IOException, InterruptedException
    {
        try (ReadingService cs = ServiceProvider.Services.getReadingService()) {
            cs.add(reading);
        } catch (ReflectiveOperationException e)
        {
            throw new RuntimeException(e);
        } catch (SQLException e)
        {
            throw new RuntimeException(e);
        }
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
        ServiceProvider.Services = new InternalServiceProvider(100, 10, 10);;
    }

    @Test
    void addReadingWithId() throws IOException, InterruptedException
    {
        String jsonString = Utils.packIntoJsonString(this._reading, Reading.class);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(_url))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(jsonString))
                .build();
        HttpResponse<String> response = _httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(HttpStatus.CREATED.value(), response.statusCode(), "Should return status code 201 CREATED");
        assertEquals(jsonString, response.body(), "Should return the same object send in request");
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

        String jsonString = Utils.packIntoJsonString(readingWithoutId, Reading.class);
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(_url))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(jsonString))
                .build();

        HttpResponse<String> response = _httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        String readingString = Utils.unpackFromJsonString(response.body(), Reading.class);
        Reading reading = _objMapper.readValue(readingString, Reading.class);

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
        String jsonString = Utils.packIntoJsonString(this._reading, Reading.class);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(_url))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(jsonString))
                .build();

        HttpResponse<String> response = _httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        String readingString = Utils.unpackFromJsonString(response.body(), Reading.class);
        Reading reading = _objMapper.readValue(readingString, Reading.class);


        assertEquals(this._reading, reading, "Should return the same object");
//        TODO check if customer is added to db
    }

    @Test
    void addReadingWithExistingCustomer() throws IOException, InterruptedException
    {
        boolean customerAddSuccess = false;
        String customerJsonString = Utils.packIntoJsonString(this._customer, Customer.class);
        HttpRequest customerRequest = HttpRequest.newBuilder()
                .uri(URI.create(this._customerUrl))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(customerJsonString))
                .build();
        HttpResponse<String> customerResponse = _httpClient.send(customerRequest, HttpResponse.BodyHandlers.ofString());
        if (customerResponse.statusCode() == 201)
        {
            customerAddSuccess = true;
            String readingJsonString = Utils.packIntoJsonString(this._reading, Reading.class);
            HttpRequest readingRequest = HttpRequest.newBuilder()
                    .uri(URI.create(_url))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(readingJsonString))
                    .build();
            HttpResponse<String> readingResponse = _httpClient.send(readingRequest, HttpResponse.BodyHandlers.ofString());
            String readingString = Utils.unpackFromJsonString(readingResponse.body(), Reading.class);
            Reading reading = _objMapper.readValue(readingString, Reading.class);
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
        HttpRequest readingRequest = HttpRequest.newBuilder()
                .uri(URI.create(this._url))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(jsonString))
                .build();
        HttpResponse<String> response = _httpClient.send(readingRequest, HttpResponse.BodyHandlers.ofString());
        Map<String, Object> body = _objMapper.readValue(response.body(), new TypeReference<Map<String, Object>>() {});

        assertEquals(HttpStatus.BAD_REQUEST.value(), response.statusCode(), "Status code should be 400 BAD REQUEST");
        assertEquals("Invalid reading data provided", body.get("message"), "Message should be Invalid reading data provided");
    }

    @Test
    void addReadingIOException() throws Exception
    {
        ServiceProvider.Services = mock(InternalServiceProvider.class);
        when(ServiceProvider.Services.getReadingService()).thenThrow(IOException.class);

        String jsonString = Utils.packIntoJsonString(this._reading, Reading.class);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(_url))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(jsonString))
                .build();
        HttpResponse<String> response = _httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        Map<String, Object> body = _objMapper.readValue(response.body(), new TypeReference<Map<String, Object>>() {});
        assertEquals(HttpStatus.BAD_REQUEST.value(), response.statusCode(), "Should return a 400 BAD REQUEST");
        assertEquals("Internal Server IOError", body.get("message"), "Message should be 'Internal Server IOError'");
    }

    @Test
    void addReadingJsonProcessingException() throws SQLException, IOException, InterruptedException
    {
        String jsonString = Utils.packIntoJsonString(this._reading, Reading.class);

        ServiceProvider.Services = mock(InternalServiceProvider.class);
        when(ServiceProvider.Services.getReadingService()).thenThrow(JsonProcessingException.class);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(_url))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(jsonString))
                .build();

        HttpResponse<String> response = _httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        Map<String, Object> body = _objMapper.readValue(response.body(), new TypeReference<Map<String, Object>>() {});
        assertEquals(HttpStatus.BAD_REQUEST.value(), response.statusCode(), "Should return a 400 BAD REQUEST");
        assertEquals("Invalid reading data provided", body.get("message"), "Message should be 'Invalid reading data provided'");
    }

    @Test
    void updateReading() throws IOException, InterruptedException
    {
//        this.addReading();
    }

    @Test
    void getReadings() throws IOException, InterruptedException, SQLException
    {
        Customer customer = null;
        try (CustomerService cs = ServiceProvider.Services.getCustomerService())
        {
            customer = cs.add(this._customer);
        }

        this._reading.setId(UUID.randomUUID());
        this.addReading(this._reading);
        this._reading.setId(UUID.randomUUID());
        this.addReading(this._reading);
        this._reading.setId(UUID.randomUUID());
        this.addReading(this._reading);


        LocalDate startDate = LocalDate.of(2000, 11, 2);
        LocalDate endDate = LocalDate.of(2999, 11, 2);

        String url = new StringBuilder(_url)
                    .append("?customer=").append(customer.getId())
                    .append("&start=").append(startDate)
                    .append("&end=").append(endDate)
                    .append("&kindOfMeter=1")
                    .toString();

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .GET()
                .build();

        HttpResponse<String> response = _httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        Collection<? extends IId> unpackedResponse =
                Utils.unpackCollectionFromJsonString(response.body(), Reading.class);

        Reading reading = ((Reading)unpackedResponse.toArray()[0]);
        assertEquals(3, unpackedResponse.size(), "The response should contain 3 elements");
        assertTrue(unpackedResponse.contains(this._reading));
        assertEquals(IReading.KindOfMeter.STROM, reading.getKindOfMeter(), "Meter type should be STROM");
        assertEquals(reading.getCustomerId(), customer.getId(), "The customer id should match");
        assertTrue(endDate.isAfter(reading.getDateOfReading()) && startDate.isBefore(reading.getDateOfReading()), "The reading date should be between the start and end dates");
    }

    @Test
    void getReadingsStartDateFormatDoesntMatch() throws IOException, InterruptedException
    {
        String startDateWrongFormat = "10000-69-69";
        String url = new StringBuilder(_url)
                .append("?start=").append(startDateWrongFormat)
                .append("&kindOfMeter=1").toString();

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .GET()
                .build();

        HttpResponse<String> response = _httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(400, response.statusCode(), "Returned status code should be 400 Bad Request");
        assertTrue(response.body().contains("expected yyyy-mm-dd"));
    }

    @Test
    void getReadingsEndDateFormatDoesntMatch() throws IOException, InterruptedException
    {

        String startDateWrongFormat = "10000-69-69";
        String url = new StringBuilder(_url)
                .append("?end=").append(startDateWrongFormat)
                .append("&kindOfMeter=1").toString();

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .GET()
                .build();

        HttpResponse<String> response = _httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(400, response.statusCode(), "Returned status code should be 400 Bad Request");
        assertTrue(response.body().contains("expected yyyy-mm-dd"));
    }

    @Test
    void getReadingsInvalidKindOfMeter() throws IOException, InterruptedException
    {
        String url = new StringBuilder(_url)
                .append("?end=").append(LocalDate.of(2024, 12, 13))
                .append("&kindOfMeter=69").toString();

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .GET()
                .build();

        HttpResponse<String> response = _httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(400, response.statusCode(), "Returned status code should be 400 Bad Request");
        assertTrue(response.body().contains("Invalid kindOfMeter value provided"));
    }

    @Test
    void getReadingsRuntimeException() throws IOException, InterruptedException, SQLException
    {
        ServiceProvider.Services = mock(InternalServiceProvider.class);
        when(ServiceProvider.Services.getReadingService()).thenThrow(IOException.class);

        String url = new StringBuilder(_url)
                .append("?start=").append(LocalDate.now())
                .append("&kindOfMeter=1").toString();

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .GET()
                .build();

        HttpResponse<String> response = _httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(500, response.statusCode(), "Returned status code should be 5090 Internal Server Error");
        assertTrue(response.body().contains("Internal Server Error"));
    }
}
