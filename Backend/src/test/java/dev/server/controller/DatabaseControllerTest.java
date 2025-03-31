package dev.server.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import dev.hv.database.DatabaseConnection;
import dev.hv.database.DbHelperService;
import dev.hv.database.provider.InternalServiceProvider;
import dev.hv.database.services.AuthUserService;
import dev.hv.database.services.AuthorisationService;
import dev.hv.model.classes.Authentification.AuthUser;
import dev.hv.model.classes.Authentification.AuthUserDto;
import dev.hv.model.enums.UserPermissions;
import dev.hv.model.enums.UserRoles;
import dev.hv.model.interfaces.ICustomer;
import dev.hv.model.interfaces.IReading;
import dev.hv.model.classes.Customer;
import dev.hv.model.classes.Reading;
import dev.provider.ServiceProvider;
import dev.server.Server;
import jakarta.ws.rs.core.Response;
import org.junit.jupiter.api.*;
import org.mockito.ArgumentCaptor;
import org.mockito.MockedStatic;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.mockStatic;

public class DatabaseControllerTest
{
    DatabaseConnection _connection;
    String _url = "http://0.0.0.0:8080/setupDB";
    HttpClient _httpClient;

    private static MockedStatic<AuthorisationService> _mockAuthorisationService;
    private InternalServiceProvider _mockedServices;
    private DatabaseConnection _mockedDbCon;
    private AuthUserService _mockedAuthUserService;
    private DatabaseController _dbController;
    private static MockedStatic<ServiceProvider> _mockedServiceProvider;

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

    @BeforeAll
    static void oneTimeSetup()
    {
        _mockAuthorisationService = mockStatic(AuthorisationService.class);
        _mockedServiceProvider = mockStatic(ServiceProvider.class);
    }

    @AfterAll
    static void OneTimeTearDown()
    {
        _mockAuthorisationService.close();
        _mockedServiceProvider.close();
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

        this._mockedServices = mock(InternalServiceProvider.class);
        this._mockedDbCon = mock(DatabaseConnection.class);
        this._dbController = new DatabaseController();
        this._mockedAuthUserService = mock(AuthUserService.class);
    }

    @AfterEach
    void tearDown()
    {
        Server.stopServer();
        ServiceProvider.Services = new InternalServiceProvider(100, 10, 10);
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
        assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), response.statusCode(), "Should return status code 500 Internal Server Error");

        ServiceProvider.Services = mock(InternalServiceProvider.class);
        when(ServiceProvider.Services.getDatabaseConnection()).thenThrow(new IOException());
        response = _httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(), response.statusCode(), "Should return status code 500 Internal Server Error");
    }

    @Test
    void setupDatabaseModNormal() throws SQLException, IOException
    {
        _mockAuthorisationService.when(AuthorisationService::CanResourceBeAccessed).thenReturn(true);
        registerMockedServices();
        assertEquals(Response.Status.OK.getStatusCode(), _dbController.setupDatabaseMod("", false).getStatus());
    }

    @Test
    void setupDatabaseModSecureDefault() throws SQLException, IOException, ReflectiveOperationException
    {
        AuthUser mockUser = mock(AuthUser.class);

        _mockAuthorisationService.when(AuthorisationService::CanResourceBeAccessed).thenReturn(true);
        registerMockedServices();

        when(_mockedAuthUserService.getByUserName(any())).thenReturn(null);
        when(_mockedAuthUserService.add(any())).thenReturn(mockUser);

        ArgumentCaptor<AuthUser> captor = ArgumentCaptor.forClass(AuthUser.class);
        assertEquals(Response.Status.OK.getStatusCode(), _dbController.setupDatabaseMod("", true).getStatus());

        verify(_mockedAuthUserService).add(captor.capture());
        AuthUser capturedUser = captor.getValue();
        var properties = DbHelperService.loadProperties();

        assertEquals(UserRoles.ADMIN, capturedUser.getRole());
        assertEquals(properties.getProperty("default.admin.username"), capturedUser.getUsername());
        assertEquals(properties.getProperty("default.admin.password"), capturedUser.getPassword());
    }

    @Test
    void setupDatabaseModSecureDefaultExists() throws SQLException, IOException, ReflectiveOperationException
    {
        AuthUser mockUser = mock(AuthUser.class);

        _mockAuthorisationService.when(AuthorisationService::CanResourceBeAccessed).thenReturn(true);
        registerMockedServices();

        when(_mockedAuthUserService.getByUserName(any())).thenReturn(null);
        when(_mockedAuthUserService.add(any())).thenReturn(mockUser);
        when(_mockedAuthUserService.getByUserName(any())).thenReturn(mockUser);
        assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), _dbController.setupDatabaseMod("", true).getStatus());

    }

    @Test
    void setupDatabaseModSecureCustom() throws SQLException, IOException, ReflectiveOperationException
    {
        ObjectMapper mapper = new ObjectMapper();

        AuthUserDto userDto = new AuthUserDto();
        userDto.setUsername("customAdmin");
        userDto.setPassword("customPassword");
        userDto.setRole(UserRoles.ADMIN);
        userDto.setPermissions(new ArrayList<>(List.of(UserPermissions.READ, UserPermissions.WRITE)));

        var userJson = mapper.writeValueAsString(userDto);

        AuthUser mockUser = mock(AuthUser.class);

        _mockAuthorisationService.when(AuthorisationService::CanResourceBeAccessed).thenReturn(true);
        registerMockedServices();

        when(_mockedAuthUserService.getByUserName(any())).thenReturn(null);
        when(_mockedAuthUserService.add(any())).thenReturn(mockUser);

        ArgumentCaptor<AuthUser> captor = ArgumentCaptor.forClass(AuthUser.class);
        assertEquals(Response.Status.OK.getStatusCode(), _dbController.setupDatabaseMod(userJson, true).getStatus());

        verify(_mockedAuthUserService).add(captor.capture());
        AuthUser capturedUser = captor.getValue();

        assertEquals(userDto.getRole(), capturedUser.getRole());
        assertEquals(userDto.getUsername(), capturedUser.getUsername());
        assertEquals(userDto.getPassword(), capturedUser.getPassword());
        assertEquals(userDto.getPermissions().toString(), capturedUser.getPermissions().toString());
    }

    @Test
    void auth() throws IOException, InterruptedException, ReflectiveOperationException, SQLException
    {
        _mockAuthorisationService.when(AuthorisationService::IsUserAdmin).thenReturn(false);
        _mockAuthorisationService.when(AuthorisationService::CanResourceBeAccessed).thenReturn(false);

        DatabaseController dbController = new DatabaseController();

        assertUnauthorized(dbController.setupDatabase());
        assertUnauthorized(dbController.setupDatabaseMod("", false));
    }

    private void assertUnauthorized(Response response)
    {
        assertEquals(Response.Status.UNAUTHORIZED.getStatusCode(), response.getStatus());
    }

    private void registerMockedServices() throws SQLException, IOException
    {
        ServiceProvider.Services = _mockedServices;
        _mockedServiceProvider.when(ServiceProvider::getAuthUserService).thenReturn(_mockedAuthUserService);
        when(_mockedServices.getDatabaseConnection()).thenReturn(_mockedDbCon);
    }
}
