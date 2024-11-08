package ace;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Scanner;

public class CsvFormatter
{
    private File _csvFile;
    private String _location;

    public CsvFormatter(File csvFile)
    {
        this._location = csvFile.getPath();
    }

    private File removeEmptyLines(File csvFile, char separator)
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

    public File formatFile(File csvFile, char separator)
    {
        this._csvFile = csvFile;
        this._csvFile = this.removeEmptyLines(csvFile, separator);
        return this._csvFile;
    }
}
