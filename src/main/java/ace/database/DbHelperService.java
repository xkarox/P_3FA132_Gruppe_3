package ace.database;

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
    private List<IDbItem> _tables = new ArrayList<IDbItem>();

    public DbHelperService(){ }

    public DbHelperService(List<IDbItem> tables){
        this._tables = tables;
    }

    public List<String> createSqlTableSchemaCommands()
    {
        List<String> commands = new ArrayList<String>();

        for (IDbItem table : _tables)
        {
            String columnDefinition = table.getSerializedStructure();
            String tableName = table.getSerializedTableName();

            String createStatement = "CREATE TABLE "
                    + tableName
                    + " ("
                    + columnDefinition
                    + ");";

            commands.add(createStatement);
        }

        return commands;
    }

    public static Properties loadProperties()
    {
        Properties properties;

        Path currentPath = Paths.get(System.getProperty("user.dir"));
        Path filePath = currentPath.resolve("src\\main\\java\\resources\\properties.config");

        try (InputStream input = new FileInputStream(String.valueOf(filePath.toAbsolutePath()))) {
            properties = loadProperties(input);
        }
        catch (IOException e)
        {
            throw new RuntimeException(e);
        }

        return properties;
    }

    public static Properties loadProperties(InputStream fileStream)
    {
        Properties properties = new Properties();

        try
        {
            properties.load(fileStream);
            fileStream.close();
        }
        catch (IOException e)
        {
            throw new RuntimeException(e);
        }

        return properties;
    }
}
