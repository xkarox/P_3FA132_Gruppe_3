package ace;

import ace.model.classes.Customer;

import java.io.File;
import java.util.*;

public class CsvParser
{
    public enum FileType {
        customerFileType,
        readingFileType
    }
    private File _csvFile;
    private CsvFormatter _csvFormatter;


    public CsvParser(File csvFile) {
        this._csvFile = csvFile;
        this._csvFormatter = new CsvFormatter();
    }

    public List<Dictionary<String, String>> getAllValues() {
        this._csvFile = this._csvFormatter.formatCsv(this._csvFile);

        try (Scanner scanner = new Scanner(this._csvFile)) {
            while (scanner.hasNextLine()){
                String line = scanner.nextLine();
                System.out.println(line);
            }
        }
        catch (Exception e){
            e.printStackTrace();
        }
        return null;
    }

    public void readCsvFile() {
        try (Scanner scanner = new Scanner(this._csvFile)) {
            switch (this.checkFileType(scanner.nextLine())) {
                case FileType.customerFileType:
                    List<String> templateHeader = new ArrayList<>();
                    List<List<String>> data = new ArrayList<>();
                    if (scanner.hasNextLine()) {
                        scanner.nextLine();
                    }
                    while(scanner.hasNextLine()) {
                        String line = scanner.nextLine();
                        String[] values = line.split(",");
                        Customer customer = new Customer();
                        // customer = values[0];
                        System.out.println(Arrays.toString(values));
                    }

                    break;
                case FileType.readingFileType:

                    break;
                case null:
                    break;
            }
        }
        catch (Exception e) {

        }
    }

    private FileType checkFileType(String firstLine) {
        if (firstLine.startsWith("UUID")) {
            return FileType.customerFileType;
        }
        else if (firstLine.startsWith("\"Kunde\"")) {
            return FileType.readingFileType;
        }
        return null;
    }

    private File getCsvFile() {
        return this._csvFile;
    }
}
