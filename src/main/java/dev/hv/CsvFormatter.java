package dev.hv;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Scanner;

public class CsvFormatter
{

    public File formatFile(File csvFile, char separator)
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
        } catch (IOException e)
        {
            throw new RuntimeException(e);
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
