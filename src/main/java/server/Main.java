package server;
import ace.CsvHelper;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.io.File;

@SpringBootApplication
public class Main {

	public static void main(String[] args)
	{
		String filePathReading = "src/main/resources/heizung.csv";
		String filePathCustomer = "src/main/resources/kunden_utf8.csv";
		File fileCustomer = new File(filePathCustomer);
		File fileReading = new File(filePathReading);
		CsvHelper csv = new CsvHelper(fileCustomer);
		csv.readCsvFile();
		// Server.startServer("{{ DatabaseUrl }}");
	}
}

