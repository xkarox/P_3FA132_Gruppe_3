package ace.database.provider;

import ace.database.DatabaseConnection;
import ace.database.DbHelperService;
import ace.database.DbTestHelper;
import ace.model.classes.Customer;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.lang.reflect.Field;
import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.*;

class ServiceProviderTest
{
    private static InternalServiceProvider internalServiceProvider;

    @BeforeAll
    static void OneTimeSetup() throws NoSuchFieldException, IllegalAccessException, IOException
    {
        ServiceProvider.DbConnectionPropertiesOverwrite(DbHelperService.loadProperties(DbTestHelper.loadTestDbProperties()));
        Field secretField = ServiceProvider.class.getDeclaredField("_services");
        secretField.setAccessible(true);
        internalServiceProvider = (InternalServiceProvider) secretField.get("_services");
    }

    @Test
    void GetDatabaseConnectionTest() throws SQLException, IOException
    {
        try(DatabaseConnection con = ServiceProvider.GetDatabaseConnection())
        {
            assertNotNull(con);
            assertFalse(con.getConnection().isClosed());
        }
    }

    @Test
    void GetCustomerServiceTest()
    {
    }

    @Test
    void GetReadingServiceTest()
    {
    }

    @Test
    void DbConnectionPropertiesOverwriteTest()
    {
    }
}