package dev.server.filter;

import dev.hv.database.services.AuthUserService;
import dev.hv.database.services.AuthorisationService;
import dev.hv.database.services.CryptoService;
import dev.hv.model.classes.Authentification.AuthUser;
import dev.hv.model.enums.UserPermissions;
import dev.hv.model.enums.UserRoles;
import dev.provider.ServiceProvider;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerResponseContext;
import jakarta.ws.rs.core.Cookie;
import jakarta.ws.rs.core.NewCookie;
import jakarta.ws.rs.core.Response;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.MockedStatic;
import org.slf4j.MDC;

import java.io.IOException;
import java.sql.SQLException;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class JwtFilterTest
{
    private AuthUser _user;
    private ContainerRequestContext _crc;
    private JwtFilter _jwtFilter;
    private static MockedStatic<AuthorisationService> _mockAuth;
    private static MockedStatic<CryptoService> _crypto;
    private static MockedStatic<ServiceProvider> _serviceProvider;
    private Map<String, Cookie> _cookies;

    @BeforeAll
    public static void oneTimeSetup()
    {
        _crypto = mockStatic(CryptoService.class);
        _serviceProvider = mockStatic(ServiceProvider.class);
        _mockAuth = mockStatic(AuthorisationService.class);
    }

    @BeforeEach
    public void setUp()
    {
        MDC.clear();
        _crc = mock(ContainerRequestContext.class);
        _jwtFilter = new JwtFilter();

        _cookies = new HashMap<>();
        _cookies.put("jwt-token", null);

        _user = new AuthUser();
        _user.setId(UUID.randomUUID());
        _user.setPassword("SuperGeheim12!");
        _user.setRole(UserRoles.USER);
        _user.setUsername("PP");
        _user.setPermissions(new ArrayList<>(List.of(UserPermissions.READ, UserPermissions.WRITE)));
    }

    @AfterAll
    static void finalTearDown()
    {
        _crypto.close();
        _serviceProvider.close();
        _mockAuth.close();
    }

    @Test
    void filter() throws IOException, ReflectiveOperationException, SQLException
    {
        _mockAuth.when(AuthorisationService::DoesAuthDbExistsWrapper).thenReturn(false);
        assertDoesNotThrow(() -> _jwtFilter.filter(null));
        assertEquals("false", MDC.get("authDbExists"));

        MDC.clear();

        _mockAuth.when(AuthorisationService::DoesAuthDbExistsWrapper).thenReturn(true);
        when(_crc.getCookies()).thenReturn(_cookies);

        assertDoesNotThrow(() -> _jwtFilter.filter(_crc));
        assertEquals("true", MDC.get("authDbExists"));
        checkResponse(_crc, Response.Status.UNAUTHORIZED);

        resetCRC();

        _cookies.put("jwt-token", new NewCookie("Test", UUID.randomUUID().toString()));
        _crypto.when(() -> CryptoService.validateToken(any())).thenReturn(UUID.randomUUID().toString());
        AuthUserService authUserService = mock(AuthUserService.class);
        _serviceProvider.when(() -> ServiceProvider.getAuthUserService()).thenReturn(authUserService);
        AuthUser mockAuthUser = mock(AuthUser.class);
        when(authUserService.getById(any())).thenReturn(null);

        assertDoesNotThrow(() -> _jwtFilter.filter(_crc));
        checkResponse(_crc, Response.Status.UNAUTHORIZED);

        resetCRC();

        when(authUserService.getById(any())).thenReturn(mockAuthUser);
        when(mockAuthUser.getId()).thenReturn(null);
        assertDoesNotThrow(() -> _jwtFilter.filter(_crc));
        checkResponse(_crc, Response.Status.UNAUTHORIZED);

        resetCRC();

        when(authUserService.getById(any())).thenReturn(_user);
        _crypto.when(() -> CryptoService.validateToken(any())).thenReturn(_user.getId().toString());

        assertDoesNotThrow(() -> _jwtFilter.filter(_crc));

        var test = MDC.get("permissions");

        assertEquals(_user.getId().toString(), MDC.get("id"));
        assertEquals(_user.getUsername(), MDC.get("username"));
        assertEquals(_user.getRole().toString(), MDC.get("role"));
        assertEquals(_user.getPermissions().toString(), "[" + MDC.get("permissions") + "]");
    }

    @Test
    void exceptions() throws ReflectiveOperationException, SQLException, IOException
    {
        _mockAuth.when(AuthorisationService::DoesAuthDbExistsWrapper).thenReturn(true);
        _cookies.put("jwt-token", new NewCookie("Test", UUID.randomUUID().toString()));
        _crypto.when(() -> CryptoService.validateToken(any())).thenReturn(UUID.randomUUID().toString());
        AuthUserService authUserService = mock(AuthUserService.class);
        AuthUser mockAuthUser = mock(AuthUser.class);
        when(authUserService.getById(any())).thenReturn(mockAuthUser);
        when(mockAuthUser.getId()).thenReturn(UUID.randomUUID());

        _serviceProvider.when(ServiceProvider::getAuthUserService).thenThrow(new SQLException());
        assertDoesNotThrow(() -> _jwtFilter.filter(_crc));
        checkResponse(_crc, Response.Status.UNAUTHORIZED);

        resetCRC();
        _serviceProvider.when(ServiceProvider::getAuthUserService).thenThrow(new IOException());
        assertDoesNotThrow(() -> _jwtFilter.filter(_crc));
        checkResponse(_crc, Response.Status.UNAUTHORIZED);

        resetCRC();
        _serviceProvider.when(ServiceProvider::getAuthUserService).thenReturn(authUserService);
        when(authUserService.getById(any())).thenThrow(new SQLException());
        assertDoesNotThrow(() -> _jwtFilter.filter(_crc));
        checkResponse(_crc, Response.Status.UNAUTHORIZED);
    }

    private void checkResponse(ContainerRequestContext containerContext, Response.Status status)
    {
        ArgumentCaptor<Response> responseCaptor = ArgumentCaptor.forClass(Response.class);
        verify(containerContext).abortWith(responseCaptor.capture());
        Response capturedResponse = responseCaptor.getValue();
        assertEquals(status.getStatusCode(), capturedResponse.getStatus());
    }

    private void resetCRC()
    {
        _crc = mock(ContainerRequestContext.class);
        when(_crc.getCookies()).thenReturn(_cookies);
    }
}