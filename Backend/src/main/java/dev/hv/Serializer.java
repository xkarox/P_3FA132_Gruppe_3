package dev.hv;

import dev.hv.csv.CsvParser;
import dev.hv.database.services.CustomerService;
import dev.hv.model.ICustomer;
import dev.hv.model.IReading;
import dev.hv.model.classes.Customer;
import dev.hv.model.classes.Reading;
import dev.hv.model.interfaces.IDbItem;
import dev.provider.ServiceProvider;

import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Serializer
{
    public static String serializeIntoCsv(List<?> items)
    {
        if (items.isEmpty())
        {
            return null;
        }
        Object firstItem = items.get(0);
        StringBuilder csvContent = new StringBuilder();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy");
        if (firstItem instanceof Reading)
        {
            List<Reading> readings = (List<Reading>) items;

            for (Reading reading : readings)
            {
                if (reading.getDateOfReading() != null)
                {
                    csvContent.append(reading.getDateOfReading().format(formatter)).append(";");
                } else
                {
                    csvContent.append(";");
                }
                if (reading.getMeterCount() != null)
                {
                    csvContent.append(reading.getMeterCount()).append(";");
                } else
                {
                    csvContent.append(";");
                }
                if (reading.getComment() != null)
                {
                    csvContent.append(reading.getComment()).append(";");
                } else
                {
                    csvContent.append(";");
                }
                if (reading.getCustomer() != null)
                {
                    csvContent.append(reading.getCustomer().getId()).append(";");
                } else
                {
                    csvContent.append(";");
                }
                if (reading.getKindOfMeter() != null)
                {
                    csvContent.append(reading.getKindOfMeter()).append(";");
                } else
                {
                    csvContent.append(";");
                }
                if (reading.getMeterId() != null)
                {
                    csvContent.append(reading.getMeterId()).append(";");
                } else
                {
                    csvContent.append(";");
                }
                if (reading.getSubstitute() != null)
                {
                    csvContent.append(reading.getSubstitute());
                }
                csvContent.append("\n");
            }
        } else if (firstItem instanceof Customer)
        {
            List<Customer> customers = (List<Customer>) items;

            for (Customer customer : customers)
            {
                if (customer.getId() != null)
                {
                    csvContent.append(customer.getId()).append(";");
                } else
                {
                    csvContent.append(";");
                }
                if (customer.getGender() != null)
                {
                    csvContent.append(customer.getGender()).append(";");
                } else
                {
                    csvContent.append(";");
                }
                if (customer.getFirstName() != null)
                {
                    csvContent.append(customer.getFirstName()).append(";");
                } else
                {
                    csvContent.append(";");
                }
                if (customer.getLastName() != null)
                {
                    csvContent.append(customer.getLastName()).append(";");
                } else
                {
                    csvContent.append(";");
                }
                if (customer.getBirthDate() != null)
                {
                    csvContent.append(customer.getBirthDate().format(formatter));
                }
                csvContent.append("\n");
            }
        }
        return csvContent.toString();
    }

    public static List<?> deserializeCsv(String csv, String type) throws IOException, ReflectiveOperationException, SQLException
    {
        CsvParser parser = new CsvParser();
        parser.setCsvContent(csv);

        boolean isDefaultReading = false;
        boolean isCustomReading = false;
        boolean isCustomer = false;

        boolean water = false;
        boolean heat = false;
        boolean electricity = false;

        String[] customerHeader = {"UUID", "Anrede", "Vorname", "Nachname", "Geburtsdatum"};

        String[] defaultReadingHeaderWater = {"Datum", "Zählerstand in m³", "Kommentar"};
        String[] defaultReadingHeaderElectricity = {"Datum", "Zählerstand in kWh", "Kommentar"};
        String[] defaultReadingHeaderHeat = {"Datum", "Zählerstand in MWh", "Kommentar"};

        String[] customReadingHeader = {"Datum", "Zählerstand", "Kommentar", "KundenId", "Zählerart", "ZählerstandId", "Ersatz"};

        if (type.equals("readings")) {
            List<String> csvReadingHeader = List.copyOf((java.util.Collection<? extends String>) parser.getReadingHeader());
            List<String> csvCustomReadingHeader = List.copyOf((java.util.Collection<? extends String>) parser.getCustomReadingHeader());

            if (Arrays.equals(csvReadingHeader.toArray(), defaultReadingHeaderWater))
            {
                isDefaultReading = true;
                water = true;
            } else if (Arrays.equals(csvReadingHeader.toArray(), defaultReadingHeaderElectricity))
            {
                isDefaultReading = true;
                electricity = true;
            } else if (Arrays.equals(csvReadingHeader.toArray(), defaultReadingHeaderHeat))
            {
                isDefaultReading = true;
                heat = true;
            } else if (Arrays.equals(csvCustomReadingHeader.toArray(), customReadingHeader))
            {
                isCustomReading = true;
            }

            if (isDefaultReading)
            {
                List<Reading> readings = parser.createDefaultReadingsFromCsv(heat, water, electricity);
                return readings;
            } else if (isCustomReading)
            {
                List<Reading> readings = parser.createCustomReadingsFromCsv();
                return readings;
            }



        }
        else if (type.equals("customers")) {
            List<String> csvCustomerHeader = List.copyOf((java.util.Collection<? extends String>) parser.getCustomerHeader());

            if (Arrays.equals(csvCustomerHeader.toArray(), customerHeader))
            {
                isCustomer = true;
            }

            if (isCustomer)
            {
                List<Customer> customers = parser.createCustomerFromCsv();
                return customers;
            }
        }
        return null;
    }


}
