package dev.hv;

import org.junit.jupiter.api.*;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

import static org.junit.jupiter.api.Assertions.assertEquals;


@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class CsvFormatterTest
{
    private static long _entriesInDirectory = 0;

    private static final String _directory = "src/test/resources/";
    private static final String _csvFileNameFlawless = "csvTestFlawless.csv";
    private static final String _csvFileNameWithEndingComma = "csvTestWithEndingComma.csv";
    private static final String _csvFileNameWithEmptyValues = "csvTestWithEmptyValues.csv";
    private static final String _csvFileNameWithEmptyLines = "csvTestWithEmptyLines.csv";

    private static final String _csvFileHeader = "UUID,Anrede,Vorname,Nachname,Geburtsdatum\n";

    private static File _csvFileFlawless = new File(_csvFileNameFlawless);
    private static File _csvFileWithEndingComma = new File(_csvFileNameWithEndingComma);
    private static File _csvFileWithEmptyValues = new File(_csvFileNameWithEmptyValues);
    private static File _csvFileWithEmptyLines = new File(_csvFileNameWithEmptyLines);

    private static Map<File, Integer> _mockedFiles = new HashMap<>();

    @BeforeAll
    static void beforeAll()
    {
        try
        {
            _entriesInDirectory = Files.list(Paths.get(_directory)).filter(Files::isRegularFile).count();
        } catch (IOException e)
        {
            throw new RuntimeException("Error when trying to count files in directory");
        }

        try (FileWriter writer = new FileWriter(_directory + _csvFileNameFlawless))
        {
            String _customerValuesFlawless =
                    "ec617965-88b4-4721-8158-ee36c38e4db3,Herr,Pumukel,Kobold,21.02.1962\n" +
                            "848c39a1-0cbb-427a-ac6f-a88941943dc8,Herr,André,Schöne,16.02.1928\n" +
                            "78dff413-7409-4313-90db-5ec95e969d6d,Frau,Antje,Kittler,12.09.1968";
            writer.write(_csvFileHeader);
            writer.write(_customerValuesFlawless);
            _mockedFiles.put(_csvFileFlawless, 4);

        } catch (IOException e)
        {
            throw new RuntimeException("Error while creating flawless temporary file", e);
        }

        try (FileWriter writer = new FileWriter(_directory + _csvFileNameWithEndingComma))
        {
            String _customerValuesWithEndingComma =
                    "f2683104-974d-44eb-a060-82ed72737cbe,Frau,Elgine,Karras,\n" +
                            "2a284519-4141-409c-a5d6-ad77bba13523,Frau,Karolina,Hamburger,\n";
            writer.write(_csvFileHeader);
            writer.write(_customerValuesWithEndingComma);
            _mockedFiles.put(_csvFileWithEndingComma, 3);

        }
        catch (IOException e) {
            throw new RuntimeException("Error while creating temporary file with ending comma");
        }

        try (FileWriter writer = new FileWriter(_directory + _csvFileNameWithEmptyValues)) {
            String _customerValuesWithEmptyValues =
                    ",Frau,Annedorle,Luber,\n" +
                            "51a57f7c-2080-4fcc-8107-17b613b2b948,,Anastasia,Weißmann,\n" +
                            "b64627ef-18cf-49b5-8998-5aa5200e3459,Herr,Hilar,Angenendt,09.02.1936\n" +
                            "4e7726e2-3ca9-4d4c-904a-bff033688162,Frau,Heide,Gierl,\n" +
                            "c8d83f41-e84e-49b2-bf36-42544266e46e,Frau,Friedlies,Mertins,\n" +
                            "1d9ad1bb-f4eb-4e3f-bc87-65d5e8ffb0c9,Frau,,Stark,\n" +
                            "1152b31d-50ad-40fb-ae96-d06d5517b7d8,Herr,Heiner,Strohm,05.02.1997";
            writer.write(_csvFileHeader);
            writer.write(_customerValuesWithEmptyValues);
            _mockedFiles.put(_csvFileWithEmptyValues, 8);
        }
        catch (IOException e) {
            throw new RuntimeException("Error while creating temporary file with emptyValues");
        }

        try (FileWriter writer = new FileWriter(_directory + _csvFileNameWithEmptyLines)) {
            String _customerValuesWithEmptyLines =
                    "ec617965-88b4-4721-8158-ee36c38e4db3,Herr,Pumukel,Kobold,21.02.1962\n" +
                            "848c39a1-0cbb-427a-ac6f-a88941943dc8,Herr,André,Schöne,16.02.1928\n" +
                            ",,\n" +
                            ",\n" +
                            "\n" +
                            ",,,,,,,,,\n" +
                            "78dff413-7409-4313-90db-5ec95e969d6d,Frau,Antje,Kittler,12.09.1968";

            writer.write(_csvFileHeader);
            writer.write(_customerValuesWithEmptyLines);
            _mockedFiles.put(_csvFileWithEmptyLines, 8);
        }
        catch (IOException e) {
            throw new RuntimeException("Error while creating temporary customer file empty lines");
        }
    }

    @Test
    @Order(1)
    void numberOfFilesInDirectoryTest()
    {
        try
        {
            long actualFileCount = Files.list(Paths.get(_directory))
                    .filter(Files::isRegularFile)
                    .count();
            assertEquals(_entriesInDirectory + _mockedFiles.size(), actualFileCount, "It should be the same amount of files counted");

        } catch (IOException e)
        {
            throw new RuntimeException("Error when trying to count files in directory");
        }
    }

    @Test
    @Order(2)
    void numberOfLinesInEachFileTest() {
        for (Map.Entry<File, Integer> entry : _mockedFiles.entrySet()) {
            File file = entry.getKey();
            int expectedLineCount = entry.getValue();

            try (Scanner scanner = new Scanner(file, StandardCharsets.UTF_8)) {
                int actualLineCount = 0;

                while (scanner.hasNextLine()) {
                    scanner.nextLine();
                    actualLineCount++;
                }

                if (actualLineCount == expectedLineCount) {
                    System.out.println("Test bestanden für Datei: " + file.getName());
                } else {
                    System.out.println("Test fehlgeschlagen für Datei: " + file.getName() +
                            " (Erwartet: " + expectedLineCount + ", Tatsächlich: " + actualLineCount + ")");
                }

            } catch (IOException e) {
                throw new RuntimeException("Error when trying to count lines of each mocked file", e);
            }
        }

    }

    @Test
    void removeEmptyLinesTest()
    {
        CsvFormatter formatter = new CsvFormatter();

        // Erstelle ein temporäres CSV mit leeren Zeilen für den Test
        File formattedFile = formatter.formatFile(_csvFileFlawless, ',');

        // Die erwarteten Zeilen im formatierte CSV
        List<String> expectedLines = new ArrayList<>();
        try (Scanner scanner = new Scanner(_csvFileFlawless))
        {
            while (scanner.hasNextLine())
            {
                String line = scanner.nextLine();
                if (!line.trim().isEmpty())
                {
                    expectedLines.add(line);  // Leere Zeilen überspringen
                }
            }
        } catch (IOException e)
        {
            throw new RuntimeException("Error when trying to safe each line");
        }

        // Jetzt die Zeilen aus der formatierten Datei sammeln
        List<String> formattedFileLines = new ArrayList<>();
        try (Scanner scanner = new Scanner(formattedFile))
        {
            while (scanner.hasNextLine())
            {
                formattedFileLines.add(scanner.nextLine());
            }
        } catch (IOException e)
        {
            throw new RuntimeException("Error when trying to safe each line");
        }

        // Überprüfen, dass beide Listen gleich sind (leere Zeilen entfernt)
        assertEquals(expectedLines, formattedFileLines, "Formatted CSV-File should have no empty lines");
    }

    @Test
    void formatFileTest()
    {

    }

    @AfterAll
    static void afterAll()
    {
        if (_csvFileFlawless.exists())
        {
            boolean deleted = _csvFileFlawless.delete();
            if (deleted)
            {
                System.out.println("CSV-File deleted: " + _csvFileFlawless.getAbsolutePath());
            } else
            {
                System.out.println("Error when trying to delete CSV-File: " + _csvFileFlawless.getAbsolutePath());
            }
        }

    }
}
