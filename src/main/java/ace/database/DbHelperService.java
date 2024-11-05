package ace.database;

import ace.model.classes.Customer;
import ace.model.classes.Reading;
import ace.model.interfaces.IDbItem;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class DbHelperService
{
    private List<IDbItem> _tables = new ArrayList<>()
    {
        {
            add(new Customer());
            add(new Reading());
        }
    };

    public DbHelperService()
    {
    }

    public DbHelperService(List<IDbItem> tables)
    {
        this._tables = tables;
    }

    public List<String> createSqlTableSchemaCommands()
    {
        List<String> commands = new ArrayList<>();

        for (IDbItem table : _tables)
        {
            String columnDefinition = table.getSerializedStructure();
            String tableName = table.getSerializedTableName();
            String createStatement = String.format("CREATE TABLE IF NOT EXISTS %s (%s);", tableName, columnDefinition);
            commands.add(createStatement);
        }

        return commands;
    }

    public static Properties loadProperties() throws IOException
    {
        Properties properties;

        Path currentPath = Paths.get(System.getProperty("user.dir"));
        Path filePath = currentPath.resolve("config/properties.config");

        try (InputStream input = new FileInputStream(String.valueOf(filePath.toAbsolutePath())))
        {
            properties = loadProperties(input);
        }

        return properties;
    }

    public static Properties loadProperties(InputStream fileStream) throws IOException
    {
        Properties properties = new Properties();
        properties.load(fileStream);
        fileStream.close();

        return properties;
    }
}
