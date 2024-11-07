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
    private FileType _csvFileType;
    private static final String[] _REQUIRED_CUSTOMER_HEADER = {"Anrede", "Vorname", "Nachname"};


    public CsvParser(File csvFile)
    {
        this._csvFormatter = new CsvFormatter();
        this._csvFile = csvFile;
        this._csvFile = this._csvFormatter.formatFile(this._csvFile);
        this.setFileType();
    }

    public ArrayList<Map<String, String>> getValues()
    {
        ArrayList<Map<String, String>> result = new ArrayList<>();
        try (Scanner scanner = new Scanner(this._csvFile))
        {
            String previousLine = "";
            boolean valuesSectionStarted = false;
            ArrayList<String> headers = new ArrayList<>();
            while (scanner.hasNextLine())
            {
                String line = scanner.nextLine().trim();
                if (valuesSectionStarted)
                {
                    String[] data = line.split(",");
                }
                if (isHeader(line, previousLine))
                {
                    valuesSectionStarted = true;
                    continue;
                }
                previousLine = line;

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
                if (this._csvFileType == FileType.customerFileType)
                {
                    String[] header = line.split(",");
                    return header;
                }
                if (this._csvFileType == FileType.readingFileType)
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

    public ArrayList<Map<String, String>> getMetaData()
    {
        ArrayList<Map<String, String>> metaDataList = new ArrayList<>();
        if (this._csvFileType == FileType.readingFileType)
        {
            try (Scanner scanner = new Scanner(this._csvFile))
            {
                while (scanner.hasNextLine())
                {
                    String line = scanner.nextLine();

                    if (line.contains(";;"))
                    {
                        break;
                    }
                    line = line.replace("\"", "");
                    String[] parts = line.split(";");

                    if (parts.length == 2)
                    {
                        Map<String, String> metaData = new HashMap<>();
                        metaData.put(parts[0], parts[1]);
                        metaDataList.add(metaData);
                    }
                }

            } catch (Exception e)
            {
                e.printStackTrace();
            }

        } else if (this._csvFileType == FileType.customerFileType)
        {
            return metaDataList;
        }
        return metaDataList;
    }

    private boolean isHeader(String line, String previousLine)
    {
        for (String s : _REQUIRED_CUSTOMER_HEADER)
        {
            if (!line.contains(s))
            {
                return false;
            }
        }
        return previousLine.equals(";;");
    }

    private void setFileType()
    {
        try (Scanner scanner = new Scanner(this._csvFile))
        {
            if (scanner.hasNextLine())
            {
                String line = scanner.nextLine();
                boolean requiredCustomerAttributes = true;
                for (String s : _REQUIRED_CUSTOMER_HEADER)
                {
                    if (!line.contains(s))
                    {
                        requiredCustomerAttributes = false;
                        break;
                    }
                }
                if (requiredCustomerAttributes)
                {
                    this._csvFileType = FileType.customerFileType;
                } else
                {
                    this._csvFileType = FileType.readingFileType;
                }
            }
        } catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    public FileType getFileType()
    {
        return this._csvFileType;
    }

}
