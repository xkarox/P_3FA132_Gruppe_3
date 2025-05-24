package dev.hv.database.services;

import dev.hv.database.DbHelperService;
import dev.hv.model.classes.Customer;
import dev.provider.ServiceProvider;
import jakarta.ws.rs.core.NewCookie;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.SQLException;
import java.util.Properties;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

class CryptoServiceTest {

    private Customer mockCustomer;
    private AuthUserService mockAuthService;

    @BeforeEach
    void setUp() {
        mockCustomer = mock(Customer.class);
        mockAuthService = mock(AuthUserService.class);
    }

    @AfterEach
    void tearDown() {
    }

    @Test
    void getExpirationTime() {
        assertEquals(300000, CryptoService.getExpirationTime());
    }

    @Test
    void createNewUsername() throws SQLException, IOException, ReflectiveOperationException {
        when(mockCustomer.getFirstName()).thenReturn("Max");
        when(mockCustomer.getLastName()).thenReturn("Mustermann");

        try (MockedStatic<ServiceProvider> mockedServiceProvider = Mockito.mockStatic(ServiceProvider.class)) {
            mockedServiceProvider.when(ServiceProvider::getAuthUserService).thenReturn(mockAuthService);
            when(mockAuthService.DisplayNameAvailable(anyString())).thenReturn(true);

            String username = CryptoService.CreateNewUsername(mockCustomer);

            assertNotNull(username);
            assertTrue(username.startsWith("max_"));
            assertTrue(username.contains("_mustermann_"));
            verify(mockAuthService).DisplayNameAvailable(anyString());
        }
    }

    @Test
    void createNewUsername_NoAvailableNames() throws SQLException, IOException, ReflectiveOperationException {
        when(mockCustomer.getFirstName()).thenReturn("Max");
        when(mockCustomer.getLastName()).thenReturn("Mustermann");

        try (MockedStatic<ServiceProvider> mockedServiceProvider = Mockito.mockStatic(ServiceProvider.class)) {
            mockedServiceProvider.when(ServiceProvider::getAuthUserService).thenReturn(mockAuthService);
            when(mockAuthService.DisplayNameAvailable(anyString())).thenReturn(false);

            assertThrows(RuntimeException.class, () -> CryptoService.CreateNewUsername(mockCustomer));
            verify(mockAuthService, times(5)).DisplayNameAvailable(anyString());
        }
    }

    @Test
    void hashStringWithSalt() throws IOException {
        // Vorbereiten
        Properties mockProps = new Properties();
        mockProps.setProperty("saltySecret", "testSalt");

        try (MockedStatic<DbHelperService> mockedDbHelper = Mockito.mockStatic(DbHelperService.class)) {
            mockedDbHelper.when(DbHelperService::loadProperties).thenReturn(mockProps);

            // Ausführen
            String hash1 = CryptoService.hashStringWithSalt("testString");
            String hash2 = CryptoService.hashStringWithSalt("testString");

            // Überprüfen
            assertNotNull(hash1);
            assertEquals(64, hash1.length()); // SHA-256 erzeugt 64 Zeichen im Hex-Format
            assertEquals(hash1, hash2); // Gleiche Eingabe sollte gleichen Hash erzeugen
        }
    }

    @ParameterizedTest
    @ValueSource(strings = {"password123", "test", "complexPassword!123"})
    void compareStringWithHash_ValidComparison(String input) throws IOException {
        // Vorbereiten
        Properties mockProps = new Properties();
        mockProps.setProperty("saltySecret", "testSalt");

        try (MockedStatic<DbHelperService> mockedDbHelper = Mockito.mockStatic(DbHelperService.class)) {
            mockedDbHelper.when(DbHelperService::loadProperties).thenReturn(mockProps);

            // Ausführen
            String hashedInput = CryptoService.hashStringWithSalt(input);

            // Überprüfen
            assertTrue(CryptoService.compareStringWithHash(input, hashedInput));
            assertFalse(CryptoService.compareStringWithHash("falscheEingabe", hashedInput));
        }
    }

    @Test
    void generateToken() {
        // Vorbereiten
        UUID testId = UUID.randomUUID();

        // Ausführen
        String token = CryptoService.generateToken(testId);

        // Überprüfen
        assertNotNull(token);
        assertFalse(token.isEmpty());
    }

    @Test
    void validateToken() {
        // Vorbereiten
        UUID testId = UUID.randomUUID();
        String token = CryptoService.generateToken(testId);

        // Ausführen
        String extractedId = CryptoService.validateToken(token);

        // Überprüfen
        assertEquals(testId.toString(), extractedId);
    }

    @Test
    void validateToken_InvalidToken() {
        // Überprüfen dass eine Exception geworfen wird bei ungültigem Token
        assertThrows(Exception.class, () -> CryptoService.validateToken("ungültiger.token.string"));
    }

    @Test
    void createTokenCookie() {
        // Vorbereiten
        UUID testId = UUID.randomUUID();

        // Ausführen
        NewCookie cookie = CryptoService.createTokenCookie(testId);

        // Überprüfen
        assertNotNull(cookie);
        assertEquals("jwt-token", cookie.getName());
        assertFalse(cookie.isSecure());
        assertTrue(cookie.isHttpOnly());
        assertEquals("/", cookie.getPath());
        assertEquals(CryptoService.getExpirationTime(), cookie.getMaxAge());

        // Validiere, dass der Token im Cookie gültig ist
        String extractedId = CryptoService.validateToken(cookie.getValue());
        assertEquals(testId.toString(), extractedId);
    }

    @Test
    // Because of test coverage
    void staticClassCreate()
    {
        var test = new CryptoService();
    }
}
