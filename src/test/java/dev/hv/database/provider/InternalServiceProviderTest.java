package dev.hv.database.provider;

import dev.hv.ErrorMessages;
import dev.hv.database.DatabaseConnection;
import dev.hv.database.DbHelperService;
import dev.hv.database.DbTestHelper;
import dev.provider.ServiceProvider;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.lang.reflect.Field;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;

class InternalServiceProviderTest
{
    private static final BlockingQueue<Long> resultQueue = new LinkedBlockingQueue<>();
    private static final AtomicReference<Throwable> capturedError = new AtomicReference<>(null);

    @BeforeAll
    static void OneTimeSetup() throws IOException
    {
        ServiceProvider.Services.dbConnectionPropertiesOverwrite(DbHelperService.loadProperties(DbTestHelper.loadTestDbProperties()));
    }

    @BeforeEach
    void beforeEach()
    {
        ServiceProvider.Services.setMultithreading(false);
        ServiceProvider.Services = new InternalServiceProvider(100, 10, 10);
    }

    @AfterAll
    static void afterAll()
    {
        ServiceProvider.Services = new InternalServiceProvider(100, 10, 10);
    }

    @Test
    void BasicTest() throws SQLException, IOException
    {
        assertOpenConnections(0, 0, 0);
        try(var dbCon = ServiceProvider.Services.getDatabaseConnection())
        {
            assertNotNull(dbCon);
            assertFalse(dbCon.getConnection().isClosed());
            assertOpenConnections(1, 0, 0);
        }
        try(var customerService = ServiceProvider.Services.getCustomerService())
        {
            assertNotNull(customerService);
            assertOpenConnections(1, 1, 0);
        }
        try(var readingService = ServiceProvider.Services.getReadingService())
        {
            assertNotNull(readingService);
            assertOpenConnections(1, 0, 1);
        }
        assertOpenConnections(0, 0, 0);
    }

    @Test
    void OverloadTest() throws SQLException, IOException
    {
        ServiceProvider.Services.configureMaxConnections(1, 0, 0);

        DatabaseConnection dbCon = ServiceProvider.Services.getDatabaseConnection();
        boolean exceptionTriggert = false;
        try
        {
            ServiceProvider.Services.getCustomerService();
        } catch (IllegalArgumentException e)
        {
            exceptionTriggert = true;
            assertTrue(e.getMessage().contains(String.valueOf(ErrorMessages.ServicesNotAvailable)));
        }
        assertTrue(exceptionTriggert, "Because the simFailedException should have been triggert");

        exceptionTriggert = false;
        try
        {
            ServiceProvider.Services.getReadingService();
        } catch (IllegalArgumentException e)
        {
            exceptionTriggert = true;
            assertTrue(e.getMessage().contains(String.valueOf(ErrorMessages.ServicesNotAvailable)));
        }
        assertTrue(exceptionTriggert, "Because the simFailedException should have been triggert");

        // Trigger other part of the if statement
        ServiceProvider.Services.configureMaxConnections(2, 0, 0);
        exceptionTriggert = false;
        try
        {
            ServiceProvider.Services.getCustomerService();
        } catch (IllegalArgumentException e)
        {
            exceptionTriggert = true;
            assertTrue(e.getMessage().contains(String.valueOf(ErrorMessages.ServicesNotAvailable)));
        }
        assertTrue(exceptionTriggert, "Because the simFailedException should have been triggert");

        exceptionTriggert = false;
        try
        {
            ServiceProvider.Services.getReadingService();
        } catch (IllegalArgumentException e)
        {
            exceptionTriggert = true;
            assertTrue(e.getMessage().contains(String.valueOf(ErrorMessages.ServicesNotAvailable)));
        }
        assertTrue(exceptionTriggert, "Because the simFailedException should have been triggert");
    }

    @Test
    void ReleaseUnregisteredConnectionTest() throws SQLException, IOException
    {
        InternalServiceProvider provider = new InternalServiceProvider(100, 10, 10);
        DatabaseConnection dbCon = ServiceProvider.Services.getDatabaseConnection();

        boolean exceptionTriggert = false;
        try
        {
            provider.releaseDbConnection(dbCon);
        } catch (IllegalArgumentException e)
        {
            exceptionTriggert = true;
            assertTrue(e.getMessage().contains("Connection was not registered with the current service provider."));
        }
        assertTrue(exceptionTriggert, "Because the simFailedException should have been triggert");
    }

