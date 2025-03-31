package dev.server.filter;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import dev.hv.database.services.AuthorisationService;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.core.Response;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.MockedStatic;
import org.slf4j.MDC;

import java.io.IOException;

class PermissionFilterTest
{
    private ContainerRequestContext _crc;
    private PermissionFilter _permissionFilter;
    private static MockedStatic<AuthorisationService> _mockAuth;

    @BeforeAll
    public static void oneTimeSetup()
    {
        _mockAuth = mockStatic(AuthorisationService.class);
    }

    @BeforeEach
    public void setUp()
    {
        MDC.clear();
        _crc = mock(ContainerRequestContext.class);
        _permissionFilter = new PermissionFilter();

    }

    @AfterAll
    static void finalTearDown()
    {
        _mockAuth.close();
    }


    @Test
    void filter() throws IOException
    {
        MDC.put("authDbExists", "false");
        assertDoesNotThrow(() -> _permissionFilter.filter(null));

        MDC.clear();

        MDC.put("authDbExists", "true");
        _mockAuth.when(() -> AuthorisationService.IsUserAdmin()).thenReturn(true);

        assertDoesNotThrow(() -> _permissionFilter.filter(null));

        _mockAuth.when(() -> AuthorisationService.IsUserAdmin()).thenReturn(false);

        MDC.put("permissions", "");

        _permissionFilter.filter(_crc);

        ArgumentCaptor<Response> responseCaptor = ArgumentCaptor.forClass(Response.class);
        verify(_crc).abortWith(responseCaptor.capture());
        Response capturedResponse = responseCaptor.getValue();
        assertEquals(Response.Status.UNAUTHORIZED.getStatusCode(), capturedResponse.getStatus());

    }

    @Test
    void filterUserPermissions() throws IOException
    {
        MDC.put("authDbExists", "true");
        MDC.put("permissions", "READ");
        _mockAuth.when(() -> AuthorisationService.IsUserAdmin()).thenReturn(false);

        when(_crc.getMethod()).thenReturn("GET");
        _permissionFilter.filter(_crc);

        when(_crc.getMethod()).thenReturn("POST");
        _permissionFilter.filter(_crc);

        ArgumentCaptor<Response> responseCaptor = ArgumentCaptor.forClass(Response.class);
        verify(_crc).abortWith(responseCaptor.capture());
        Response capturedResponse = responseCaptor.getValue();
        assertEquals(Response.Status.UNAUTHORIZED.getStatusCode(), capturedResponse.getStatus());


    }
}