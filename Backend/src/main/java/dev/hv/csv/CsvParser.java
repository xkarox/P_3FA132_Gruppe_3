package dev.hv.csv;

import dev.hv.database.services.CustomerService;
import dev.hv.database.services.ReadingService;
import dev.hv.model.ICustomer;
import dev.hv.model.IReading;
import dev.hv.model.classes.Customer;
import dev.hv.model.classes.Reading;
import dev.provider.ServiceProvider;

import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class CsvParser
{
    enum Separator
    {
        READING_SEPARATOR(";"),
        CUSTOMER_SEPARATOR(",");

        private final String description;

        Separator(String description)
        {
            this.description = description;
        }

        @Override
        public String toString()
        {
            return description;
        }
    }

    enum LineNumbers
    {
        LINES_UNTIL_VALUES_READING(3),
        LINES_UNTIL_VALUES_CUSTOMER(1),
        METADATA_READING_NUMBER_OF_VALUES(2);

        private final int number;

        LineNumbers(int number)
        {
            this.number = number;
        }

        public int getNumber()
        {
            return number;
        }
    }

    private String csvContent;
    private CustomerService cs;
    private ReadingService rs;

    public CsvParser() throws IOException
    {

    }

    public void setCsvContent(String csvContent)
    {
        this.csvContent = csvContent;
    }

    public String getCsvContent() {
        return this.csvContent;
    }

    public Iterable<List<String>> getDefaultReadingValues()
    {
        CsvFormatter formatter = new CsvFormatter();
        setCsvContent(formatter.formatReadingCsv(this.getCsvContent()));
        List<List<String>> valuesList = new ArrayList<>();
        Scanner scanner = new Scanner(this.csvContent);

        int linesToSkip = 3;
        while (scanner.hasNextLine() && linesToSkip > 0) {
            scanner.nextLine();
            linesToSkip--;
        }
        while (scanner.hasNextLine())
        {
            String line = scanner.nextLine();
            List<String> values = Arrays.stream(line.split(Separator.READING_SEPARATOR.toString())).toList();
            valuesList.add(values);
        }

        return valuesList;
    }

    public Iterable<List<String>> getCustomReadingValues() {
        CsvFormatter formatter = new CsvFormatter();
        setCsvContent(formatter.formatReadingCsv(this.getCsvContent()));
        List<List<String>> valuesList = new ArrayList<>();
        Scanner scanner = new Scanner(this.csvContent);

        if (scanner.hasNextLine()) {
            scanner.nextLine();
        }

        while (scanner.hasNextLine()) {
            String line = scanner.nextLine(); // Zeile einlesen
            List<String> values = Arrays.stream(line.split(Separator.READING_SEPARATOR.toString())).toList();
            valuesList.add(values);
        }
        return valuesList;

    }

    public Iterable<List<String>> getCustomerValues()
    {
        CsvFormatter formatter = new CsvFormatter();
        setCsvContent(formatter.formatCustomerCsv(this.getCsvContent()));
        List<List<String>> valuesList = new ArrayList<>();
        Scanner scanner = new Scanner(this.csvContent);

        if (scanner.hasNextLine())
        {
            scanner.nextLine();
        }

        while (scanner.hasNextLine())
        {
            String line = scanner.nextLine();
            List<String> values = Arrays.stream(line.split(Separator.READING_SEPARATOR.toString())).toList();
            valuesList.add(values);
        }

        return valuesList;
    }


    public Iterable<String> getReadingHeader()
    {
        CsvFormatter formatter = new CsvFormatter();
        setCsvContent(formatter.formatReadingCsv(this.getCsvContent()));
        List<String> headerList = new ArrayList<>();
        Scanner scanner = new Scanner(this.csvContent);

        String separatorRegex = "^[;,]+$";
        String line;

        while (scanner.hasNextLine())
        {
            line = scanner.nextLine().replace("\"", "");
            String[] headers = line.split("[;,]");
            if (headers.length >= 3)
            {
                Collections.addAll(headerList, headers);
                break;
            }
            if (!line.matches(separatorRegex))
            {
                scanner.nextLine();
            }
        }
        return headerList;
    }

    public Iterable<String> getCustomerHeader()
    {
        CsvFormatter formatter = new CsvFormatter();
        setCsvContent(formatter.formatCustomerCsv(this.getCsvContent()));
        List<String> headerList = new ArrayList<>();
        Scanner scanner = new Scanner(this.csvContent);
        if (scanner.hasNextLine())
        {
            String line = scanner.nextLine();
            String[] headers = line.split("[;,]");
            Collections.addAll(headerList, headers);
        }
        return headerList;
    }


    public Iterable<Map<String, String>> getMetaData()
    {
        List<Map<String, String>> metaDataList = new ArrayList<>();
        int lineCount = 0;
        Scanner scanner = new Scanner(this.csvContent);

        if (Objects.equals(this.getSeparator(), Separator.READING_SEPARATOR.toString()))
        {
            while (scanner.hasNextLine() && lineCount < LineNumbers.METADATA_READING_NUMBER_OF_VALUES.getNumber())
            {
                String line = scanner.nextLine();
                lineCount++;

                line = line.replace("\"", "");
                String[] parts = line.split(Separator.READING_SEPARATOR.toString());

                if (parts.length == LineNumbers.METADATA_READING_NUMBER_OF_VALUES.getNumber())
                {
                    Map<String, String> dataMap = new HashMap<>();
                    dataMap.put(parts[0], parts[1]);
                    metaDataList.add(dataMap);
                }
            }
        }
        return metaDataList;
    }


    public String getSeparator()
    {
        return ";";
    }

    public String createReadingsByKindOfMeter(IReading.KindOfMeter kindOfMeter) throws SQLException, IOException, ReflectiveOperationException
    {
        String readingHeader = "Datum;Zählerstand;Kommentar;KundenId;Zählerart;ZählerstandId;Ersatz\n";
        String readingValues = "";
        this.rs = ServiceProvider.Services.getReadingService();

        List<Reading> allReadings = rs.getAll();
        List<Reading> typeReadings = new ArrayList<>();
        switch (kindOfMeter)
        {
            case IReading.KindOfMeter.WASSER:

                for (Reading r : allReadings)
                {
                    if (r.getKindOfMeter() == IReading.KindOfMeter.WASSER)
                    {
                        typeReadings.add(r);
                    }
                }
                break;
            case IReading.KindOfMeter.STROM:

                for (Reading r : allReadings)
                {
                    if (r.getKindOfMeter() == IReading.KindOfMeter.STROM)
                    {
                        typeReadings.add(r);
                    }
                }
                break;
            case IReading.KindOfMeter.HEIZUNG:

                for (Reading r : allReadings)
                {
                    if (r.getKindOfMeter() == IReading.KindOfMeter.HEIZUNG)
                    {
                        typeReadings.add(r);
                    }
                }
                break;
            case IReading.KindOfMeter.UNBEKANNT:

                for (Reading r : allReadings)
                {
                    if (r.getKindOfMeter() == IReading.KindOfMeter.UNBEKANNT)
                    {
                        typeReadings.add(r);
                    }
                }
                break;
        }
        for (int i = 0; i < typeReadings.size(); i++)
        {
            if (typeReadings.get(i).getDateOfReading() != null)
            {
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy");
                readingValues += typeReadings.get(i).getDateOfReading().format(formatter) + ";";
            } else
            {
                readingValues += ";";
            }
            if (typeReadings.get(i).getMeterCount() != null)
            {
                readingValues += typeReadings.get(i).getMeterCount() + ";";
            } else
            {
                readingValues += ";";
            }
            if (typeReadings.get(i).getComment() != null)
            {
                readingValues += typeReadings.get(i).getComment() + ";";
            } else
            {
                readingValues += ";";
            }
            if (typeReadings.get(i).getCustomerId() != null)
            {
                readingValues += typeReadings.get(i).getCustomerId() + ";";
            } else
            {
                readingValues += ";";
            }
            if (typeReadings.get(i).getKindOfMeter() != null)
            {
                readingValues += typeReadings.get(i).getKindOfMeter() + ";";
            } else
            {
                readingValues += ";";
            }
            if (typeReadings.get(i).getMeterId() != null)
            {
                readingValues += typeReadings.get(i).getMeterId() + ";";
            } else
            {
                readingValues += ";";
            }
            if (typeReadings.get(i).getSubstitute() != null)
            {
                readingValues += typeReadings.get(i).getSubstitute() + ";\n";
            }
            readingValues = readingValues.replace(',', '.');
        }
        String readingCsv = "";
        readingCsv = readingHeader + readingValues;
        return readingCsv;
    }

}
