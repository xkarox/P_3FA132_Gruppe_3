package server.controller;

import ace.database.DatabaseConnection;
import ace.model.classes.Customer;
import ace.model.interfaces.ICustomer.Gender;
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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

public class CustomerControllerTest
{
    DatabaseConnection _connection;
    String _url = "http://0.0.0.0:8080/customers";
    HttpClient _httpClient;
    Customer _customer;
    ObjectMapper _objMapper;

    Customer getTestCustomer()
    {
        Customer customer = new Customer(null);
        customer.setFirstName("Latten");
        customer.setLastName("Sep");
        customer.setBirthDate(LocalDate.of(1995, 5, 6));
        customer.setGender(Gender.M);
        return customer;
    }

    String formatJson(String jsonString)
    {
        return new StringBuilder("{\"customer\":").append(jsonString).append("}").toString();
    }


    void addCustomer() throws IOException, InterruptedException
    {
        String jsonString = _objMapper.writeValueAsString(this._customer);
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(_url))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(jsonString))
                .build();
        HttpResponse<String> response = _httpClient.send(request, HttpResponse.BodyHandlers.ofString());
    }

    @BeforeEach
    void setUp() throws IOException, SQLException
    {
        Server.startServer(" ");
        _httpClient = HttpClient.newHttpClient();

        if(_connection == null)
        {
            _connection = new DatabaseConnection();
            _connection.openConnection();
            _connection.truncateAllTables();
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
        Server.stopServer();
    }

    @Test
    void addCustomerWithId() throws IOException, InterruptedException
    {
        String jsonString = _objMapper.writeValueAsString(this._customer);
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(_url))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(jsonString))
                .build();
        HttpResponse<String> response = _httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(response.statusCode(), HttpStatus.CREATED.value(), "Should return status code 201 CREATED");
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

        String jsonString = _objMapper.writeValueAsString(customerWithoutId);
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(_url))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(jsonString))
                .build();
        HttpResponse<String> response = _httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        Customer customer = _objMapper.readValue(response.body(), Customer.class);

        assertEquals(response.statusCode(), HttpStatus.CREATED.value(), "Should return status code 201 CREATED");
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
        assertEquals("Invalid customer data provided", body.get("message"), "Message should be Invalid customer data provided");
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
        assertEquals("Invalid customer data provided", body.get("message"), "Message should be Invalid customer data provided");
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

        String jsonString = _objMapper.writeValueAsString(this._customer);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(_url))
                .header("Content-Type", "application/json")
                .PUT(HttpRequest.BodyPublishers.ofString(jsonString))
                .build();

        HttpResponse<String> response = _httpClient.send(request, HttpResponse.BodyHandlers.ofString());
//        TODO get customer from db
        assertEquals(HttpStatus.OK.value(), response.statusCode(), "Status code should be 200 OK");
        assertEquals("Customer successfully updated", response.body(), "Should return a message on success");
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
        assertEquals("Invalid customer data provided", body.get("message"), "Message should be Invalid customer data provided");
    }

    @Test
    void updateCustomerNotFound() throws IOException, InterruptedException
    {
        String jsonString = _objMapper.writeValueAsString(this._customer);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(_url))
                .header("Content-Type", "application/json")
                .PUT(HttpRequest.BodyPublishers.ofString(jsonString))
                .build();

        HttpResponse<String> response = _httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        Map<String, Object> body = _objMapper.readValue(response.body(), new TypeReference<Map<String, Object>>() {});

        assertEquals(HttpStatus.NOT_FOUND.value(), response.statusCode(), "Should return Status Code 404 Not Found");
        assertEquals("Customer not found in database", body.get("message"), "Message should be Customer not found in database");
    }

}
