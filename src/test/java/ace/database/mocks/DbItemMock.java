package ace.database.mocks;

import ace.model.interfaces.IDbItem;

public class DbItemMock implements IDbItem
{
    private final String _structure;
    private final String _tableName;

    public DbItemMock(String structure, String tableName){
        this._structure = structure;
        this._tableName = tableName;
    }

    @Override
    public String getSerializedStructure()
    {
        return this._structure;
    }

    @Override
    public String getSerializedTableName()
    {
        return this._tableName;
    }
}
