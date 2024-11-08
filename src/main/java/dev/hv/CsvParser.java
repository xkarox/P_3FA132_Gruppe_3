package dev.hv;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class CsvParser
{
    private File _csvFile;

    public CsvParser(File csvFile)
    {
        CsvFormatter formatter = new CsvFormatter(csvFile);
        this._csvFile = csvFile;
        this._csvFile = formatter.formatFile(csvFile, this.getSeparator());
    }

    public ArrayList<Map<String, String>> getValues()
    {
        ArrayList<Map<String, String>> valuesList = new ArrayList<>();
        try (Scanner scanner = new Scanner(this._csvFile))
        {
            String[] header = this.getHeader();

            if (this.getSeparator() == ',')
            {
                if (scanner.hasNextLine())
                {
                    scanner.nextLine();
                }

                while (scanner.hasNextLine())
                {
                    String line = scanner.nextLine();
                    String[] values = line.split(",");

                    Map<String, String> dataMap = new HashMap<>();

                    for (int i = 0; i < header.length; i++)
                    {
                        if (i < values.length)
                        {
                            dataMap.put(header[i], values[i].isEmpty() ? "" : values[i]);
                        } else
                        {
                            dataMap.put(header[i], "");
                        }
                    }
                    valuesList.add(dataMap);
                }
            } else if (this.getSeparator() == ';')
            {
                for (int i = 0; i < 4 && scanner.hasNextLine(); i++)
                {
                    scanner.nextLine();
                }
                while (scanner.hasNextLine())
                {
                    String line = scanner.nextLine();
                    line = line.replace("\"", "");
                    String[] values = line.split(";");

                    Map<String, String> dataMap = new HashMap<>();

                    for (int i = 0; i < header.length; i++)
                    {
                        if (i < values.length)
                        {
                            dataMap.put(header[i], values[i].isEmpty() ? "" : values[i]);
                        } else
                        {
                            dataMap.put(header[i], "");
                        }
                    }
                    valuesList.add(dataMap);
                }
            }
        } catch (Exception e)
        {
            e.printStackTrace();
        }
        return valuesList;
    }

    public String[] getHeader()
    {
        try (Scanner scanner = new Scanner(this._csvFile))
        {
            if (this.getSeparator() == ',')
            {
                if (scanner.hasNextLine())
                {
                    String line = scanner.nextLine();
                    return line.split(",");
                }
            } else if (this.getSeparator() == ';')
            {
                int lineNumber = 0;
                while (scanner.hasNextLine())
                {
                    String line = scanner.nextLine();
                    lineNumber++;

                    if (lineNumber == 3)
                    {
                        line = line.replace("\"", "");
                        return line.split(";");
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
        int lineCount = 0;
        try (Scanner scanner = new Scanner(this._csvFile))
        {
            if (this.getSeparator() == ';')
            {
                while (scanner.hasNextLine() && lineCount < 2)
                {
                    String line = scanner.nextLine();
                    lineCount++;

                    line = line.replace("\"", "");
                    String[] parts = line.split(";");

                    if (parts.length == 2)
                    {
                        Map<String, String> dataMap = new HashMap<>();
                        dataMap.put(parts[0], parts[1]); // Füge Schlüssel-Wert-Paar hinzu
                        metaDataList.add(dataMap);       // Map zur ArrayList hinzufügen
                    }
                }
            } else if (this.getSeparator() == ',')
            {
                return metaDataList;
            }
        } catch (Exception e)
        {
            e.printStackTrace();
        }
        return metaDataList;
    }

    public char getSeparator()
    {
        try (Scanner scanner = new Scanner(this._csvFile))
        {
            if (scanner.hasNextLine())
            {
                String line = scanner.nextLine();
                if (line.contains(","))
                {
                    return ',';
                } else if (line.contains(";"))
                {
                    return ';';
                }
            }
        } catch (IOException e)
        {
            e.printStackTrace();
        }
        return '"';
    }

}
