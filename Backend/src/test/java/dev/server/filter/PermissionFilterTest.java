package dev.server.filter;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import dev.hv.database.services.AuthUserService;
import dev.hv.database.services.AuthorisationService;
import dev.provider.ServiceProvider;
import jakarta.ws.rs.container.ContainerRequestContext;
import org.glassfish.jersey.server.ContainerRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.slf4j.MDC;

import java.io.IOException;
import java.lang.reflect.Field;

class PermissionFilterTest
{
    @BeforeEach
    public void setUp()
    {
        MDC.clear();
    }

    @Test
    void filter() throws IOException
    {
        PermissionFilter permissionFilter = new PermissionFilter();
        MDC.put("authDbExists", "false");
        assertDoesNotThrow(() -> permissionFilter.filter(null));

        MDC.clear();

        MDC.put("authDbExists", "true");
        MockedStatic<AuthorisationService> mockAuth = mockStatic(AuthorisationService.class);
        mockAuth.when(() -> AuthorisationService.IsUserAdmin()).thenReturn(true);

        assertDoesNotThrow(() -> permissionFilter.filter(null));

        mockAuth.when(() -> AuthorisationService.IsUserAdmin()).thenReturn(false);
        MDC.clear();

        MDC.put("permissions", "");
        // ToDo implement this shit
    }
}