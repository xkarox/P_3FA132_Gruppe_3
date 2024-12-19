package dev.hv.csv;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Scanner;

public class CsvFormatter
{

    public File formatFile(File csvFile, char separator) throws IOException
    {
        File tempFile = new File(csvFile.getParent(), "temp.csv");

        try (Scanner scanner = new Scanner(csvFile);
             FileWriter writer = new FileWriter(tempFile))
        {
            String regex = "^" + separator + "+$";

            while (scanner.hasNextLine())
            {
                String line = scanner.nextLine();

                if (line.trim().isEmpty() || line.matches(regex))
                {
                    continue;
                }
                writer.write(line + System.lineSeparator());
            }
        }

        if (csvFile.delete())
        {
            if (!tempFile.renameTo(csvFile))
            {
                throw new RuntimeException("Error at replacing of old file");
            }
        } else
        {
            throw new RuntimeException("Error at deleting old file");
        }
        return csvFile;
    }
}
