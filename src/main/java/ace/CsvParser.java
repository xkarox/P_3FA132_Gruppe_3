package ace;

import ace.model.classes.Customer;

import java.io.File;
import java.util.*;

public class CsvParser
{
    public enum FileType
    {
        customerFileType,
        readingFileType
    }

    private File _csvFile;
    private CsvFormatter _csvFormatter;
    private static final String[] _REQUIRED_CUSTOMER_HEADER = {"Anrede", "Vorname", "Nachname"};


    public CsvParser(File csvFile)
    {
        this._csvFile = csvFile;
        this._csvFormatter = new CsvFormatter();
    }

    public List<Dictionary<String, String>> getAllValues()
    {
        this._csvFile = this._csvFormatter.formatCsv(this._csvFile);

        try (Scanner scanner = new Scanner(this._csvFile))
        {
            while (scanner.hasNextLine())
            {
                String line = scanner.nextLine();
                System.out.println(line);
            }
        } catch (Exception e)
        {
            e.printStackTrace();
        }
        return null;
    }

    public String[] getHeader()
    {
        try (Scanner scanner = new Scanner(this._csvFile))
        {
            if (scanner.hasNextLine())
            {
                String line = scanner.nextLine();
                boolean containsRequiredCustomerHeader = true;
                for (String s : _REQUIRED_CUSTOMER_HEADER)
                {
                    if (!line.contains(s))
                    {
                        containsRequiredCustomerHeader = false;
                        break;
                    }
                }
                if (containsRequiredCustomerHeader)
                {
                    String[] header = line.split(",");
                    return header;
                } else
                {
                    boolean foundSeparator = false;
                    while (scanner.hasNextLine())
                    {
                        String currentLine = scanner.nextLine();
                        if (currentLine.equals(";;"))
                        {
                            foundSeparator = true;
                            continue;
                        }
                        if (foundSeparator)
                        {
                            currentLine = currentLine.replace("\"", "");
                            String[] header = currentLine.split(";");
                            return header;
                        }
                    }

                }
            }
        } catch (Exception e)
        {
            e.printStackTrace();
        }
        return new String[0];
    }

    public void readCsvFile()
    {
        try (Scanner scanner = new Scanner(this._csvFile))
        {
            switch (this.checkFileType(scanner.nextLine()))
            {
                case FileType.customerFileType:
                    List<String> templateHeader = new ArrayList<>();
                    List<List<String>> data = new ArrayList<>();
                    if (scanner.hasNextLine())
                    {
                        scanner.nextLine();
                    }
                    while (scanner.hasNextLine())
                    {
                        String line = scanner.nextLine();
                        String[] values = line.split(",");
                        Customer customer = new Customer();
                        // customer = values[0];
                        System.out.println(Arrays.toString(values));
                    }

                    break;
                case FileType.readingFileType:

                    break;
                case null:
                    break;
            }
        } catch (Exception e)
        {

        }
    }

    private FileType checkFileType(String firstLine)
    {
        if (firstLine.startsWith("UUID"))
        {
            return FileType.customerFileType;
        } else if (firstLine.startsWith("\"Kunde\""))
        {
            return FileType.readingFileType;
        }
        return null;
    }

    private File getCsvFile()
    {
        return this._csvFile;
    }
}
