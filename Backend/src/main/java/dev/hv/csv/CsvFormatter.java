package dev.hv.csv;

import java.util.Scanner;

public class CsvFormatter
{

    public String formatReadingCsv(String csvFile)
    {
        StringBuilder formattedContent = new StringBuilder();

        try (Scanner scanner = new Scanner(csvFile))
        {
            String separator = ";";
            String regex = "^" + separator + "+$";

            while (scanner.hasNextLine())
            {
                String line = scanner.nextLine();
                // line = line.replace(',', '.');

                if (line.trim().isEmpty() || line.matches(regex))
                {
                    continue;
                }
                formattedContent.append(line).append("\n");
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }

        return formattedContent.toString();
    }

    public String formatCustomerCsv(String csvFile) {
        StringBuilder formattedContent = new StringBuilder();

        try (Scanner scanner = new Scanner(csvFile))
        {
            String separator = ";";
            String regex = "^" + separator + "+$";

            while (scanner.hasNextLine())
            {
                String line = scanner.nextLine();
                line = line.replace(',', ';');

                if (line.trim().isEmpty() || line.matches(regex))
                {
                    continue;
                }
                formattedContent.append(line).append("\n");
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }

        return formattedContent.toString();
    }
}
