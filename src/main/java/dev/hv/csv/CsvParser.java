package dev.hv.csv;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class CsvParser
{
    private File _csvFile;

    public CsvParser(File csvFile)
    {
        CsvFormatter formatter = new CsvFormatter();
        this._csvFile = csvFile;
        this._csvFile = formatter.formatFile(csvFile, this.getSeparator());
    }

    public Iterable<List<String>> getValues() {
        List<List<String>> valuesList = new ArrayList<>();
        try (Scanner scanner = new Scanner(this._csvFile)) {
            if (this.getSeparator() == ',') {
                if (scanner.hasNextLine()) {
                    scanner.nextLine();
                }

                while (scanner.hasNextLine()) {
                    String line = scanner.nextLine();
                    List<String> values = Arrays.stream(line.split(",")).toList();
                    valuesList.add(values);

                }
            } else if (this.getSeparator() == ';') {
                for (int i = 0; i < 3 && scanner.hasNextLine(); i++) {
                    scanner.nextLine();
                }

                while (scanner.hasNextLine()) {
                    String line = scanner.nextLine();
                    List<String> values = Arrays.stream(line.split(";")).toList();
                    valuesList.add(values);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return valuesList;
    }


    public Iterable<String> getHeader() {
        List<String> headerList = new ArrayList<>();

        try (Scanner scanner = new Scanner(this._csvFile)) {
            if (this.getSeparator() == ',') {
                if (scanner.hasNextLine()) {
                    String line = scanner.nextLine();
                    String[] headers = line.split(",");
                    Collections.addAll(headerList, headers);
                }
            } else if (this.getSeparator() == ';') {
                int lineNumber = 0;
                while (scanner.hasNextLine()) {
                    String line = scanner.nextLine();
                    lineNumber++;

                    if (lineNumber == 3) {
                        line = line.replace("\"", "");
                        String[] headers = line.split(";");
                        Collections.addAll(headerList, headers);
                        break;
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return headerList;
    }


    public Iterable<Map<String, String>> getMetaData() {
        List<Map<String, String>> metaDataList = new ArrayList<>();
        int lineCount = 0;

        try (Scanner scanner = new Scanner(this._csvFile)) {
            if (this.getSeparator() == ';') {
                while (scanner.hasNextLine() && lineCount < 2) {
                    String line = scanner.nextLine();
                    lineCount++;

                    line = line.replace("\"", "");
                    String[] parts = line.split(";");

                    if (parts.length == 2) {
                        Map<String, String> dataMap = new HashMap<>();
                        dataMap.put(parts[0], parts[1]);
                        metaDataList.add(dataMap);
                    }
                }
            } else if (this.getSeparator() == ',') {
                return metaDataList;
            }
        } catch (Exception e) {
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
