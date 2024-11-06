package server;
import ace.CsvParser;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;

@SpringBootApplication
public class Main {

	public static void main(String[] args)
	{
		String filePathReading = "src/main/resources/heizung.csv";
		String filePathCustomer = "src/main/resources/kunden_utf8.csv";
		String filePathTest = "src/main/resources/test.csv";

		File fileCustomer = new File(filePathCustomer);
		File fileReading = new File(filePathReading);
		File fileTest = new File(filePathTest);


		CsvParser csv = new CsvParser(fileReading);
		// System.out.println(csv.getFileType());
		// System.out.println(Arrays.toString(csv.getHeader()));
		ArrayList<Map<String, String>> metaData = csv.getMetaData();

		// Ausgabe der Meta-Daten (direkt die ArrayList mit den Maps)
		System.out.println(metaData);
		// Server.startServer("{{ DatabaseUrl }}");
	}
}

