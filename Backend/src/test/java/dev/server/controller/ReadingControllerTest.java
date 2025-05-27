package dev.server.controller;

import dev.hv.ResponseMessages;
import dev.hv.database.services.AuthorisationService;
import dev.hv.database.services.CustomerService;
import dev.hv.database.services.ReadingService;
import dev.hv.model.interfaces.IId;
import dev.provider.ServiceProvider;
import dev.hv.Utils;
import dev.hv.database.DatabaseConnection;
import dev.hv.database.provider.InternalServiceProvider;
import dev.hv.model.classes.Customer;
import dev.hv.model.classes.Reading;
import dev.hv.model.interfaces.ICustomer;
import dev.hv.model.interfaces.IReading;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import jakarta.ws.rs.core.Response;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.*;
import dev.server.Server;
import org.mockito.MockedStatic;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

public class ReadingControllerTest
{
    DatabaseConnection _connection;
    String _url = "http://0.0.0.0:8080/readings";
    String _customerUrl = "http://0.0.0.0:8080/customers";
    HttpClient _httpClient;
    Customer _customer;
    Reading _reading;
    ObjectMapper _objMapper;
    private static MockedStatic<AuthorisationService> _mockAuthorisationService;


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

    @BeforeAll
    static void beforeAll()
    {
        String restartServer = System.getenv("SkipServerRestart");
        if (Objects.equals(restartServer, "True"))
            Server.startServer("http://localhost:8080/");
        _mockAuthorisationService = mockStatic(AuthorisationService.class);
    }

    @AfterAll
    static void afterAll()
    {
        String restartServer = System.getenv("SkipServerRestart");
        if (Objects.equals(restartServer, "True"))
            Server.stopServer();
        _mockAuthorisationService.close();
    }

