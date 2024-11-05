package ace;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class CsvHelper
{
    public enum FileType {
        customerFileType,
        readingFileType
    }
    private File _csvFile;

    public CsvHelper(File csvFile) {
        this._csvFile = csvFile;
    }

    public void readCsvFile() {
        try (Scanner scanner = new Scanner(this._csvFile)) {
            switch (this.checkFileType(scanner.nextLine())) {
                case FileType.customerFileType:
                    List<List<String>> data = new ArrayList<>();
                    if (scanner.hasNextLine()) {
                        scanner.nextLine();
                    }
                    while(scanner.hasNextLine()) {
                        String line = scanner.nextLine();
                        String[] values = line.split(",");
                        System.out.println(values[0]);
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
}
