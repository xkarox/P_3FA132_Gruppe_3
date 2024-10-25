import ace.database.DatabaseConnection;
import ace.database.services.CustomerService;
import ace.database.services.ReadingService;
import ace.model.classes.Customer;
import ace.model.classes.Reading;
import ace.model.interfaces.ICustomer.Gender;
import ace.model.interfaces.IDbItem;
import ace.model.interfaces.IReading.KindOfMeter;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class Main {

	public static void main(String[] args)
	{
		connectDb();
	}

	// Just for testing and temp use
	public static void connectDb(){
		List<IDbItem> tables = new ArrayList<IDbItem>(){};
		tables.add(new Customer());
		tables.add(new Reading());

		DatabaseConnection dbConnection = new DatabaseConnection(tables);
		dbConnection.openConnection();
		dbConnection.removeAllTables();
		dbConnection.createAllTables();

		CustomerService customerService = new CustomerService(dbConnection);
		ReadingService readingService = new ReadingService(dbConnection);

		Customer testCustomer = new Customer(UUID.randomUUID(), "firstName", "lastName", LocalDate.now(), Gender.M);
		Reading testReading = new Reading(UUID.randomUUID(), "Kommentar", testCustomer.getId(), LocalDate.now(), KindOfMeter.WASSER, 133.03, "11", true);
		// customerService.add(testCustomer);
		readingService.add(testReading);

		// var customerData = dbConnection.getAllObjectsFromDbTable(new Customer());
		// var readingData = dbConnection.getAllObjectsFromDbTable(new Reading());
	}
}
