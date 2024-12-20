package dev.server;

import dev.hv.csv.CsvParser;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;
import java.util.Map;


@SpringBootApplication
public class Main
{
    public static void main(String[] args)
    {
        Server.startServer("{{ DatabaseUrl }}");
        // Pfad zur CSV-Datei
        String readingFilePath = "src/main/resources/heizung.csv";
        String customerFilePath = "src/main/resources/kunden_utf8.csv";

        try (FileInputStream fileInputStream = new FileInputStream(readingFilePath))
        {
            CsvParser parser = new CsvParser(fileInputStream);

            System.out.println("Separator: " + parser.getSeparator());

            System.out.println("Header:");
            for (String header : parser.getHeader())
            {
                System.out.print(header + " | ");
            }
            System.out.println();

            System.out.println("Metadaten:");
            for (Map<String, String> metaData : parser.getMetaData())
            {
                for (Map.Entry<String, String> entry : metaData.entrySet())
                {
                    System.out.println(entry.getKey() + ": " + entry.getValue());
                }
            }

            System.out.println("Werte:");
            for (List<String> row : parser.getValues())
            {
                for (String value : row)
                {
                    System.out.print(value + " | ");
                }
                System.out.println();
            }

        } catch (IOException e)
        {
            e.printStackTrace();
        }
    }


}
