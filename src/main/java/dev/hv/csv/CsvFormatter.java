package dev.hv.csv;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Scanner;

public class CsvFormatter
{

    public File formatFile(File csvFile, String separator) throws IOException
    {
        File formattedFile = new File(csvFile.getParent(), "formatted_" + csvFile.getName());

        try (Scanner scanner = new Scanner(csvFile);
             FileWriter writer = new FileWriter(formattedFile))
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
        catch (Exception e) {
          e.printStackTrace();
        }

        return formattedFile;
    }
}
