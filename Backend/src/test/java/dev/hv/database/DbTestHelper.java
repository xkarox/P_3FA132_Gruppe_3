package dev.hv.database;

import dev.hv.database.provider.InternalServiceProvider;
import dev.provider.ServiceProvider;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;

public class DbTestHelper
{
    public static InputStream loadTestDbProperties()
    {
        Path currentPath = Paths.get(System.getProperty("user.dir"));
        Path filePath = currentPath.resolve("config/properties.config.test");

        try
        {
            return new FileInputStream(String.valueOf(filePath.toAbsolutePath()));
        } catch (IOException e)
        {
            throw new RuntimeException(e);
        }
    }

    public static void InsertMockConnection(DatabaseConnection dbConnection, Connection mockConnection) throws NoSuchFieldException, IllegalAccessException
    {
        Field secretField = DatabaseConnection.class.getDeclaredField("_connection");
        secretField.setAccessible(true);
        secretField.set(dbConnection, mockConnection);
    }

    public static void LoadTestServiceProvider() throws IOException
    {
        ServiceProvider.Services.dbConnectionPropertiesOverwrite(DbHelperService.loadProperties(DbTestHelper.loadTestDbProperties()));
    }

    public static void UnloadTestServiceProvider()
    {
        ServiceProvider.Services.setMultithreading(false);
        ServiceProvider.Services = new InternalServiceProvider(100, 10, 10);
    }
}