    @BeforeEach
    void setUp() throws IOException, SQLException
    {
        String restartServer = System.getenv("SkipServerRestart");
        if (!Objects.equals(restartServer, "True"))
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
        String restartServer = System.getenv("SkipServerRestart");
        if (!Objects.equals(restartServer, "True"))
            Server.stopServer();
        ServiceProvider.Services = new InternalServiceProvider(100, 10, 10);
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

        assertEquals(Response.Status.CREATED.getStatusCode(), response.statusCode(), "Should return status code 201 CREATED");
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

        assertEquals(response.statusCode(), Response.Status.CREATED.getStatusCode(), "Should return status code 201 CREATED");
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

        assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), response.statusCode(), "Status code should be 400 BAD REQUEST");
        assertEquals(ResponseMessages.ControllerBadRequest.toString(), body.get("message"), "Message should be Invalid reading data provided");
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

        assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), response.statusCode(), "Status code should be 400 BAD REQUEST");
        assertEquals(ResponseMessages.ControllerBadRequest.toString(), body.get("message"), "Message should be Invalid reading data provided");
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
        assertEquals(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(), response.statusCode(), "Should return a 400 BAD REQUEST");
        assertEquals(ResponseMessages.ControllerInternalError.toString(), body.get("message"), "Message should be 'Internal Server IOError'");
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
        assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), response.statusCode(), "Should return a 400 BAD REQUEST");
        assertEquals(ResponseMessages.ControllerBadRequest.toString(), body.get("message"), "Message should be 'Invalid reading data provided'");
    }

    @Test
    void updateReading() throws IOException, InterruptedException, ReflectiveOperationException, SQLException
    {
        ServiceProvider.Services = mock(InternalServiceProvider.class);
        ReadingService mockReadingService = mock(ReadingService.class);

        when(mockReadingService.getById(any())).thenReturn(this._reading);
        when(mockReadingService.update(any())).thenReturn(this._reading);

        CustomerService mockCustomerService = mock(CustomerService.class);
        when(mockCustomerService.getById(any())).thenReturn(this._customer);

        when(ServiceProvider.Services.getCustomerService()).thenReturn(mockCustomerService);
        when(ServiceProvider.Services.getReadingService()).thenReturn(mockReadingService);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(_url))
                .header("Content-Type", "application/json")
                .PUT(HttpRequest.BodyPublishers.ofString(Utils.packIntoJsonString(this._reading, Reading.class)))
                .build();

        HttpResponse<String> response = _httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(Response.Status.OK.getStatusCode(), response.statusCode(), "Should return status code 200 OK");
    }

    @Test
    void updateReadingThrowsExceptions() throws SQLException, IOException, ReflectiveOperationException, InterruptedException
    {
        ServiceProvider.Services = mock(InternalServiceProvider.class);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(_url))
                .header("Content-Type", "application/json")
                .PUT(HttpRequest.BodyPublishers.ofString(Utils.packIntoJsonString(this._reading, Reading.class)))
                .build();

        testThrownReadingServiceException(request, IOException.class, Response.Status.INTERNAL_SERVER_ERROR, ResponseMessages.ControllerInternalError);
        testThrownReadingServiceException(request, SQLException.class, Response.Status.BAD_REQUEST, ResponseMessages.ControllerBadRequest);
        testThrownReadingServiceException(request, JsonProcessingException.class, Response.Status.BAD_REQUEST, ResponseMessages.ControllerBadRequest);

        // ReflectionException
        ServiceProvider.Services = mock(InternalServiceProvider.class);
        ReadingService mockReadingService = mock(ReadingService.class);
        when(mockReadingService.getById(any())).thenThrow(ReflectiveOperationException.class);

        when(ServiceProvider.Services.getReadingService()).thenReturn(mockReadingService);

        HttpResponse<String> response2 = _httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), response2.statusCode(), "Should return status code 400 BAD REQUEST");
    }

    @Test
    void updateReadingThrowsExceptionsOnNUll() throws SQLException, IOException, ReflectiveOperationException, InterruptedException
    {
        Reading reading = getTestReading();
        reading.setCustomer(null);

        ServiceProvider.Services = mock(InternalServiceProvider.class);
        ReadingService mockReadingService = mock(ReadingService.class);

        when(mockReadingService.getById(any())).thenReturn(null);
        when(ServiceProvider.Services.getReadingService()).thenReturn(mockReadingService);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(_url))
                .header("Content-Type", "application/json")
                .PUT(HttpRequest.BodyPublishers.ofString(Utils.packIntoJsonString(reading, Reading.class)))
                .build();

        HttpResponse<String> response = _httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(Response.Status.NOT_FOUND.getStatusCode(), response.statusCode(), "Should return status code 28 Not found");
    }

    @Test
    void getReadingById() throws IOException, SQLException, InterruptedException, ReflectiveOperationException
    {
        ServiceProvider.Services = mock(InternalServiceProvider.class);
        ReadingService mockReadingService = mock(ReadingService.class);
        when(mockReadingService.getById(any())).thenReturn(this._reading);
        when(ServiceProvider.Services.getReadingService()).thenReturn(mockReadingService);


        UUID id = UUID.randomUUID();
        String url = _url + "/" + id;

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Content-Type", "application/json")
                .GET()
                .build();

        HttpResponse<String> response = _httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(Response.Status.OK.getStatusCode(), response.statusCode(), "Should return status code 200 OK");

        String readingJson = Utils.unpackFromJsonString(response.body(), Reading.class);
        Reading reading = Utils.getObjectMapper().readValue(readingJson, Reading.class);
        assertEquals(this._reading.getId(), reading.getId(), "Should return the same object");
    }

    @Test
    void getReadingByIdThrowsExceptions() throws SQLException, IOException, ReflectiveOperationException, InterruptedException
    {
        ServiceProvider.Services = mock(InternalServiceProvider.class);

        UUID id = UUID.randomUUID();
        String url = _url + "/" + id;

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Content-Type", "application/json")
                .GET()
                .build();

        testThrownReadingServiceException(request, IOException.class, Response.Status.INTERNAL_SERVER_ERROR, ResponseMessages.ControllerInternalError);
        testThrownReadingServiceException(request, SQLException.class, Response.Status.BAD_REQUEST, ResponseMessages.ControllerBadRequest);

        // ReflectionException
        ServiceProvider.Services = mock(InternalServiceProvider.class);
        ReadingService mockReadingService = mock(ReadingService.class);
        when(mockReadingService.getById(any())).thenThrow(ReflectiveOperationException.class);
        when(ServiceProvider.Services.getReadingService()).thenReturn(mockReadingService);

        HttpResponse<String> response2 = _httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), response2.statusCode(), "Should return status code 400 BAD REQUEST");
    }

    @Test
    void deleteReadingById() throws SQLException, IOException, InterruptedException, ReflectiveOperationException
    {
        ServiceProvider.Services = mock(InternalServiceProvider.class);
        ReadingService mockReadingService = mock(ReadingService.class);

        when(mockReadingService.getById(any())).thenReturn(this._reading);
        doNothing().when(mockReadingService).remove(any());
        when(ServiceProvider.Services.getReadingService()).thenReturn(mockReadingService);

        UUID id = UUID.randomUUID();
        String url = _url + "/" + id;

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Content-Type", "application/json")
                .DELETE()
                .build();

        HttpResponse<String> response = _httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(Response.Status.OK.getStatusCode(), response.statusCode(), "Should return status code 200 OK");

        String readingJson = Utils.unpackFromJsonString(response.body(), Reading.class);
        Reading reading = Utils.getObjectMapper().readValue(readingJson, Reading.class);
        assertEquals(this._reading.getId(), reading.getId(), "Should return the same object");
    }

    @Test
    void deleteReadingByIdThrowsExceptions() throws SQLException, IOException, ReflectiveOperationException, InterruptedException
    {
        ServiceProvider.Services = mock(InternalServiceProvider.class);

        UUID id = UUID.randomUUID();
        String url = _url + "/" + id;

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Content-Type", "application/json")
                .DELETE()
                .build();

        testThrownReadingServiceException(request, IOException.class, Response.Status.INTERNAL_SERVER_ERROR, ResponseMessages.ControllerInternalError);
        testThrownReadingServiceException(request, SQLException.class, Response.Status.BAD_REQUEST, ResponseMessages.ControllerBadRequest);

        // ReflectionException
        ServiceProvider.Services = mock(InternalServiceProvider.class);
        ReadingService mockReadingService = mock(ReadingService.class);
        when(mockReadingService.getById(any())).thenThrow(ReflectiveOperationException.class);
        when(ServiceProvider.Services.getReadingService()).thenReturn(mockReadingService);

        HttpResponse<String> response2 = _httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), response2.statusCode(), "Should return status code 400 BAD REQUEST");
    }

    void testThrownReadingServiceException(HttpRequest request, Class<? extends Exception> exception, Response.Status expectedStatusCode, ResponseMessages responseMessage) throws SQLException, IOException, InterruptedException
    {
        ServiceProvider.Services = mock(InternalServiceProvider.class);
        when(ServiceProvider.Services.getReadingService()).thenThrow(exception);
        HttpResponse<String> response = _httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(expectedStatusCode.getStatusCode(), response.statusCode(), "Should return status code " + expectedStatusCode.getStatusCode());

        Map<String, Object> body = _objMapper.readValue(response.body(), new TypeReference<Map<String, Object>>() {});
        assertEquals(responseMessage.toString(), body.get("message"));
    }
    @Test
    void getReadingsTo() throws IOException, InterruptedException, SQLException, ReflectiveOperationException
    {
        Reading mockReading = mock(Reading.class);
        ReadingService mockRs = mock(ReadingService.class);

        InternalServiceProvider mockedInternalServiceProvider = mock(InternalServiceProvider.class);
        ServiceProvider.Services = mockedInternalServiceProvider;
        when(mockedInternalServiceProvider.getReadingService()).thenReturn(mockRs);
        when(mockRs.getAll()).thenReturn(List.of(mockReading));

        ReadingController rs = new ReadingController();

        assertEquals(Response.Status.OK.getStatusCode(), rs.getReadings(null, null, null, null).getStatus());
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

        String url = _url +
                "?customer=" + customer.getId() +
                "&start=" + startDate +
                "&end=" + endDate +
                "&kindOfMeter=1";

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
        assertEquals(reading.getCustomer().getId(), customer.getId(), "The customer id should match");
        assertTrue(endDate.isAfter(reading.getDateOfReading()) && startDate.isBefore(reading.getDateOfReading()), "The reading date should be between the start and end dates");
    }

    @Test
    void getReadingsStartDateFormatDoesntMatch() throws IOException, InterruptedException
    {
        String startDateWrongFormat = "10000-69-69";
        String url = _url +
                "?start=" + startDateWrongFormat +
                "&kindOfMeter=1";

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .GET()
                .build();

        HttpResponse<String> response = _httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(400, response.statusCode(), "Returned status code should be 400 Bad Request");
        assertTrue(response.body().contains(ResponseMessages.InvalidDateFormatProvided.toString()));
    }

    @Test
    void getReadingsEndDateFormatDoesntMatch() throws IOException, InterruptedException
    {

        String startDateWrongFormat = "10000-69-69";
        String url = _url +
                "?end=" + startDateWrongFormat +
                "&kindOfMeter=1";

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .GET()
                .build();

        HttpResponse<String> response = _httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(400, response.statusCode(), "Returned status code should be 400 Bad Request");
        assertTrue(response.body().contains(ResponseMessages.InvalidDateFormatProvided.toString()));
    }

    @Test
    void getReadingsInvalidKindOfMeter() throws IOException, InterruptedException
    {
        String url = _url +
                "?end=" + LocalDate.of(2024, 12, 13) +
                "&kindOfMeter=69";

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .GET()
                .build();

        HttpResponse<String> response = _httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(400, response.statusCode(), "Returned status code should be 400 Bad Request");
        assertTrue(response.body().contains(ResponseMessages.InvalidKindOfMeterProvided.toString()));
    }

    @Test
    void getReadingsRuntimeException() throws IOException, InterruptedException, SQLException
    {
        ServiceProvider.Services = mock(InternalServiceProvider.class);
        when(ServiceProvider.Services.getReadingService()).thenThrow(IOException.class);

        String url = _url +
                "?start=" + LocalDate.now() +
                "&kindOfMeter=1";

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .GET()
                .build();

        HttpResponse<String> response = _httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(500, response.statusCode(), "Returned status code should be 500 Internal Server Error");
        assertTrue(response.body().contains("Internal Server IOError"));
    }

    @Test
    void getReadingsFileDataJson() throws IOException, InterruptedException
    {
        String url = _url + "/getReadingsFileData?kindOfMeter=WASSER&fileType=json";
        String url1 = _url + "/getReadingsFileData?kindOfMeter=STROM&fileType=json";
        String url2 = _url + "/getReadingsFileData?kindOfMeter=HEIZUNG&fileType=json";

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Content-Type", "application/json")
                .GET()
                .build();

        HttpRequest request1 = HttpRequest.newBuilder()
                .uri(URI.create(url1))
                .header("Content-Type", "application/json")
                .GET()
                .build();


        HttpRequest request2 = HttpRequest.newBuilder()
                .uri(URI.create(url2))
                .header("Content-Type", "application/json")
                .GET()
                .build();


        HttpResponse<String> response = _httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        // assertEquals(Response.Status.OK.getStatusCode(), response.statusCode());

        HttpResponse<String> response1 = _httpClient.send(request1, HttpResponse.BodyHandlers.ofString());
        // assertEquals(Response.Status.OK.getStatusCode(), response1.statusCode());

        HttpResponse<String> response2 = _httpClient.send(request2, HttpResponse.BodyHandlers.ofString());
        // assertEquals(Response.Status.OK.getStatusCode(), response2.statusCode());
    }


    @Test
    void getReadingsFileDataXml() throws IOException, InterruptedException
    {
        String url = _url + "/getReadingsFileData?kindOfMeter=WASSER&fileType=xml";

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Content-Type", "application/json")
                .GET()
                .build();

        HttpResponse<String> response = _httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        // assertEquals(Response.Status.OK.getStatusCode(), response.statusCode());
    }

    @Test
    void getReadingsFileDataCsv() throws IOException, InterruptedException
    {
        String url = _url + "/getReadingsFileData?kindOfMeter=WASSER&fileType=csv";

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Content-Type", "application/json")
                .GET()
                .build();

        HttpResponse<String> response = _httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        // assertEquals(Response.Status.OK.getStatusCode(), response.statusCode());
    }

    @Test
    void getReadingsFileDataBadRequestTest() throws IOException, InterruptedException
    {
        String url = _url + "/getReadingsFileData?kindOfMeter=&fileType=csv";

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Content-Type", "application/json")
                .GET()
                .build();

        HttpResponse<String> response = _httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        // assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), response.statusCode());

        String url2 = _url + "/getReadingsFileData?kindOfMeter=&fileType=";

        HttpRequest request2 = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Content-Type", "application/json")
                .GET()
                .build();

        HttpResponse<String> response2 = _httpClient.send(request2, HttpResponse.BodyHandlers.ofString());
        // assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), response2.statusCode());
    }

    @Test
    void getReadingsFileDataInternalServerErrorTest() throws IOException, InterruptedException
    {
        String url = _url + "/getReadingsFileData?kindOfMeter=WASSER&fileType=exe";

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Content-Type", "application/json")
                .GET()
                .build();

        HttpResponse<String> response = _httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        // assertEquals(Response.Status.UNSUPPORTED_MEDIA_TYPE.getStatusCode(), response.statusCode());
    }

    @Test
    void uploadReadingFileJsonTest() throws IOException, InterruptedException
    {
        String url = _url + "/upload";
        String jsonInput = "{\n" +
                "  \"readings\": [\n" +
                "    {\n" +
                "      \"id\": \"b3bec254-dd57-47bc-85b0-01d84a85f509\",\n" +
                "      \"comment\": null,\n" +
                "      \"customer\": {\n" +
                "        \"id\": \"ec617965-88b4-4721-8158-ee36c38e4db3\",\n" +
                "        \"firstName\": \"Pumukel\",\n" +
                "        \"lastName\": \"Kobold\",\n" +
                "        \"birthDate\": null,\n" +
                "        \"gender\": \"M\"\n" +
                "      },\n" +
                "      \"dateOfReading\": \"2019-02-01\",\n" +
                "      \"kindOfMeter\": \"STROM\",\n" +
                "      \"meterCount\": 19471,\n" +
                "      \"meterId\": \"MST-af34569\",\n" +
                "      \"substitute\": false\n" +
                "    }\n" +
                "  ]\n" +
                "}";

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(jsonInput))
                .build();

        HttpResponse<String> response = _httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        // assertEquals(Response.Status.OK.getStatusCode(), response.statusCode());
    }

    @Test
    void uploadReadingFileXmlTest() throws IOException, InterruptedException
    {
        String url = _url + "/upload";
        String xmlInput = "<ReadingWrapper>\n" +
                "<Readings>\n" +
                "<Id>b3bec254-dd57-47bc-85b0-01d84a85f509</Id>\n" +
                "<Customer>\n" +
                "<Id>ec617965-88b4-4721-8158-ee36c38e4db3</Id>\n" +
                "<FirstName>Pumukel</FirstName>\n" +
                "<LastName>Kobold</LastName>\n" +
                "<Gender>M</Gender>\n" +
                "</Customer>\n" +
                "<DateOfReading>2019-02-01</DateOfReading>\n" +
                "<KindOfMeter>STROM</KindOfMeter>\n" +
                "<MeterCount>19471.0</MeterCount>\n" +
                "<MeterId>MST-af34569</MeterId>\n" +
                "<Substitute>false</Substitute>\n" +
                "</Readings>\n" +
                "</ReadingWrapper>";

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Content-Type", "application/xml")
                .POST(HttpRequest.BodyPublishers.ofString(xmlInput))
                .build();

        HttpResponse<String> response = _httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        // assertEquals(Response.Status.OK.getStatusCode(), response.statusCode());
    }

    @Test
    void uploadReadingFileCsvTest() throws IOException, InterruptedException
    {
        String url = _url + "/upload";
        String csvInput = "Datum;Zählerstand;Kommentar;KundenId;Zählerart;ZählerstandId;Ersatz\n" +
                "01.10.2020;63.0;;ec617965-88b4-4721-8158-ee36c38e4db3;WASSER;786523123;false;\n";

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Content-Type", "text/plain")
                .POST(HttpRequest.BodyPublishers.ofString(csvInput))
                .build();

        HttpResponse<String> response = _httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        // assertEquals(Response.Status.OK.getStatusCode(), response.statusCode());
    }

    @Test
    void uploadReadingFileBadRequestJsonTest() throws IOException, InterruptedException
    {
        String url = _url + "/upload";

        String invalidJson = "{\"reading\": []}";

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(invalidJson))
                .build();

        HttpResponse<String> response = _httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        // assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), response.statusCode());
    }

    @Test
    void exportReadingsXmlTest() throws IOException, InterruptedException
    {
        String url = _url + "/exportReadings?kindOfMeter=WASSER&fileType=xml";

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Content-Type", "application/json")
                .GET()
                .build();

        HttpResponse<String> response = _httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(Response.Status.OK.getStatusCode(), response.statusCode());
    }

    @Test
    void exportReadingsJsonTest() throws IOException, InterruptedException
    {
        String url = _url + "/exportReadings?kindOfMeter=WASSER&fileType=json";

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Content-Type", "application/json")
                .GET()
                .build();

        HttpResponse<String> response = _httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(Response.Status.OK.getStatusCode(), response.statusCode());
    }

    @Test
    void exportReadingsCsvTest() throws IOException, InterruptedException
    {
        String url = _url + "/exportReadings?kindOfMeter=WASSER&fileType=csv";

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Content-Type", "application/json")
                .GET()
                .build();

        HttpResponse<String> response = _httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(Response.Status.OK.getStatusCode(), response.statusCode());
    }

    @Test
    void exportReadingsUnsupportedMediaTypeTest() throws IOException, InterruptedException
    {
        {
            String url = _url + "/exportReadings?kindOfMeter=WASSER&fileType=undefined";

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("Content-Type", "application/json")
                    .GET()
                    .build();

            HttpResponse<String> response = _httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            assertEquals(Response.Status.UNSUPPORTED_MEDIA_TYPE.getStatusCode(), response.statusCode());
        }
    }

    @Test
    void exportReadingsFileTypeNullBadRequestTest() throws IOException, InterruptedException
    {
        {
            String url = _url + "/exportReadings?kindOfMeter=WASSER&fileType=";

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("Content-Type", "application/json")
                    .GET()
                    .build();

            HttpResponse<String> response = _httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), response.statusCode());
        }
    }

    @Test
    void exportReadingsKindOfMeterNullBadRequestTest() throws IOException, InterruptedException
    {
        {
            String url = _url + "/exportReadings?kindOfMeter=&fileType=json";

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("Content-Type", "application/json")
                    .GET()
                    .build();

            HttpResponse<String> response = _httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), response.statusCode());
        }
    }

    @Test
    void validateReadingsJsonTest() throws IOException, InterruptedException
    {
        String url = _url + "/validateReadings";

        String validJson = "{\n" +
                "      \"readings\": [\n" +
                "        {\n" +
                "          \"id\": \"bc3ab87a-4748-4e34-bdc7-097116fdcc43\",\n" +
                "          \"comment\": null,\n" +
                "          \"customerId\": \"ec617965-88b4-4721-8158-ee36c38e4db3\",\n" +
                "          \"customer\": {\n" +
                "            \"id\": \"ec617965-88b4-4721-8158-ee36c38e4db3\",\n" +
                "            \"firstName\": \"Pumukel\",\n" +
                "            \"lastName\": \"Kobold\",\n" +
                "            \"birthDate\": \"1962-02-21\",\n" +
                "            \"gender\": \"M\"\n" +
                "          },\n" +
                "          \"dateOfReading\": \"2020-10-01\",\n" +
                "          \"kindOfMeter\": \"WASSER\",\n" +
                "          \"meterCount\": 63,\n" +
                "          \"meterId\": \"786523123\",\n" +
                "          \"substitute\": false\n" +
                "        }\n" +
                "      ]\n" +
                "    }";

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(validJson))
                .build();

        HttpResponse<String> response = _httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(Response.Status.OK.getStatusCode(), response.statusCode());
    }

    @Test
    void validateReadingsCsvTest() throws IOException, InterruptedException
    {
        String url = _url + "/validateReadings";

        String validCsv = "Datum;Zählerstand;Kommentar;KundenId;Zählerart;ZählerstandId;Ersatz\n" +
                "01.10.2020;63.0;;ec617965-88b4-4721-8158-ee36c38e4db3;WASSER;786523123;false;\n";

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Content-Type", "text/plain")
                .POST(HttpRequest.BodyPublishers.ofString(validCsv))
                .build();

        HttpResponse<String> response = _httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(Response.Status.OK.getStatusCode(), response.statusCode());
    }

    @Test
    void validateReadingsXmlTest() throws IOException, InterruptedException
    {
        String url = _url + "/validateReadings";

        String validXml = "<ReadingWrapper>\n" +
                "<Readings>\n" +
                "<Id>bc3ab87a-4748-4e34-bdc7-097116fdcc43</Id>\n" +
                "<Customer>\n" +
                "<Id>ec617965-88b4-4721-8158-ee36c38e4db3</Id>\n" +
                "<FirstName>Pumukel</FirstName>\n" +
                "<LastName>Kobold</LastName>\n" +
                "<BirthDate>1962-02-21</BirthDate>\n" +
                "<Gender>M</Gender>\n" +
                "</Customer>\n" +
                "<DateOfReading>2020-10-01</DateOfReading>\n" +
                "<KindOfMeter>WASSER</KindOfMeter>\n" +
                "<MeterCount>63.0</MeterCount>\n" +
                "<MeterId>786523123</MeterId>\n" +
                "<Substitute>false</Substitute>\n" +
                "</Readings>\n" +
                "</ReadingWrapper>";

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Content-Type", "application/xml")
                .POST(HttpRequest.BodyPublishers.ofString(validXml))
                .build();

        HttpResponse<String> response = _httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(Response.Status.OK.getStatusCode(), response.statusCode());
    }

    @Test
    void validateReadingsMissingContentTypeTest() throws IOException, InterruptedException
    {
        String url = _url + "/validateReadings";

        String validCsv = "UUID,Anrede,Vorname,Nachname,Geburtsdatum\n" +
                "ec617965-88b4-4721-8158-ee36c38e4db3,Frau,Anna,Test,21.02.1962\n";

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Content-Type", "")
                .POST(HttpRequest.BodyPublishers.ofString(validCsv))
                .build();

        HttpResponse<String> response = _httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), response.statusCode());
    }

    @Test
    void validateReadingsMissingBodyTypeTest() throws IOException, InterruptedException
    {
        String url = _url + "/validateReadings";

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Content-Type", "text/plain")
                .POST(HttpRequest.BodyPublishers.ofString(""))
                .build();

        HttpResponse<String> response = _httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), response.statusCode());
    }

    @Test
    void validateReadingsMissingBodyTypeAndContentTypeTest() throws IOException, InterruptedException
    {
        String url = _url + "/validateReadings";

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Content-Type", "")
                .POST(HttpRequest.BodyPublishers.ofString(""))
                .build();

        HttpResponse<String> response = _httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), response.statusCode());
    }

    @Test
    void auth() throws IOException, InterruptedException, ReflectiveOperationException, SQLException
    {
        Reading mockReading = mock(Reading.class);
        ReadingService mockRs = mock(ReadingService.class);

        InternalServiceProvider mockedInternalServiceProvider = mock(InternalServiceProvider.class);
        ServiceProvider.Services = mockedInternalServiceProvider;
        when(mockedInternalServiceProvider.getReadingService()).thenReturn(mockRs);
        when(mockRs.getById(any())).thenReturn(mockReading);

        _mockAuthorisationService.when(AuthorisationService::IsUserAdmin).thenReturn(false);
        _mockAuthorisationService.when(() -> AuthorisationService.CanUserAccessResource(any())).thenReturn(false);

        String jsonString = Utils.packIntoJsonString(this._reading, Reading.class);

        ReadingController rs = new ReadingController();

        assertUnauthorized(rs.addReading(jsonString));
        assertUnauthorized(rs.updateReading(jsonString));
        assertUnauthorized(rs.getReading(UUID.randomUUID()));
        assertUnauthorized(rs.deleteReading(UUID.randomUUID()));
        assertUnauthorized(rs.getReadings(null, null, null, null));
    }

    private void assertUnauthorized(Response response)
    {
        assertEquals(Response.Status.UNAUTHORIZED.getStatusCode(), response.getStatus());
    }

    @Test
    void addBatch() throws IOException, SQLException
    {
        ReadingController cc = new ReadingController();
        InternalServiceProvider mockedInternalServiceProvider = mock(InternalServiceProvider.class);
        ReadingService mockedCs = mock(ReadingService.class);
        ServiceProvider.Services = mockedInternalServiceProvider;
        _mockAuthorisationService.when(AuthorisationService::IsUserAdmin).thenReturn(true);

        when(mockedInternalServiceProvider.getReadingService()).thenReturn(mockedCs);
        doNothing().when(mockedCs).addBatch(anyList());

        assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), cc.addReadingBatch(null).getStatus());
        assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), cc.addReadingBatch("").getStatus());

        String jsonString = "[\n" +
                "    {\n" +
                "    \"id\": null,\n" +
                "    \"comment\": \"Level is over 9k >.>\",\n" +
                "    \"customer\": {\n" +
                "        \"id\":\"33829434-4f9b-41e2-a58c-75e88d4e0b9b\",\n" +
                "        \"firstName\":\"Latten\",\n" +
                "        \"lastName\":\"Sep\",\n" +
                "        \"birthDate\":\"1995-05-06\",\n" +
                "        \"gender\":\"M\"\n" +
                "        },\n" +
                "    \"dateOfReading\": \"2024-01-04\",\n" +
                "    \"kindOfMeter\": \"WASSER\",\n" +
                "    \"meterCount\": 625197.7,\n" +
                "    \"meterId\": \"X1D3-ABCD\",\n" +
                "    \"substitute\": false\n" +
                "    }\n" +
                "]";
        assertEquals(Response.Status.CREATED.getStatusCode(), cc.addReadingBatch(jsonString).getStatus());
        doThrow(new SQLException("SQL Error")).when(mockedCs).addBatch(anyList());
        assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), cc.addReadingBatch(jsonString).getStatus());
    }
}
