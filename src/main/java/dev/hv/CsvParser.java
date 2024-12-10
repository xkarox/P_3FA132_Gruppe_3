package dev.hv;

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

    public Iterable<Map<String, String>> getValues() {
        List<Map<String, String>> valuesList = new ArrayList<>();
        try (Scanner scanner = new Scanner(this._csvFile)) {
            Iterable<String> headerIterable = this.getHeader();
            List<String> header = new ArrayList<>();
            headerIterable.forEach(header::add);

            if (this.getSeparator() == ',') {
                if (scanner.hasNextLine()) {
                    scanner.nextLine();
                }

                while (scanner.hasNextLine()) {
                    String line = scanner.nextLine();
                    String[] values = line.split(",");

                    Map<String, String> dataMap = new HashMap<>();
                    for (int i = 0; i < header.size(); i++) {
                        if (i < values.length) {
                            dataMap.put(header.get(i), values[i].isEmpty() ? "" : values[i]);
                        } else {
                            dataMap.put(header.get(i), "");
                        }
                    }
                    valuesList.add(dataMap);
                }
            } else if (this.getSeparator() == ';') {
                for (int i = 0; i < 3 && scanner.hasNextLine(); i++) {
                    scanner.nextLine();
                }

                while (scanner.hasNextLine()) {
                    String line = scanner.nextLine();
                    line = line.replace("\"", "");
                    String[] values = line.split(";");

                    Map<String, String> dataMap = new HashMap<>();
                    for (int i = 0; i < header.size(); i++) {
                        if (i < values.length) {
                            dataMap.put(header.get(i), values[i].isEmpty() ? "" : values[i]);
                        } else {
                            dataMap.put(header.get(i), "");
                        }
                    }
                    valuesList.add(dataMap);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return valuesList; // Gibt ein Iterable zurück, da List das Interface Iterable implementiert
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
