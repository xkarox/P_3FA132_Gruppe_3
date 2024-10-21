package ace.database;

import ace.model.interfaces.IDbItem;
import java.util.ArrayList;
import java.util.List;

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
}
