package dev.hv.database.services;

import dev.hv.model.enums.UserRoles;
import dev.provider.ServiceProvider;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.slf4j.MDC;

import java.sql.SQLException;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class AuthorisationServiceTest
{

    private AuthUserService mockAuthUserService;
    private MockedStatic<ServiceProvider> mockedServiceProvider;

    @BeforeEach
    public void setUp()
    {
        mockAuthUserService = mock(AuthUserService.class);
        mockedServiceProvider = mockStatic(ServiceProvider.class);
        mockedServiceProvider.when(() -> ServiceProvider.getAuthUserService()).thenReturn(mockAuthUserService);
        MDC.clear();
    }

    @AfterEach
    public void tearDown()
    {
        mockedServiceProvider.close();
        MDC.clear();
    }

    @Test
    public void testDoesAuthDbExistsWrapper_WhenDatabaseExists() throws SQLException
    {
        when(mockAuthUserService.checkIfAuthDatabaseExists()).thenReturn(true);

        boolean result = AuthorisationService.DoesAuthDbExistsWrapper();

        assertTrue(result);
        verify(mockAuthUserService).checkIfAuthDatabaseExists();
    }

    @Test
    public void testDoesAuthDbExistsWrapper_WhenDatabaseDoesNotExist() throws SQLException
    {
        when(mockAuthUserService.checkIfAuthDatabaseExists()).thenReturn(false);

        boolean result = AuthorisationService.DoesAuthDbExistsWrapper();

        assertFalse(result);
        verify(mockAuthUserService).checkIfAuthDatabaseExists();
    }

    @Test
    public void testDoesAuthDbExistsWrapper_WhenSQLExceptionWithTableNotExists() throws SQLException
    {
        when(mockAuthUserService.checkIfAuthDatabaseExists())
                .thenThrow(new SQLException("'homeautomation.authenticationinformation' doesn't exist"));

        boolean result = AuthorisationService.DoesAuthDbExistsWrapper();

        assertFalse(result);
        verify(mockAuthUserService).checkIfAuthDatabaseExists();
    }

    @Test
    public void testDoesAuthDbExistsWrapper_WhenSQLExceptionWithOtherMessage() throws SQLException
    {
        SQLException sqlException = new SQLException("Other error message");
        when(mockAuthUserService.checkIfAuthDatabaseExists()).thenThrow(sqlException);

        Exception exception = assertThrows(RuntimeException.class, () ->
        {
            AuthorisationService.DoesAuthDbExistsWrapper();
        });

        assertEquals(sqlException, exception.getCause());
        verify(mockAuthUserService).checkIfAuthDatabaseExists();
    }

    @Test
    public void testIsUserAdmin_WhenAuthDbFlagIsTrue()
    {
        // MDC ist leer, AuthDbFlag sollte true zurückgeben
        boolean result = AuthorisationService.IsUserAdmin();

        assertTrue(result);
    }

    @Test
    public void testIsUserAdmin_WhenAuthDbFlagIsFalseAndUserIsAdmin()
    {
        MDC.put("authDbExists", "true");
        MDC.put("role", UserRoles.ADMIN.toString());

        boolean result = AuthorisationService.IsUserAdmin();

        assertTrue(result);
    }

    @Test
    public void testIsUserAdmin_WhenAuthDbFlagIsFalseAndUserIsNotAdmin()
    {
        MDC.put("authDbExists", "true");
        MDC.put("role", UserRoles.USER.toString());

        boolean result = AuthorisationService.IsUserAdmin();

        assertFalse(result);
    }

    @Test
    public void testCanUserAccessResource_WhenAuthDbFlagIsTrue()
    {
        UUID userId = UUID.randomUUID();

        boolean result = AuthorisationService.CanUserAccessResource(userId);

        assertTrue(result);
    }

    @Test
    public void testCanUserAccessResource_WhenUserIdsMatch()
    {
        UUID userId = UUID.randomUUID();
        MDC.put("authDbExists", "true");
        MDC.put("id", userId.toString());

        boolean result = AuthorisationService.CanUserAccessResource(userId);

        assertTrue(result);
    }

    @Test
    public void testCanUserAccessResource_WhenUserIdsDoNotMatchButUserIsAdmin()
    {
        UUID userId = UUID.randomUUID();
        UUID differentUserId = UUID.randomUUID();
        MDC.put("authDbExists", "true");
        MDC.put("id", differentUserId.toString());
        MDC.put("role", UserRoles.ADMIN.toString());

        boolean result = AuthorisationService.CanUserAccessResource(userId);

        assertTrue(result);
    }

    @Test
    public void testCanUserAccessResource_WhenUserIdsDoNotMatchAndUserIsNotAdmin()
    {
        UUID userId = UUID.randomUUID();
        UUID differentUserId = UUID.randomUUID();
        MDC.put("authDbExists", "true");
        MDC.put("id", differentUserId.toString());
        MDC.put("role", UserRoles.USER.toString());

        boolean result = AuthorisationService.CanUserAccessResource(userId);

        assertFalse(result);
    }

    @Test
    public void testCanResourceBeAccessed_WhenAuthDbFlagIsTrue()
    {
        boolean result = AuthorisationService.CanResourceBeAccessed();

        assertTrue(result);
    }

    @Test
    public void testCanResourceBeAccessed_WhenAuthDbFlagIsFalseAndUserIsAdmin()
    {
        MDC.put("authDbExists", "true");
        MDC.put("role", UserRoles.ADMIN.toString());

        boolean result = AuthorisationService.CanResourceBeAccessed();

        assertTrue(result);
    }

    @Test
    public void testCanResourceBeAccessed_WhenAuthDbFlagIsFalseAndUserIsNotAdmin()
    {
        MDC.put("authDbExists", "true");
        MDC.put("role", UserRoles.USER.toString());

        boolean result = AuthorisationService.CanResourceBeAccessed();

        assertFalse(result);
    }

    @Test
        // Because of test coverage
    void staticClassCreate()
    {
        var test = new AuthorisationService();
    }
}
