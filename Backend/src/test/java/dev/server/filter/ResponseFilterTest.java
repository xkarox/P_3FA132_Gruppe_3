package dev.server.filter;

import dev.hv.database.services.AuthUserService;
import dev.hv.database.services.AuthorisationService;
import dev.hv.database.services.CryptoService;
import dev.hv.model.classes.Authentification.AuthUser;
import dev.provider.ServiceProvider;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerResponseContext;
import jakarta.ws.rs.core.*;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.MockedStatic;
import org.slf4j.MDC;

import java.io.IOException;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ResponseFilterTest
{
    private ContainerRequestContext _crc;
    private ContainerResponseContext _cResponseC;
    private ResponseFilter _responseFilter;
    private static MockedStatic<AuthorisationService> _mockAuth;
    private static MockedStatic<CryptoService> _crypto;
    private static MockedStatic<ServiceProvider> _serviceProvider;
    private Map<String, Cookie> _cookies;

    @BeforeAll
    public static void oneTimeSetup()
    {
        _mockAuth = mockStatic(AuthorisationService.class);
        _crypto = mockStatic(CryptoService.class);
        _serviceProvider = mockStatic(ServiceProvider.class);
    }

    @BeforeEach
    public void setUp()
    {
        MDC.clear();
        _crc = mock(ContainerRequestContext.class);
        _cResponseC = mock(ContainerResponseContext.class);
        _responseFilter = new ResponseFilter();

        _cookies = new HashMap<>();
        _cookies.put("jwt-token", null);
    }

    @AfterAll
    static void finalTearDown()
    {
        _mockAuth.close();
        _crypto.close();
        _serviceProvider.close();
    }

    @Test
    void responseFilter() throws SQLException, IOException, ReflectiveOperationException
    {
        _mockAuth.when(AuthorisationService::DoesAuthDbExistsWrapper).thenReturn(false);
        assertDoesNotThrow(() ->_responseFilter.filter(null, null));

        _mockAuth.when(AuthorisationService::DoesAuthDbExistsWrapper).thenReturn(true);
        when(_crc.getCookies()).thenReturn(_cookies);
        assertDoesNotThrow(() ->_responseFilter.filter(_crc, null));

        _cookies.put("jwt-token", new NewCookie("Test", UUID.randomUUID().toString()));
        _crypto.when(() -> CryptoService.validateToken(anyString())).thenReturn(null);
        _responseFilter.filter(_crc, null);
        checkResponse(_crc, Response.Status.UNAUTHORIZED);
    }

    @Test
    void validation () throws ReflectiveOperationException, SQLException, IOException
    {
        UUID id = UUID.randomUUID();
        _mockAuth.when(AuthorisationService::DoesAuthDbExistsWrapper).thenReturn(true);
        _cookies.put("jwt-token", new NewCookie("Test", id.toString()));

        when(_crc.getCookies()).thenReturn(_cookies);

        _crypto.when(() -> CryptoService.validateToken(anyString())).thenReturn(id.toString());
        AuthUser mockAuthUser = mock(AuthUser.class);
        when(mockAuthUser.getId()).thenReturn(id);

        AuthUserService mockAuthUserService = mock(AuthUserService.class);
        _serviceProvider.when(ServiceProvider::getAuthUserService).thenReturn(mockAuthUserService);
        when(mockAuthUserService.getById(any())).thenReturn(null);

        _responseFilter.filter(_crc, _cResponseC);
        checkResponse(_crc, Response.Status.UNAUTHORIZED);

        when(mockAuthUserService.getById(any())).thenReturn(mockAuthUser);
        _crypto.when(() -> CryptoService.createTokenCookie(id)).thenReturn(new NewCookie("test", "test"));

        MultivaluedMap<String, Object> headers = new MultivaluedHashMap<>();
        when(_cResponseC.getHeaders()).thenReturn(headers);
        _responseFilter.filter(_crc, _cResponseC);

        assertEquals(1, headers.size());
        assertTrue(headers.containsKey("Set-Cookie"));
    }

    @Test
    void nullId() throws ReflectiveOperationException, SQLException, IOException
    {
        UUID id = UUID.randomUUID();
        _mockAuth.when(AuthorisationService::DoesAuthDbExistsWrapper).thenReturn(true);
        _cookies.put("jwt-token", new NewCookie("Test", id.toString()));

        when(_crc.getCookies()).thenReturn(_cookies);

        _crypto.when(() -> CryptoService.validateToken(anyString())).thenReturn(id.toString());
        AuthUser mockAuthUser = mock(AuthUser.class);
        when(mockAuthUser.getId()).thenReturn(null);

        AuthUserService mockAuthUserService = mock(AuthUserService.class);
        _serviceProvider.when(ServiceProvider::getAuthUserService).thenReturn(mockAuthUserService);
        when(mockAuthUserService.getById(any())).thenReturn(mockAuthUser);

        _responseFilter.filter(_crc, null);
        checkResponse(_crc, Response.Status.UNAUTHORIZED);

        when(mockAuthUser.getId()).thenReturn(id);
        checkResponse(_crc, Response.Status.UNAUTHORIZED);

        _serviceProvider.when(ServiceProvider::getAuthUserService).thenThrow(new RuntimeException());
        checkResponse(_crc, Response.Status.UNAUTHORIZED);

        _crypto.when(() -> CryptoService.validateToken(anyString())).thenThrow(new RuntimeException());
        checkResponse(_crc, Response.Status.UNAUTHORIZED);

    }


    private void checkResponse(ContainerRequestContext containerContext, Response.Status status)
    {
        ArgumentCaptor<Response> responseCaptor = ArgumentCaptor.forClass(Response.class);
        verify(containerContext).abortWith(responseCaptor.capture());
        Response capturedResponse = responseCaptor.getValue();
        assertEquals(status.getStatusCode(), capturedResponse.getStatus());
    }
}