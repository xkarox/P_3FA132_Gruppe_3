package dev.hv.database;

import dev.hv.model.classes.Customer;
import dev.hv.model.classes.Reading;
import dev.hv.model.interfaces.IDbItem;

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
        return createSqlTableSchemaCommands(new ArrayList<>());
    }

    public List<String> createSqlTableSchemaCommands(List<IDbItem> additionalTables)
    {
        List<String> commands = new ArrayList<>();

        List<IDbItem> allTables = _tables;
        allTables.addAll(additionalTables);

        for (IDbItem table : allTables)
        {
            String columnDefinition = table.getSerializedStructure();
            String tableName = table.getSerializedTableName();
            String createStatement = String.format("CREATE TABLE IF NOT EXISTS %s (%s);", tableName, columnDefinition);
            commands.add(createStatement);
        }

        return commands;
    }

    // Req. Nr.: 17
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
