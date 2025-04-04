package dev.server.controller;

import com.fasterxml.jackson.databind.JsonNode;
import dev.hv.ResponseMessages;
import dev.hv.database.DbHelperService;
import dev.hv.database.DbTestHelper;
import dev.hv.database.services.AuthorisationService;
import dev.hv.database.services.CustomerService;
import dev.hv.database.services.ReadingService;
import dev.hv.model.interfaces.IId;
import dev.hv.model.interfaces.IReading;
import dev.hv.model.classes.Reading;
import dev.provider.ServiceProvider;
import dev.hv.Utils;
import dev.hv.database.DatabaseConnection;
import dev.hv.database.provider.InternalServiceProvider;
import dev.hv.model.classes.Customer;
import dev.hv.model.interfaces.ICustomer.Gender;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import dev.server.validator.CustomerWithReadingsJsonSchemaValidatorService;
import jakarta.ws.rs.core.Response;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import dev.server.Server;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.security.Provider;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.Map;
import java.util.Objects;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class CustomerControllerTest
{
    private static final Logger log = LoggerFactory.getLogger(CustomerControllerTest.class);
    private DatabaseConnection _connection;
    private final String _url = "http://0.0.0.0:8080/customers";
    private HttpClient _httpClient;
    private Customer _customer;
    private ObjectMapper _objMapper;
    private static MockedStatic<AuthorisationService> _mockAuthorisationService;


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
        _mockAuthorisationService = mockStatic(AuthorisationService.class);
    }

    @AfterAll
    static void afterAll() throws IOException
    {
        ServiceProvider.Services.dbConnectionPropertiesOverwrite(DbHelperService.loadProperties(DbTestHelper.loadTestDbProperties()));
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
        ServiceProvider.Services = new InternalServiceProvider(100, 100, 10);
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


        assertEquals(Response.Status.CREATED.getStatusCode(), response.statusCode(),"Should return status code 201 CREATED");
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

        assertEquals(Response.Status.CREATED.getStatusCode(), response.statusCode(), "Should return status code 201 CREATED");
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
        Map<String, String> body = _objMapper.readValue(response.body(), new TypeReference<Map<String, String>>() {});

        assertEquals(response.statusCode(), Response.Status.BAD_REQUEST.getStatusCode(), "Should return a 400 BAD REQUEST");
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

        assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), response.statusCode(), "Should return a 400 BAD REQUEST");
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
        assertEquals(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(), response.statusCode(), "Should return a 500 Internal Server Error");
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
        assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), response.statusCode(), "Should return a 400 BAD REQUEST");
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

        assertEquals(Response.Status.OK.getStatusCode(), response.statusCode(), "Status code should be 200 OK");
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

        assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), response.statusCode(), "Should be Status Code 400 Bad Request");
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

        assertEquals(Response.Status.NOT_FOUND.getStatusCode(), response.statusCode(), "Should return Status Code 404 Not Found");
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
        assertEquals(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(), response.statusCode(), "Should return a 500 Internal Server Error");
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
        Map<String, String> body = _objMapper.readValue(response.body(), new TypeReference<Map<String, String>>() {});
        assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), response.statusCode(), "Should return a 400 BAD REQUEST");
        assertEquals(ResponseMessages.ControllerBadRequest.toString(), body.get("message"), "Message should be 'Invalid customer data provided'");
    }

    @Test
    void getAllCustomersTest() throws ReflectiveOperationException, SQLException, IOException, InterruptedException
    {
        ServiceProvider.Services = mock(InternalServiceProvider.class);
        CustomerService cs = mock(CustomerService.class);
        when(ServiceProvider.Services.getCustomerService()).thenReturn(cs);

        Customer customer1 = new Customer();
        customer1.setGender(Gender.M);
        customer1.setFirstName("Elon");
        customer1.setLastName("Musk");
        customer1.setBirthDate(LocalDate.of(1986, 4, 6));

        Customer customer2 = new Customer();
        customer2.setGender(Gender.W);
        customer2.setFirstName("Angela");
        customer2.setLastName("Merkel");
        customer2.setBirthDate(LocalDate.of(1976, 9, 22));

        List<Customer> mockCustomers = Arrays.asList(customer1, customer2);

        when(cs.getAll()).thenReturn(mockCustomers);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(_url))
                .header("Content-Type", "application/json")
                .GET()
                .build();

        HttpResponse<String> response = _httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(Response.Status.OK.getStatusCode(), response.statusCode(), "Should return status code 200 OK");

        Collection<? extends IId> unpackedCustomer = Utils.unpackCollectionFromJsonString(response.body(), Customer.class);

        Customer customer = (Customer)unpackedCustomer.toArray()[0];
        assertEquals(2, unpackedCustomer.size(), "The response should contain 2 elements");
        assertTrue(unpackedCustomer.contains(customer1), "Customer is not contained");
        assertEquals(Gender.M, customer1.getGender(), "Gender should be male");
        assertEquals("Elon", customer1.getFirstName(), "First name should be 'Elon'");
        assertEquals("Musk", customer1.getLastName(), "Last name should be 'Musk'");
        assertEquals(LocalDate.of(1986, 4, 6), customer1.getBirthDate(), "Birthdate should be 06.04.1986");
    }

    @Test
    void getAllCustomersThrowsException() throws SQLException, IOException, InterruptedException, ReflectiveOperationException
    {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(_url))
                .header("Content-Type", "application/json")
                .GET()
                .build();

        testThrownCustomerServiceException(request, IOException.class, Response.Status.INTERNAL_SERVER_ERROR.getStatusCode());
        testThrownCustomerServiceException(request, SQLException.class, Response.Status.INTERNAL_SERVER_ERROR.getStatusCode());

        //Reflection Exception
        ServiceProvider.Services = mock(InternalServiceProvider.class);
        CustomerService mockCustomerService = mock(CustomerService.class);

        when(mockCustomerService.getAll()).thenThrow(ReflectiveOperationException.class);
        when(ServiceProvider.Services.getCustomerService()).thenReturn(mockCustomerService);

        HttpResponse<String> response = _httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(), response.statusCode(), "Should return status code 500 internal server error");

    }

    @Test
    void getCustomerByIdTest() throws ReflectiveOperationException, SQLException, IOException, InterruptedException
    {
        ServiceProvider.Services = mock(InternalServiceProvider.class);
        CustomerService mockCustomerService = mock(CustomerService.class);
        when(mockCustomerService.getById(any())).thenReturn(this._customer);
        when(ServiceProvider.Services.getCustomerService()).thenReturn(mockCustomerService);

        UUID id = UUID.randomUUID();
        String url = _url + "/" + id;

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Content-Type", "application/json")
                .GET()
                .build();

        HttpResponse<String> response = _httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(Response.Status.OK.getStatusCode(), response.statusCode(), "Should return status code 200 OK");

        String customerJson = Utils.unpackFromJsonString(response.body(), Customer.class);
        Customer customer = Utils.getObjectMapper().readValue(customerJson, Customer.class);
        assertEquals(this._customer.getId(), customer.getId(), "Should return the same object");
    }

    @Test
    void getCustomerByIdThrowsException() throws SQLException, IOException, InterruptedException, ReflectiveOperationException
    {

        UUID id = UUID.randomUUID();
        String url = _url + "/" + id;

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Content-Type", "application/json")
                .GET()
                .build();

        testThrownCustomerServiceException(request, IOException.class, Response.Status.INTERNAL_SERVER_ERROR.getStatusCode());
        testThrownCustomerServiceException(request, SQLException.class, Response.Status.INTERNAL_SERVER_ERROR.getStatusCode());

        //Reflection Exception
        ServiceProvider.Services = mock(InternalServiceProvider.class);
        CustomerService mockCustomerService = mock(CustomerService.class);

        when(mockCustomerService.getById(any())).thenThrow(ReflectiveOperationException.class);
        when(ServiceProvider.Services.getCustomerService()).thenReturn(mockCustomerService);

        HttpResponse<String> response = _httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(), response.statusCode(), "Should return status code 500 internal server error");

    }

    void testThrownCustomerServiceException(HttpRequest request, Class<? extends Exception> exception, int expectedStatusCode) throws SQLException, IOException, InterruptedException
    {
        ServiceProvider.Services = mock(InternalServiceProvider.class);
        when(ServiceProvider.Services.getCustomerService()).thenThrow(exception);
        HttpResponse<String> response = _httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(expectedStatusCode, response.statusCode(), "Should return status code " + expectedStatusCode);
    }

    @Test
    void deleteCustomerByIdTest() throws ReflectiveOperationException, SQLException, IOException, InterruptedException
    {
        Reading reading1 = new Reading();
        reading1.setId(UUID.randomUUID());
        reading1.setMeterId("456");
        reading1.setCustomer(this._customer);
        reading1.setSubstitute(true);
        reading1.setDateOfReading(LocalDate.now());
        reading1.setKindOfMeter(IReading.KindOfMeter.STROM);
        reading1.setMeterCount(100);
        reading1.setComment("comment");

        Reading reading2 = new Reading();
        reading2.setId(UUID.randomUUID());
        reading2.setMeterId("123");
        reading2.setCustomer(this._customer);
        reading2.setSubstitute(true);
        reading2.setDateOfReading(LocalDate.now());
        reading2.setKindOfMeter(IReading.KindOfMeter.WASSER);
        reading2.setMeterCount(9999);
        reading2.setComment("comment2");

        List<Reading> readings = Arrays.asList(reading1, reading2);

        ServiceProvider.Services = mock(InternalServiceProvider.class);
        CustomerService mockCustomerService = mock(CustomerService.class);
        ReadingService mockReadingService = mock(ReadingService.class);
        when(ServiceProvider.Services.getCustomerService()).thenReturn(mockCustomerService);
        when(ServiceProvider.Services.getReadingService()).thenReturn(mockReadingService);
        when(mockCustomerService.getById(any())).thenReturn(this._customer);
        when(mockReadingService.getReadingsByCustomerId(any())).thenReturn(readings);
        doAnswer(invocation -> {
            reading1.setCustomer(null);
            reading2.setCustomer(null);
            return null;
        }).when(mockCustomerService).remove(any());


        UUID id = UUID.randomUUID();
        String url = _url + "/" + id;

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Content-Type", "application/json")
                .DELETE()
                .build();

        HttpResponse<String> response = _httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(Response.Status.OK.getStatusCode(), response.statusCode(), "Should return code 200 OK");

        boolean invalidCustomerWithReadings = CustomerWithReadingsJsonSchemaValidatorService.getInstance().validate(response.body());
        assertFalse(invalidCustomerWithReadings, "customer with readings is not valid");

        ObjectMapper objectMapper = Utils.getObjectMapper();
        JsonNode rootNode = objectMapper.readTree(response.body());

        assertTrue(rootNode.has("customer"), "JSON should contain 'customer'");
        assertTrue(rootNode.has("readings"), "JSON should contain 'readings'");

        JsonNode customerNode = rootNode.get("customer");
        assertEquals(this._customer.getId().toString(), customerNode.get("id").asText(), "Customer ID should match");
        assertEquals(this._customer.getFirstName(), customerNode.get("firstName").asText(), "Customer firstName should match");
        assertEquals(this._customer.getLastName(), customerNode.get("lastName").asText(), "Customer lastName should match");

        JsonNode readingsNode = rootNode.get("readings");
        assertTrue(readingsNode.isArray(), "Readings should be an array");
        assertEquals(2, readingsNode.size(), "Should have 2 readings");

        JsonNode reading1Node = readingsNode.get(0);
        assertEquals(reading1.getKindOfMeter().toString(), reading1Node.get("kindOfMeter").asText(), "First reading kindOfMeter should match");
        assertEquals(reading1.getMeterCount(), reading1Node.get("meterCount").asInt(), "First reading meterCount should match");

        JsonNode reading2Node = readingsNode.get(1);
        assertEquals(reading2.getKindOfMeter().toString(), reading2Node.get("kindOfMeter").asText(), "Second reading kindOfMeter should match");
        assertEquals(reading2.getMeterCount(), reading2Node.get("meterCount").asInt(), "Second reading meterCount should match");
    }

    @Test
    void deleteCustomerByIdThrowsException() throws SQLException, IOException, InterruptedException, ReflectiveOperationException
    {
        UUID id = UUID.randomUUID();
        String url = _url + "/" + id;

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Content-Type", "application/json")
                .DELETE()
                .build();

        testThrownCustomerServiceException(request, IOException.class, Response.Status.INTERNAL_SERVER_ERROR.getStatusCode());
        testThrownCustomerServiceException(request, SQLException.class, Response.Status.INTERNAL_SERVER_ERROR.getStatusCode());

        //Reflection Exception
        ServiceProvider.Services = mock(InternalServiceProvider.class);
        CustomerService mockCustomerService = mock(CustomerService.class);

        when(mockCustomerService.getById(any())).thenThrow(ReflectiveOperationException.class);
        when(ServiceProvider.Services.getCustomerService()).thenReturn(mockCustomerService);

        HttpResponse<String> response = _httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(), response.statusCode(), "Should return status code 500 internal server error");

    }

    @Test
    void auth() throws IOException, InterruptedException, ReflectiveOperationException, SQLException
    {
        Customer mockCustomer = mock(Customer.class);

        CustomerService mockedCs = mock(CustomerService.class);

        InternalServiceProvider mockedInternalServiceProvider = mock(InternalServiceProvider.class);
        ServiceProvider.Services = mockedInternalServiceProvider;
        when(mockedInternalServiceProvider.getCustomerService()).thenReturn(mockedCs);
        when(mockedCs.getById(any())).thenReturn(mockCustomer);

        _mockAuthorisationService.when(AuthorisationService::IsUserAdmin).thenReturn(false);
        _mockAuthorisationService.when(() -> AuthorisationService.CanUserAccessResource(any())).thenReturn(false);


        String jsonString = Utils.packIntoJsonString(this._customer, Customer.class);

        CustomerController cs = new CustomerController();

        assertUnauthorized(cs.addCustomer(jsonString));
        assertUnauthorized(cs.getCustomer(UUID.randomUUID()));
        assertUnauthorized(cs.getCustomers());
        assertUnauthorized(cs.updateCustomer(jsonString));
        assertUnauthorized(cs.deleteCustomer(UUID.randomUUID()));
    }

    private void assertUnauthorized(Response response)
    {
        assertEquals(Response.Status.UNAUTHORIZED.getStatusCode(), response.getStatus());
    }
}
