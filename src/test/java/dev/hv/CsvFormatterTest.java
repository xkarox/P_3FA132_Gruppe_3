package dev.hv;

import org.junit.jupiter.api.*;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import static org.junit.jupiter.api.Assertions.assertEquals;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class CsvFormatterTest
{
    private static Path _csvDirectory;
    private static String _directory = "src/test/resources";
    private static String _csvFileName = "csvTest.csv";
    private static long _entriesInDirectory = 0;
    private static int _filesAdded = 0;
    private static int _linesAdded = 0;
    private static File _csvFile;

    @BeforeAll
    static void beforeAll()
    {
        try
        {
            _csvDirectory = Paths.get(_directory);
            _entriesInDirectory = Files.list(_csvDirectory).filter(Files::isRegularFile).count();
        } catch (IOException e)
        {
            throw new RuntimeException("Error when trying to count files in directory");
        }

        try
        {
            _csvFile = new File(_directory, _csvFileName);

            try (FileWriter writer = new FileWriter(_csvFile))
            {
                String _customerCorrectHeader = "UUID,Anrede,Vorname,Nachname,Geburtsdatum\n";
                String _customerCorrectValues =
                        "ec617965-88b4-4721-8158-ee36c38e4db3,Herr,Pumukel,Kobold,21.02.1962\n" +
                                "848c39a1-0cbb-427a-ac6f-a88941943dc8,Herr,André,Schöne,16.02.1928\n" +
                                "78dff413-7409-4313-90db-5ec95e969d6d,Frau,Antje,Kittler,12.09.1968";
                writer.write(_customerCorrectHeader);
                writer.write(_customerCorrectValues);
                _filesAdded++;
                _linesAdded += 4;
            }
        } catch (IOException e)
        {
            throw new RuntimeException("Error while creating temporary file");
        }
    }

    @Test
    @Order(1)
    void numberOfFilesInDirectory()
    {
        try
        {
            long actualFileCount = Files.list(_csvDirectory)
                    .filter(Files::isRegularFile)
                    .count();
            assertEquals(_entriesInDirectory + _filesAdded, actualFileCount, "It should be the same amount of files counted");

        } catch (IOException e)
        {
            throw new RuntimeException("Error when trying to count files in directory");
        }
    }

    @Test
    @Order(2)
    void numberOfLinesInFile()
    {
        try (Scanner scanner = new Scanner(_csvFile, StandardCharsets.UTF_8))
        {
            int actualLines = 0;
            while (scanner.hasNextLine())
            {
                actualLines++;
                scanner.nextLine();
            }
            assertEquals(actualLines, _linesAdded, "Line numbers are not equal");
        } catch (IOException e)
        {
            throw new RuntimeException("Error when trying to read number of lines");
        }
    }

    @Test
    void removeEmptyLinesTest()
    {
        CsvFormatter formatter = new CsvFormatter();
        File formattedFile = formatter.formatFile(_csvFile, ',');
        List<String> formattedFileLines = new ArrayList<>();
        List<String> unformattedFileLines = new ArrayList<>();

        try (Scanner scanner = new Scanner(formattedFile)) {
            while (scanner.hasNextLine()) {
                formattedFileLines.add(scanner.nextLine());
            }
        }
        catch (IOException e) {
            throw new RuntimeException("Error when trying to safe each line");
        }
        try (Scanner scanner = new Scanner(_csvFile)) {
            while (scanner.hasNextLine()) {
                unformattedFileLines.add(scanner.nextLine());
            }
        }
        catch (IOException e) {
            throw new RuntimeException("Error when trying to safe each line");
        }
        assertEquals(formattedFileLines, unformattedFileLines, "Formatted CSV-File should have same format");
    }

    @Test
    void formatFileTest()
    {

    }

    @AfterAll
    static void afterAll()
    {
        if (_csvFile.exists())
        {
            boolean deleted = _csvFile.delete();
            if (deleted)
            {
                System.out.println("CSV-File deleted: " + _csvFile.getAbsolutePath());
            } else
            {
                System.out.println("Error when trying to delete CSV-File: " + _csvFile.getAbsolutePath());
            }
        }

    }
}
