package ace;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Scanner;

public class CsvFormatter
{
    private File _csvFile;

    private File removeEmptyLines(File csvFile)
    {
        File tempFile = new File("tempFile.csv");

        try (Scanner scanner = new Scanner(csvFile);
             FileWriter writer = new FileWriter(tempFile))
        {

            while (scanner.hasNextLine())
            {
                String line = scanner.nextLine();

                if (line.trim().isEmpty())
                {
                    continue;
                }
                writer.write(line + System.lineSeparator());
            }
           return tempFile;
        } catch (IOException e)
        {
            throw new RuntimeException(e);
        }
    }

    private char getSeparator(File csvFile)
    {
       try (Scanner scanner = new Scanner(csvFile)) {
           if (scanner.hasNextLine()){
               String line = scanner.nextLine();
               if (line.contains(",")) {
                   return ',';
               }
               else if (line.contains(";")) {
                   return ';';
               }
           }
       }
       catch (IOException e) {
           e.printStackTrace();
       }
       return '"';
    }


    public File formatFile(File csvFile)
    {
        this._csvFile = csvFile;
        this._csvFile = this.removeEmptyLines(csvFile);
        char separator = this.getSeparator(this._csvFile);

        // test
        try (Scanner scanner = new Scanner(this._csvFile))
        {
            while (scanner.hasNextLine()) {
                System.out.println(separator);
            }
        }
        catch (Exception e) {

        }

        return this._csvFile;

    }
}