    @Test
    void FindUnusedService() throws NoSuchFieldException, IllegalAccessException, SQLException, IOException
    {
        InternalServiceProvider services = new InternalServiceProvider(2, 0, 0);

        // Configure new unused connection
        DatabaseConnection testCon = new DatabaseConnection();
        DatabaseConnection testCon1 = new DatabaseConnection();
        Map<Integer, DatabaseConnection> possibleDbConnections = new HashMap<>();
        possibleDbConnections.put(System.identityHashCode(testCon), testCon);
        possibleDbConnections.put(System.identityHashCode(testCon1), testCon1);

        DatabaseConnection lowerCon = System.identityHashCode(testCon) < System.identityHashCode(testCon1) ? testCon : testCon1;

        Field secretField = InternalServiceProvider.class.getDeclaredField("_possibleDbConnections");
        secretField.setAccessible(true);
        secretField.set(services, possibleDbConnections);

        List<Integer> usedDbConnections = new ArrayList<>();
        usedDbConnections.add(System.identityHashCode(lowerCon));

        Field secretField1 = InternalServiceProvider.class.getDeclaredField("_usedDbConnections");
        secretField1.setAccessible(true);
        secretField1.set(services, usedDbConnections);

        try(DatabaseConnection con1 = services.getDatabaseConnection())
        {
            assertEquals(System.identityHashCode(testCon) ,System.identityHashCode(con1), "Because the connection should be the same");
        }
    }

    @Test
    // ToDo: Test with multiple allowed connections
    void MultithreadingQueueingTest() throws InterruptedException
    {
        ServiceProvider.Services.setMultithreading(true);
        int waitTime = 250;
        int drift = 25;
        ServiceProvider.Services.configureMaxConnections(1, 0, 0);
        Thread[] threads = new Thread[4];
        threads[0] = createNewConnectionThread(waitTime);
        threads[1] = createNewConnectionThread(waitTime);
        threads[2] = createNewConnectionThread(waitTime);
        threads[3] = createNewConnectionThread(waitTime);

        for (Thread thread : threads)
        {
            thread.start();
        }

        for (Thread thread : threads) {
            try {
                thread.join();
            } catch (InterruptedException e) {
                System.out.println("Warten auf Thread unterbrochen");
            }
        }

        long lastTimestamp = -1;
        while (!resultQueue.isEmpty()) {
            long result = resultQueue.take();
            if (lastTimestamp == -1) {
                lastTimestamp = result;
                continue;
            }
            long expected = lastTimestamp + waitTime;
            long timestampDiff = result - expected;
            assertTrue(result >= expected - drift && result <= expected + drift, "Because the timestamps should start after another. Diff: " + timestampDiff);
            lastTimestamp = result;
        }

        assertNull(capturedError.get());
    }

    @Test
    void ThreadInterruptTest() throws InterruptedException
    {
        ServiceProvider.Services.setMultithreading(true);
        ServiceProvider.Services.configureMaxConnections(1, 0, 0);
        Thread[] threads = new Thread[2];
        threads[0] = createNewConnectionThread(200);
        threads[1] = createNewConnectionThread(200);

        threads[0].start();
        Thread.sleep(50);
        threads[1].start();
        Thread.sleep(50);
        threads[1].interrupt();

        for (Thread thread : threads) {
            thread.join();
        }
        var throwable = capturedError.get();
        // assertThrows(AssertionFailedError.class, () -> {throw throwable;});
        assertNull(throwable);

    }

    @Test
        // Just for coverage in jacoco report
    void staticTest()
    {
        ServiceProvider serviceProvider = new ServiceProvider();
    }

    private Thread createNewConnectionThread(int timeout)
    {
        return new Thread(() -> {
            boolean nullChecked = false;
            try (var dbConnection = ServiceProvider.Services.getDatabaseConnection())
            {
                nullChecked = dbConnection == null;
                Thread.sleep(timeout);
                resultQueue.put(System.currentTimeMillis());
            } catch (InterruptedException | IOException | SQLException e) {
                try{
                    assertTrue(nullChecked);
                } catch (AssertionError error) {
                    capturedError.set(error);
                }
                nullChecked = false;
            }
            assertFalse(nullChecked);
        });
    }

    private void assertOpenConnections(int dbCon, int custCon, int readCon)
    {
        assertEquals(dbCon, ServiceProvider.Services.getOpenDbConnectionsCount());
        assertEquals(custCon, ServiceProvider.Services.getOpenCustomerServicesCount());
        assertEquals(readCon, ServiceProvider.Services.getOpenReadingServicesCount());
    }
}