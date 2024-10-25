package ace.database;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.nio.file.Paths;

public class DbTestHelper
{
    public static InputStream loadTestDbProperties ()
    {
        Path currentPath = Paths.get(System.getProperty("user.dir"));
        Path filePath = currentPath.resolve("config/properties.config.test");

        try
        {
            return new FileInputStream(String.valueOf(filePath.toAbsolutePath()));
        }
        catch (IOException e)
        {
            throw new RuntimeException(e);
        }
    }
}
