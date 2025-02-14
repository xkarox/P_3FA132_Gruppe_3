package dev.hv.csv;

import dev.hv.database.services.CustomerService;
import dev.hv.database.services.ReadingService;
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

    public void setCsvContent(String csvContent) {
        this.csvContent = csvContent;
    }

    public Iterable<List<String>> getValues() {
        List<List<String>> valuesList = new ArrayList<>();
        Scanner scanner = new Scanner(this.csvContent);

        if (Objects.equals(this.getSeparator(), Separator.CUSTOMER_SEPARATOR.toString())) {
            if (scanner.hasNextLine()) {
                scanner.nextLine();
            }

            while (scanner.hasNextLine()) {
                String line = scanner.nextLine();
                List<String> values = Arrays.stream(line.split(Separator.CUSTOMER_SEPARATOR.toString())).toList();
                valuesList.add(values);
            }
        } else if (Objects.equals(this.getSeparator(), Separator.READING_SEPARATOR.toString())) {
            for (int i = 0; i < LineNumbers.LINES_UNTIL_VALUES_READING.getNumber() && scanner.hasNextLine(); i++) {
                scanner.nextLine();
            }

            while (scanner.hasNextLine()) {
                String line = scanner.nextLine();
                List<String> values = Arrays.stream(line.split(Separator.READING_SEPARATOR.toString())).toList();
                valuesList.add(values);
            }
        }
        return valuesList;
    }


    public Iterable<String> getHeader() {
        List<String> headerList = new ArrayList<>();
        Scanner scanner = new Scanner(this.csvContent);

        if (Objects.equals(this.getSeparator(), Separator.CUSTOMER_SEPARATOR.toString())) {
            if (scanner.hasNextLine()) {
                String line = scanner.nextLine();
                String[] headers = line.split(Separator.CUSTOMER_SEPARATOR.toString());
                Collections.addAll(headerList, headers);
            }
        } else if (Objects.equals(this.getSeparator(), Separator.READING_SEPARATOR.toString())) {
            int lineNumber = 0;
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine();
                lineNumber++;

                if (lineNumber == LineNumbers.LINES_UNTIL_VALUES_READING.getNumber()) {
                    line = line.replace("\"", "");
                    String[] headers = line.split(Separator.READING_SEPARATOR.toString());
                    Collections.addAll(headerList, headers);
                    break;
                }
            }
        }
        return headerList;
    }


    public Iterable<Map<String, String>> getMetaData() {
        List<Map<String, String>> metaDataList = new ArrayList<>();
        int lineCount = 0;
        Scanner scanner = new Scanner(this.csvContent);

        if (Objects.equals(this.getSeparator(), Separator.READING_SEPARATOR.toString())) {
            while (scanner.hasNextLine() && lineCount < LineNumbers.METADATA_READING_NUMBER_OF_VALUES.getNumber()) {
                String line = scanner.nextLine();
                lineCount++;

                line = line.replace("\"", "");
                String[] parts = line.split(Separator.READING_SEPARATOR.toString());

                if (parts.length == LineNumbers.METADATA_READING_NUMBER_OF_VALUES.getNumber()) {
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
        Scanner scanner = new Scanner(this.csvContent);
        if (scanner.hasNextLine()) {
            String line = scanner.nextLine();
            if (line.contains(Separator.CUSTOMER_SEPARATOR.toString())) {
                return Separator.CUSTOMER_SEPARATOR.toString();
            } else if (line.contains(Separator.READING_SEPARATOR.toString())) {
                return Separator.READING_SEPARATOR.toString();
            }
        }
        return "";
    }

    public String createReadingsCsvFromCustomer(Customer customer) throws SQLException, IOException, ReflectiveOperationException
    {
        this.rs = ServiceProvider.Services.getReadingService();

        String readingValues = "";
        String readingMetaData = "Kunde;" + customer.getId() + "\n" + "Zählernummer;";
        List<Reading> readings = this.rs.getReadingsByCustomerId(customer.getId());
        for (int i = 0; i < readings.size(); i++) {
            if (readings.get(i).getDateOfReading() != null) {
                LocalDate date = LocalDate.parse(readings.get(i).getDateOfReading().toString());
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy");
                String formattedDate = date.format(formatter);
                readingValues += formattedDate;
            }
            readingValues += ";";
            readingValues += readings.get(i).getMeterCount().toString();
            readingValues += ";";
            if (readings.get(i).getComment() != null) {
                readingValues += readings.get(i).getComment();
            }
            else {
                readingValues += "";
            }

            readingValues += "\n";
        }
        readingMetaData += readings.get(0).getMeterId();
        readingMetaData += "\n";

        String readingHeader = "Datum;Zählerstand in m³;Kommentar\n";
        String readingCsv = "";
        readingCsv += readingMetaData + readingHeader + readingValues;
        return readingCsv;
    }

    public String createAllCustomerCsv() throws SQLException, IOException, ReflectiveOperationException
    {
        String customerHeader = "UUID,Anrede,Vorname,Nachname,Geburtsdatum\n";
        String customerValues = "";
        this.cs = ServiceProvider.Services.getCustomerService();

        List<Customer> customers = this.cs.getAll();
        for (int i = 0; i < customers.size(); i++) {
            customerValues += customers.get(i).getId() + ",";
            customerValues += customers.get(i).getGender() + ",";
            customerValues += customers.get(i).getFirstName() + ",";
            customerValues += customers.get(i).getLastName() + ",";
            if (customers.get(i).getBirthDate() != null) {
                customerValues += customers.get(i).getBirthDate() + "\n";
            }
            else {
                customerValues += "\n";
            }

        }
        String customerCsv = "";
        customerCsv = customerHeader + customerValues;
        return customerCsv;
    }

}
