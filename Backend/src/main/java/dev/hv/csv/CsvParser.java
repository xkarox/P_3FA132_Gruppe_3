package dev.hv.csv;

import dev.hv.Serializer;
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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CsvParser
{
    enum Separator
    {
        READING_SEPARATOR(";");

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

    public String getCsvContent()
    {
        return this.csvContent;
    }

    public Iterable<List<String>> getDefaultReadingValues()
    {
        CsvFormatter formatter = new CsvFormatter();
        setCsvContent(formatter.formatReadingCsv(this.getCsvContent()));
        List<List<String>> valuesList = new ArrayList<>();
        Scanner scanner = new Scanner(this.csvContent);

        int linesToSkip = 3;
        while (scanner.hasNextLine() && linesToSkip > 0)
        {
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

    public Iterable<List<String>> getCustomReadingValues()
    {
        CsvFormatter formatter = new CsvFormatter();
        setCsvContent(formatter.formatReadingCsv(this.getCsvContent()));
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
        String line;

        while (scanner.hasNextLine())
        {
            line = scanner.nextLine().replace("\"", "");
            String[] headers = line.split("[;,]");
            if (headers.length >= 3)
            {
                Collections.addAll(headerList, headers);

            }
            if (scanner.hasNextLine())
            {
                scanner.nextLine();
            }
        }
        return headerList;
    }

    public Iterable<String> getCustomReadingHeader()
    {
        CsvFormatter formatter = new CsvFormatter();
        setCsvContent(formatter.formatReadingCsv(this.getCsvContent()));
        List<String> headerList = new ArrayList<>();
        Scanner scanner = new Scanner(this.csvContent);
        String line;

        if (scanner.hasNextLine())
        {
            line = scanner.nextLine().replace("\"", "");
            String[] headers = line.split("[;,]");
            Collections.addAll(headerList, headers);
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
        return metaDataList;
    }


    public String getSeparator()
    {
        return Separator.READING_SEPARATOR.toString();
    }

    public String createReadingsByKindOfMeter(IReading.KindOfMeter kindOfMeter) throws SQLException, IOException, ReflectiveOperationException
    {
        String readingHeader = "Datum;Zählerstand;Kommentar;KundenId;Zählerart;ZählerstandId;Ersatz\n";
        this.rs = ServiceProvider.Services.getReadingService();

        List<Reading> allReadings = rs.getAll();
        List<Reading> typeReadings = allReadings.stream().filter(e -> e.getKindOfMeter() == kindOfMeter).toList();

        String readingCsv = Serializer.serializeIntoCsv(typeReadings);
        if (readingCsv == null)
        {
            return readingHeader;
        }

        return readingHeader + readingCsv;
    }

    public String createAllCustomerCsv() throws SQLException, IOException, ReflectiveOperationException
    {
        String customerHeader = "UUID;Anrede;Vorname;Nachname;Geburtsdatum\n";
        this.cs = ServiceProvider.Services.getCustomerService();

        List<Customer> customers = this.cs.getAll();
        String customerCsv = Serializer.serializeIntoCsv(customers);

        return customerHeader + customerCsv;
    }

    public List<Reading> createDefaultReadingsFromCsv(boolean heat, boolean water, boolean electricity) throws ReflectiveOperationException, SQLException, IOException
    {
        CustomerService cs = ServiceProvider.Services.getCustomerService();
        List<Reading> readings = new ArrayList<>();
        Iterable<List<String>> defaultReadingValues = getDefaultReadingValues();
        Iterable<Map<String, String>> metaData = getMetaData();
        String meterId = "";

        Iterator<Map<String, String>> iterator = metaData.iterator();
        Map<String, String> customerMetadata = new HashMap<>();
        Map<String, String> meterIdMetaData = new HashMap<>();

        if (iterator.hasNext())
        {
            customerMetadata = iterator.next();

        }

        if (iterator.hasNext())
        {
            meterIdMetaData = iterator.next();
            meterId = meterIdMetaData.get("Zählernummer");

        }

        for (List<String> defaultReadingList : defaultReadingValues)
        {
            Reading reading = new Reading();

            if (cs.getById(UUID.fromString(customerMetadata.get("Kunde"))) != null)
            {
                reading.setCustomer(cs.getById(UUID.fromString(customerMetadata.get("Kunde"))));
            } else
            {
                Customer customer = new Customer(UUID.fromString(customerMetadata.get("Kunde")));
                reading.setCustomer(customer);
            }

            reading.setMeterId(meterIdMetaData.get("Zählernummer"));
            reading.setSubstitute(false);

            if (defaultReadingList.size() > 0)
            {
                DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy");
                reading.setDateOfReading(LocalDate.parse(defaultReadingList.getFirst(), dateTimeFormatter));
            }
            if (defaultReadingList.size() > 1)
            {
                if (water)
                {
                    reading.setKindOfMeter(IReading.KindOfMeter.WASSER);
                } else if (heat)
                {
                    reading.setKindOfMeter(IReading.KindOfMeter.HEIZUNG);
                } else if (electricity)
                {
                    reading.setKindOfMeter(IReading.KindOfMeter.STROM);
                } else
                {
                    reading.setKindOfMeter(IReading.KindOfMeter.UNBEKANNT);
                }
                reading.setMeterCount(Double.parseDouble(defaultReadingList.get(1)));
            }
            if (defaultReadingList.size() > 2)
            {
                Pattern pattern = Pattern.compile("Nummer\\s+(\\S+)");
                Matcher matcher = pattern.matcher(defaultReadingList.get(2));

                if (matcher.find())
                {
                    meterId = matcher.group(1);
                }

                reading.setComment(defaultReadingList.get(2));
            }
            reading.setMeterId(meterId);
            readings.add(reading);
        }
        return readings;
    }

    public List<Reading> createCustomReadingsFromCsv() throws ReflectiveOperationException, SQLException, IOException
    {
        this.cs = ServiceProvider.Services.getCustomerService();
        List<Reading> readings = new ArrayList<>();
        Iterable<List<String>> customReadingValues = getCustomReadingValues();

        for (List<String> customReadingList : customReadingValues)
        {
            Reading reading = new Reading();

            if (customReadingList.size() > 0)
            {
                DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy");
                reading.setDateOfReading(LocalDate.parse(customReadingList.getFirst(), dateTimeFormatter));
            }
            if (customReadingList.size() > 1)
            {
                reading.setMeterCount(Double.parseDouble(customReadingList.get(1)));
            }
            if (customReadingList.size() > 2)
            {
                if (customReadingList.get(2).equals("null")) {
                    reading.setComment(null);
                }
                else {
                    reading.setComment(customReadingList.get(2));
                }

            }
            if (customReadingList.size() > 3)
            {
                if (cs.getById(UUID.fromString(customReadingList.get(3))) != null)
                {
                    reading.setCustomer(cs.getById(UUID.fromString(customReadingList.get(3))));
                } else
                {
                    Customer customer = new Customer(UUID.fromString(customReadingList.get(3)));
                    reading.setCustomer(customer);
                }

            }
            if (customReadingList.size() > 4)
            {
                switch (customReadingList.get(4))
                {
                    case "STROM":
                        reading.setKindOfMeter(IReading.KindOfMeter.STROM);
                        break;
                    case "HEIZUNG":
                        reading.setKindOfMeter(IReading.KindOfMeter.HEIZUNG);
                        break;
                    case "WASSER":
                        reading.setKindOfMeter(IReading.KindOfMeter.WASSER);
                        break;
                    default:
                        reading.setKindOfMeter(IReading.KindOfMeter.UNBEKANNT);
                        break;
                }
            }
            if (customReadingList.size() > 5)
            {
                reading.setMeterId(customReadingList.get(5));
            }
            if (customReadingList.size() > 6)
            {
                String capitalizedBoolean = customReadingList.get(6).substring(0, 1).toUpperCase() + customReadingList.get(6).substring(1);
                reading.setSubstitute(Boolean.parseBoolean(capitalizedBoolean));
            }
            readings.add(reading);
        }
        return readings;
    }

    public List<Customer> createCustomerFromCsv()
    {
        List<Customer> customers = new ArrayList<>();
        Iterable<List<String>> customerValues = getCustomerValues();
        for (List<String> customerList : customerValues)
        {
            Customer customer = new Customer();
            if (customerList.size() > 0)
            {
                customer.setId(UUID.fromString(customerList.getFirst()));
            }
            if (customerList.size() > 1)
            {
                switch (customerList.get(1))
                {
                    case "Herr":
                    case "M":
                        customer.setGender(ICustomer.Gender.M);
                        break;
                    case "Frau":
                    case "W":
                        customer.setGender(ICustomer.Gender.W);
                        break;
                    case "k.A.":
                    case "U":
                        customer.setGender(ICustomer.Gender.U);
                        break;
                }
            }
            if (customerList.size() > 2)
            {
                customer.setFirstName(customerList.get(2));
            }
            if (customerList.size() > 3)
            {
                customer.setLastName(customerList.get(3));
            }
            if (customerList.size() > 4)
            {
                if (customerList.get(4).equals("null")) {
                    customer.setBirthDate(null);
                }
                else {
                    DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy");
                    customer.setBirthDate(LocalDate.parse(customerList.get(4), dateTimeFormatter));
                }

            }

            customers.add(customer);
        }
        return customers;
    }
}
