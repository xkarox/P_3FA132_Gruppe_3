package dev.server.controller;

import dev.hv.ResponseMessages;
import dev.provider.ServiceProvider;
import dev.hv.Utils;
import dev.hv.database.DatabaseConnection;
import dev.hv.database.provider.InternalServiceProvider;
import dev.hv.model.classes.Customer;
import dev.hv.model.ICustomer.Gender;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import dev.server.Server;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.Map;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class CustomerControllerTest
{
    private static final Logger log = LoggerFactory.getLogger(CustomerControllerTest.class);
    private DatabaseConnection _connection;
    private String _url = "http://0.0.0.0:8080/customers";
    private HttpClient _httpClient;
    private Customer _customer;
    private ObjectMapper _objMapper;


    Customer getTestCustomer()
    {
        Customer customer = new Customer(null);
        customer.setFirstName("Latten");
        customer.setLastName("Sep");
        customer.setBirthDate(LocalDate.of(1995, 5, 6));
        customer.setGender(Gender.M);
        return customer;
    }

    void addCustomer() throws IOException, InterruptedException
    {
        String jsonString = Utils.packIntoJsonString(this._customer, Customer.class);
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(_url))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(jsonString))
                .build();
        HttpResponse<String> response = _httpClient.send(request, HttpResponse.BodyHandlers.ofString());
    }

    @BeforeAll
    static void beforeAll()
    {
        String restartServer = System.getenv("SkipServerRestart");
        if (Objects.equals(restartServer, "True"))
            Server.startServer(" ");
    }

    @AfterAll
    static void afterAl()
    {
        String restartServer = System.getenv("SkipServerRestart");
        if (Objects.equals(restartServer, "True"))
            Server.stopServer();
    }


    @BeforeEach
    void setUp() throws IOException, SQLException
    {
        String restartServer = System.getenv("SkipServerRestart");
        if (!Objects.equals(restartServer, "True"))
            Server.startServer(" ");

        _httpClient = HttpClient.newHttpClient();

        if(_connection == null)
        {
            _connection = new DatabaseConnection();
            _connection.openConnection();
            _connection.removeAllTables();
            _connection.createAllTables();
        }
        else
        {
            _connection.truncateAllTables();
        }

        _objMapper = new ObjectMapper();
        _objMapper.registerModule(new JavaTimeModule());
        _objMapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
        _objMapper.setDateFormat(new SimpleDateFormat("yyyy-MM-dd"));

        _customer = this.getTestCustomer();
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
    void addCustomerWithId() throws IOException, InterruptedException, SQLException
    {
        String jsonString = Utils.packIntoJsonString(this._customer, Customer.class);
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(_url))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(jsonString))
                .build();
        HttpResponse<String> response = _httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(HttpStatus.CREATED.value(), response.statusCode(),"Should return status code 201 CREATED");
        assertEquals(jsonString, response.body(), "Should return the same object send in request");
    }

    @Test
    void addCustomerWithoutId()  throws IOException, InterruptedException
    {
        Customer customerWithoutId = new Customer();
        customerWithoutId.setLastName("Ruehl");
        customerWithoutId.setFirstName("Markus");
        customerWithoutId.setBirthDate(LocalDate.of(1972,2,22));
        customerWithoutId.setGender(Gender.M);

        String jsonString = Utils.packIntoJsonString(customerWithoutId, Customer.class);
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(_url))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(jsonString))
                .build();
        HttpResponse<String> response = _httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        Customer customer = _objMapper.readValue(Utils.unpackFromJsonString(response.body(), Customer.class), Customer.class);

        assertEquals(HttpStatus.CREATED.value(), response.statusCode(), "Should return status code 201 CREATED");
        assertEquals(customerWithoutId.getFirstName(), customer.getFirstName(), "First name should match");
        assertEquals(customerWithoutId.getLastName(), customer.getLastName(), "Last name should match");
        assertEquals(customerWithoutId.getBirthDate(), customer.getBirthDate(), "Birth date should match");
        assertEquals(customerWithoutId.getGender(), customer.getGender(), "Gender should match");
        assertNotEquals(customerWithoutId.getId(), customer.getId(), "ID should be different");
    }

    @Test
    void addCustomerInvalidObject() throws IOException, InterruptedException
    {
        String jsonString = "\"id\":\"123123123\", \"kennzeichen\":\"STA-GM405\"";
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(_url))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(jsonString))
                .build();
        HttpResponse<String> response = _httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        Map<String, Object> body = _objMapper.readValue(response.body(), new TypeReference<Map<String, Object>>() {});

        assertEquals(response.statusCode(), HttpStatus.BAD_REQUEST.value(), "Should return a 400 BAD REQUEST");
        assertEquals(ResponseMessages.ControllerBadRequest.toString(), body.get("message"), "Message should be Invalid customer data provided");
    }

    @Test
    void addCustomerEmptyBody() throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(_url))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString("{}"))
                .build();
        HttpResponse<String> response = _httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        Map<String, Object> body = _objMapper.readValue(response.body(), new TypeReference<Map<String, Object>>() {});

        assertEquals(HttpStatus.BAD_REQUEST.value(), response.statusCode(), "Should return a 400 BAD REQUEST");
        assertEquals(ResponseMessages.ControllerBadRequest.toString(), body.get("message"), "Message should be Invalid customer data provided");
    }

    @Test
    void addCustomerIOException() throws Exception
    {
        ServiceProvider.Services =  mock(InternalServiceProvider.class);
        when(ServiceProvider.Services.getCustomerService()).thenThrow(IOException.class);

        String jsonString = Utils.packIntoJsonString(this._customer, Customer.class);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(_url))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(jsonString))
                .build();
        HttpResponse<String> response = _httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        Map<String, Object> body = _objMapper.readValue(response.body(), new TypeReference<Map<String, Object>>() {});
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR.value(), response.statusCode(), "Should return a 500 Internal Server Error");
        assertEquals(ResponseMessages.ControllerInternalError.toString(), body.get("message"), "Message should be 'Internal Server IOError'");
    }

    @Test
    void addCustomerJsonProcessingException() throws SQLException, IOException, InterruptedException, NoSuchFieldException, IllegalAccessException
    {
        String jsonString = Utils.packIntoJsonString(this._customer, Customer.class);

        ServiceProvider.Services = mock(InternalServiceProvider.class);
        when(ServiceProvider.Services.getCustomerService()).thenThrow(JsonProcessingException.class);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(_url))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(jsonString))
                .build();
        HttpResponse<String> response = _httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        Map<String, Object> body = _objMapper.readValue(response.body(), new TypeReference<Map<String, Object>>() {});
        assertEquals(HttpStatus.BAD_REQUEST.value(), response.statusCode(), "Should return a 400 BAD REQUEST");
        assertEquals(ResponseMessages.ControllerBadRequest.toString(), body.get("message"), "Message should be 'Invalid customer data provided'");
    }

    @Test
    void updateCustomer() throws IOException, InterruptedException
    {
        this.addCustomer();

        String newFirstName = "Donald";
        String newLastName = "Trump";
        LocalDate newBirthday = LocalDate.of(1977, 11, 2);
        Gender newGender = Gender.D;

        this._customer.setGender(newGender);
        this._customer.setFirstName(newFirstName);
        this._customer.setLastName(newLastName);
        this._customer.setBirthDate(newBirthday);

        String jsonString = Utils.packIntoJsonString(this._customer, Customer.class);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(_url))
                .header("Content-Type", "application/json")
                .PUT(HttpRequest.BodyPublishers.ofString(jsonString))
                .build();

        HttpResponse<String> response = _httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(HttpStatus.OK.value(), response.statusCode(), "Status code should be 200 OK");
        assertEquals(ResponseMessages.ControllerUpdateSuccess.toString(), response.body(), "Should return a message on success");
    }

    @Test
    void updateCustomerBadRequest() throws IOException, InterruptedException
    {
        String jsonString = "{sl;fjk;lsdkf}";

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(_url))
                .header("Content-Type", "application/json")
                .PUT(HttpRequest.BodyPublishers.ofString(jsonString))
                .build();

        HttpResponse<String> response = _httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        Map<String, Object> body = _objMapper.readValue(response.body(), new TypeReference<Map<String, Object>>() {});

        assertEquals(HttpStatus.BAD_REQUEST.value(), response.statusCode(), "Should be Status Code 400 Bad Request");
        assertEquals(ResponseMessages.ControllerBadRequest.toString(), body.get("message"), "Message should be Invalid customer data provided");
    }

    @Test
    void updateCustomerNotFound() throws IOException, InterruptedException
    {
        String jsonString = Utils.packIntoJsonString(this._customer, Customer.class);
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(_url))
                .header("Content-Type", "application/json")
                .PUT(HttpRequest.BodyPublishers.ofString(jsonString))
                .build();

        HttpResponse<String> response = _httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        Map<String, Object> body = _objMapper.readValue(response.body(), new TypeReference<Map<String, Object>>() {});

        assertEquals(HttpStatus.NOT_FOUND.value(), response.statusCode(), "Should return Status Code 404 Not Found");
        assertEquals(ResponseMessages.ControllerNotFound.toString(), body.get("message"), "Message should be Customer not found in database");
    }

    @Test
    void updateCustomerIOException() throws Exception
    {
        this.addCustomer();

        ServiceProvider.Services = mock(InternalServiceProvider.class);
        when(ServiceProvider.Services.getCustomerService()).thenThrow(IOException.class);

        String jsonString = Utils.packIntoJsonString(this._customer, Customer.class);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(_url))
                .header("Content-Type", "application/json")
                .PUT(HttpRequest.BodyPublishers.ofString(jsonString))
                .build();
        HttpResponse<String> response = _httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        Map<String, Object> body = _objMapper.readValue(response.body(), new TypeReference<Map<String, Object>>() {});
        assertEquals(HttpStatus.BAD_REQUEST.value(), response.statusCode(), "Should return a 400 BAD REQUEST");
        assertEquals(ResponseMessages.ControllerInternalError.toString(), body.get("message"), "Message should be 'Internal Server IOError'");
    }

    @Test
    void updateCustomerJsonProcessingException() throws SQLException, IOException, InterruptedException, NoSuchFieldException, IllegalAccessException
    {
        this.addCustomer();

        String jsonString = Utils.packIntoJsonString(this._customer, Customer.class);
        ServiceProvider.Services = mock(InternalServiceProvider.class);
        when(ServiceProvider.Services.getCustomerService()).thenThrow(JsonProcessingException.class);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(_url))
                .header("Content-Type", "application/json")
                .PUT(HttpRequest.BodyPublishers.ofString(jsonString))
                .build();

        HttpResponse<String> response = _httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        Map<String, Object> body = _objMapper.readValue(response.body(), new TypeReference<Map<String, Object>>() {});
        assertEquals(HttpStatus.BAD_REQUEST.value(), response.statusCode(), "Should return a 400 BAD REQUEST");
        assertEquals(ResponseMessages.ControllerBadRequest.toString(), body.get("message"), "Message should be 'Invalid customer data provided'");
    }
}
