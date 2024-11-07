package ace;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Scanner;

public class CsvFormatter
{
    private File _csvFile;

    public File formatCsv(File csvFile)
    {
        File formattedFile = new File("formattedFile");
        try (Scanner scanner = new Scanner(csvFile))
        {
            PrintWriter writer = new PrintWriter(new FileWriter(formattedFile));

            while (scanner.hasNextLine())
            {
                String line = scanner.nextLine();

                if (line.trim().isEmpty())
                {
                    continue;
                }
                writer.println(line);
            }
            writer.flush();
        } catch (Exception e)
        {
            e.printStackTrace();
        }
        return formattedFile;
    }

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

    private char getSeparator()
    {
        return 'a';
    }

    private void replaceSeparator()
    {

    }

    public File formatFile(File csvFile)
    {
        this._csvFile = csvFile;
        this._csvFile = this.removeEmptyLines(csvFile);
        // char separator = this.getSeparator();
        // this.replaceSeparator();

        // test
        try (Scanner scanner = new Scanner(this._csvFile))
        {
            while (scanner.hasNextLine()) {
                System.out.println(scanner.nextLine());
            }
        }
        catch (Exception e) {

        }

        return this._csvFile;

    }
}
