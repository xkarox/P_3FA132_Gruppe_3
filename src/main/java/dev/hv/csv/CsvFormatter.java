package dev.hv.csv;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Scanner;

public class CsvFormatter
{

    public String formatFile(String csvFile)
    {
        StringBuilder formattedContent = new StringBuilder();

        try (Scanner scanner = new Scanner(csvFile))
        {
            String separator = ";";
            String regex = "^" + separator + "+$";

            while (scanner.hasNextLine())
            {
                String line = scanner.nextLine();

                if (line.trim().isEmpty() || line.matches(regex))
                {
                    continue;
                }
                formattedContent.append(line).append(System.lineSeparator());
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }

        return formattedContent.toString();
    }
}
