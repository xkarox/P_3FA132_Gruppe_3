import ace.database.DatabaseConnection;
import ace.model.classes.Customer;
import ace.model.classes.Reading;
import ace.model.interfaces.IDbItem;

import java.util.ArrayList;
import java.util.List;

public class Main {

	public static void main(String[] args)
	{
		List<IDbItem> tables = new ArrayList<IDbItem>(){};
		tables.add(new Customer());
		tables.add(new Reading());

		DatabaseConnection dbConnection = new DatabaseConnection(tables);
		dbConnection.openConnection();
		dbConnection.createAllTables();
	}
}
