package ace;

import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.Scanner;

public class CsvFormatter
{

    public File formatCsv(File csvFile) {
        File formattedFile = new File("formattedFile");
        try (Scanner scanner = new Scanner(csvFile)) {
            PrintWriter writer = new PrintWriter(new FileWriter(formattedFile));

            while (scanner.hasNextLine()) {
                String line = scanner.nextLine();

                if (line.trim().isEmpty()) {
                    continue;
                }
                writer.println(line);
            }
            writer.flush();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        return formattedFile;
    }
}
