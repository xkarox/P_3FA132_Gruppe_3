package dev.server;

import dev.hv.CsvParser;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.io.File;
import java.util.Arrays;

@SpringBootApplication
public class Main {
	public static void main(String[] args)
    {
		File wasserTest = new File("src/main/resources/wasser.csv");
		File kundeTest = new File("src/main/resources/kunden_utf8.csv");
		CsvParser csv = new CsvParser(kundeTest);
		System.out.println(csv.getValues());
		Server.startServer("{{ DatabaseUrl }}");
	}
}
