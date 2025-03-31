package dev.server.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.hv.Utils;
import dev.hv.database.DatabaseConnection;
import dev.hv.database.provider.InternalServiceProvider;
import dev.hv.database.services.*;
import dev.hv.model.classes.Authentification.AuthUser;
import dev.hv.model.classes.Authentification.AuthUserDto;
import dev.hv.model.classes.Customer;
import dev.hv.model.classes.Reading;
import dev.hv.model.enums.UserPermissions;
import dev.hv.model.enums.UserRoles;
import dev.provider.ServiceProvider;
import jakarta.ws.rs.core.NewCookie;
import jakarta.ws.rs.core.Response;
import org.junit.jupiter.api.*;
import org.mockito.MockedStatic;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AuthenticationControllerTest
{
    private static final ObjectMapper mapper = new ObjectMapper();

    private static MockedStatic<AuthorisationService> _mockAuthorisationService;
    private InternalServiceProvider _mockedServices;
    private DatabaseConnection _mockedDbCon;
    private AuthUserService _mockedAuthUserService;
    private CustomerService _mockedCustomerService;
    private static MockedStatic<ServiceProvider> _mockedServiceProvider;
    private static MockedStatic<CryptoService> _mockedCryptoService;

    private AuthUserDto _authUserDto;
    private AuthUser _authUser;
    private AuthenticationController _authController;
    private NewCookie _cookie;


    @BeforeAll
    static void oneTimeSetup()
    {
        _mockAuthorisationService = mockStatic(AuthorisationService.class);
        _mockedServiceProvider = mockStatic(ServiceProvider.class);
        _mockedCryptoService = mockStatic(CryptoService.class);
    }

    @AfterAll
    static void OneTimeTearDown()
    {
        _mockAuthorisationService.close();
        _mockedServiceProvider.close();
        _mockedCryptoService.close();
    }

    @BeforeEach
    void setUp() throws SQLException, IOException
    {
        this._mockedServices = mock(InternalServiceProvider.class);
        this._mockedDbCon = mock(DatabaseConnection.class);
        this._mockedAuthUserService = mock(AuthUserService.class);
        this._mockedCustomerService = mock(CustomerService.class);

        registerMockedServices();

        _authUserDto = new AuthUserDto(){{
            setUsername("test");
            setPassword("test");
            setRole(UserRoles.ADMIN);
            setPermissions(new ArrayList<>(){{
                add(UserPermissions.READ);
                add(UserPermissions.WRITE);
            }});
        }};

        _authUser = new AuthUser(_authUserDto);
        _authUser.setId(UUID.randomUUID());
        _authController = new AuthenticationController();
        _cookie = new NewCookie("token", _authUser.getId().toString());
    }

    @AfterEach
    void tearDown()
    {
        ServiceProvider.Services = new InternalServiceProvider(100, 10, 10);
    }

    @Test
    void login() throws IOException, ReflectiveOperationException, SQLException
    {
        String userBody = "{\"username\":\"test\",\"password\":\"test\"}";
        when(_mockedAuthUserService.getByUserName("test")).thenReturn(_authUser);
        _mockedCryptoService.when(() -> CryptoService.compareStringWithHash(any(), any())).thenReturn(true);
        _mockedCryptoService.when(() -> CryptoService.createTokenCookie(any())).thenReturn(_cookie);

        var res = _authController.login(userBody);
        assertEquals(200, res.getStatus());
        assertEquals(_cookie, res.getCookies().get("token"));
        _authUserDto.setPassword(null);
        _authUserDto.setId(_authUser.getId());
        var userDtoString = mapper.writeValueAsString(_authUserDto);
        assertEquals(userDtoString, res.getEntity());
    }

    @Test
    void login_userNotFound() throws IOException, ReflectiveOperationException, SQLException
    {
        String userBody = "{\"username\":\"test\",\"password\":\"test\"}";
        when(_mockedAuthUserService.getByUserName("test")).thenReturn(null);

        var res = _authController.login(userBody);
        assertEquals(404, res.getStatus());
    }

    @Test
    void login_passwordIncorrect() throws IOException, ReflectiveOperationException, SQLException
    {
        String userBody = "{\"username\":\"test\",\"password\":\"test\"}";
        when(_mockedAuthUserService.getByUserName("test")).thenReturn(_authUser);
        _mockedCryptoService.when(() -> CryptoService.compareStringWithHash(any(), any())).thenReturn(false);

        var res = _authController.login(userBody);
        assertEquals(401, res.getStatus());
    }

    @Test
    void login_badRequest() throws IOException, ReflectiveOperationException, SQLException
    {
        String userBody = "{\"username\":\"test\",\"password\":\"test\"}";
        when(_mockedAuthUserService.getByUserName("test")).thenThrow(new SQLException());

        var res = _authController.login(userBody);
        assertEquals(400, res.getStatus());
    }

    @Test
    void login_internalServerError() throws IOException, ReflectiveOperationException, SQLException
    {
        String userBody = "{\"username\":\"test\",\"password\":\"test\"}";
        when(_mockedAuthUserService.getByUserName("test")).thenThrow(new IOException());

        var res = _authController.login(userBody);
        assertEquals(500, res.getStatus());
    }

    @Test
    void register() throws IOException, ReflectiveOperationException, SQLException
    {
        _authUserDto.setId(_authUser.getId());
        String userBody = mapper.writeValueAsString(_authUserDto);
        when(_mockedAuthUserService.getByUserName("test")).thenReturn(null);
        when(_mockedAuthUserService.add(any())).thenReturn(_authUser);
        when(_mockedCustomerService.getById(any())).thenReturn(new Customer());

        _mockAuthorisationService.when(AuthorisationService::IsUserAdmin).thenReturn(true);
        assertEquals(201, _authController.register(userBody).getStatus());
    }

    @Test
    void register_userIdNull() throws IOException, ReflectiveOperationException, SQLException
    {
        String userBody = mapper.writeValueAsString(_authUserDto);
        _mockAuthorisationService.when(AuthorisationService::IsUserAdmin).thenReturn(true);
        assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), _authController.register(userBody).getStatus());
    }

    @Test
    void register_userExists() throws IOException, ReflectiveOperationException, SQLException
    {
        _authUserDto.setId(_authUser.getId());
        String userBody = mapper.writeValueAsString(_authUserDto);
        _mockAuthorisationService.when(AuthorisationService::IsUserAdmin).thenReturn(true);
        when(_mockedAuthUserService.getById(any())).thenReturn(new AuthUser());
        assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), _authController.register(userBody).getStatus());
    }

    @Test
    void register_customerNotFound() throws IOException, ReflectiveOperationException, SQLException
    {
        _authUserDto.setId(_authUser.getId());
        String userBody = mapper.writeValueAsString(_authUserDto);
        _mockAuthorisationService.when(AuthorisationService::IsUserAdmin).thenReturn(true);
        when(_mockedAuthUserService.getById(any())).thenReturn(null);
        when(_mockedCustomerService.getById(any())).thenReturn(null);

        assertEquals(Response.Status.NOT_FOUND.getStatusCode(), _authController.register(userBody).getStatus());
    }

    @Test
    void register_sqlException() throws IOException, ReflectiveOperationException, SQLException
    {
        _authUserDto.setId(_authUser.getId());
        String userBody = mapper.writeValueAsString(_authUserDto);
        _mockAuthorisationService.when(AuthorisationService::IsUserAdmin).thenReturn(true);
        when(_mockedAuthUserService.getById(any())).thenReturn(null);
        when(_mockedCustomerService.getById(any())).thenReturn(new Customer());
        when(_mockedAuthUserService.add(any())).thenThrow(new SQLException());

        assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), _authController.register(userBody).getStatus());
    }

    @Test
    void register_internalServerError() throws IOException, ReflectiveOperationException, SQLException
    {
        _authUserDto.setId(_authUser.getId());
        String userBody = mapper.writeValueAsString(_authUserDto);
        _mockAuthorisationService.when(AuthorisationService::IsUserAdmin).thenReturn(true);
        when(_mockedAuthUserService.getById(any())).thenReturn(null);
        when(_mockedCustomerService.getById(any())).thenReturn(new Customer());
        when(_mockedAuthUserService.add(any())).thenThrow(new IOException());

        assertEquals(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(), _authController.register(userBody).getStatus());
    }

    @Test
    void delete() throws IOException, ReflectiveOperationException, SQLException
    {
        _authUserDto.setId(_authUser.getId());
        _mockAuthorisationService.when(AuthorisationService::IsUserAdmin).thenReturn(true);
        when(_mockedAuthUserService.getById(any())).thenReturn(_authUser);

        assertEquals(Response.Status.OK.getStatusCode(), _authController.delete(_authUserDto.getId()).getStatus());
    }

    @Test
    void delete_userNotFound() throws IOException, ReflectiveOperationException, SQLException
    {
        _authUserDto.setId(_authUser.getId());
        _mockAuthorisationService.when(AuthorisationService::IsUserAdmin).thenReturn(true);
        when(_mockedAuthUserService.getById(any())).thenReturn(null);

        assertEquals(Response.Status.NOT_FOUND.getStatusCode(), _authController.delete(_authUserDto.getId()).getStatus());
    }

    @Test
    void delete_badRequest() throws IOException, ReflectiveOperationException, SQLException
    {
        _authUserDto.setId(_authUser.getId());
        _mockAuthorisationService.when(AuthorisationService::IsUserAdmin).thenReturn(true);
        when(_mockedAuthUserService.getById(any())).thenThrow(new SQLException());

        assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), _authController.delete(_authUserDto.getId()).getStatus());
    }

    @Test
    void delete_internalServerError() throws IOException, ReflectiveOperationException, SQLException
    {
        _authUserDto.setId(_authUser.getId());
        _mockAuthorisationService.when(AuthorisationService::IsUserAdmin).thenReturn(true);
        when(_mockedAuthUserService.getById(any())).thenThrow(new IOException());

        assertEquals(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(), _authController.delete(_authUserDto.getId()).getStatus());
    }

    @Test
    void update() throws IOException, ReflectiveOperationException, SQLException
    {
        _authUserDto.setId(_authUser.getId());
        _mockAuthorisationService.when(AuthorisationService::IsUserAdmin).thenReturn(true);
        when(_mockedAuthUserService.getByUserName(any())).thenReturn(_authUser);

        assertEquals(Response.Status.OK.getStatusCode(), _authController.update(mapper.writeValueAsString(_authUserDto)).getStatus());
    }

    @Test
    void update_userNotFound() throws IOException, ReflectiveOperationException, SQLException
    {
        _authUserDto.setId(_authUser.getId());
        _mockAuthorisationService.when(AuthorisationService::IsUserAdmin).thenReturn(true);
        when(_mockedAuthUserService.getByUserName(any())).thenReturn(null);

        assertEquals(Response.Status.NOT_FOUND.getStatusCode(), _authController.update(mapper.writeValueAsString(_authUserDto)).getStatus());
    }

    @Test
    void update_badRequest() throws IOException, ReflectiveOperationException, SQLException
    {
        _authUserDto.setId(_authUser.getId());
        _mockAuthorisationService.when(AuthorisationService::IsUserAdmin).thenReturn(true);
        when(_mockedAuthUserService.getByUserName(any())).thenThrow(new SQLException());

        assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), _authController.update(mapper.writeValueAsString(_authUserDto)).getStatus());
    }

    @Test
    void update_internalServerError() throws IOException, ReflectiveOperationException, SQLException
    {
        _authUserDto.setId(_authUser.getId());
        _mockAuthorisationService.when(AuthorisationService::IsUserAdmin).thenReturn(true);
        when(_mockedAuthUserService.getByUserName(any())).thenThrow(new IOException());

        assertEquals(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(), _authController.update(mapper.writeValueAsString(_authUserDto)).getStatus());
    }

    @Test
    void getAllUsers() throws IOException, ReflectiveOperationException, SQLException
    {
        _mockAuthorisationService.when(AuthorisationService::IsUserAdmin).thenReturn(true);
        when(_mockedAuthUserService.getAll()).thenReturn(new ArrayList<>(){{
            add(_authUser);
        }});

        Response res = _authController.getAllUsers();
        assertEquals(200, res.getStatus());

        String responseBody = res.getEntity().toString();

        List<AuthUserDto> objectList = mapper.readValue(responseBody, new TypeReference<List<AuthUserDto>>() {});
        assertEquals(1, objectList.size());
    }

    @Test
    void getAllUsers_badRequest() throws IOException, ReflectiveOperationException, SQLException
    {
        _mockAuthorisationService.when(AuthorisationService::IsUserAdmin).thenReturn(true);
        when(_mockedAuthUserService.getAll()).thenThrow(new SQLException());

        assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), _authController.getAllUsers().getStatus());
    }

    @Test
    void getAllUsers_internalServerError() throws IOException, ReflectiveOperationException, SQLException
    {
        _mockAuthorisationService.when(AuthorisationService::IsUserAdmin).thenReturn(true);
        when(_mockedAuthUserService.getAll()).thenThrow(new IOException());

        assertEquals(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(), _authController.getAllUsers().getStatus());
    }

    @Test
    void auth() throws IOException, InterruptedException, ReflectiveOperationException, SQLException
    {
        _mockAuthorisationService.when(AuthorisationService::IsUserAdmin).thenReturn(false);

        assertUnauthorized(_authController.register(""));
        assertUnauthorized(_authController.delete(UUID.randomUUID()));
        assertUnauthorized(_authController.update(""));
        assertUnauthorized(_authController.getAllUsers());
    }

    private void registerMockedServices() throws SQLException, IOException
    {
        ServiceProvider.Services = _mockedServices;
        _mockedServiceProvider.when(ServiceProvider::getAuthUserService).thenReturn(_mockedAuthUserService);
        when(_mockedServices.getDatabaseConnection()).thenReturn(_mockedDbCon);
        when(_mockedServices.getCustomerService()).thenReturn(_mockedCustomerService);
    }

    private void assertUnauthorized(Response response)
    {
        assertEquals(Response.Status.UNAUTHORIZED.getStatusCode(), response.getStatus());
    }
}