package dev.External.Tools;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class RequirementsTracker
{
    public static void runScan() throws IOException
    {
        Path sourceDir = Paths.get("src");
        Path requirementsFile = Paths.get("src/main/resources/requirements.txt");

        List<String> requirements = loadRequirements(requirementsFile);
        Set<String> foundRequirements = new HashSet<>();


        Files.walk(sourceDir)
                .filter(path -> path.toString().endsWith(".java"))
                .forEach(path ->
                {
                    try
                    {
                        processFile(path, requirements, foundRequirements);
                    } catch (IOException e)
                    {
                        System.err.println("Error on reading from file: " + path);
                    }
                });

        printResults(requirements, foundRequirements);
    }

    private static List<String> loadRequirements(Path file) throws IOException
    {
        return Files.lines(file)
                .map(String::trim)
                .filter(line -> !line.isEmpty() && !line.startsWith("#"))
                .collect(Collectors.toList());
    }

    private static void processFile(Path filePath, List<String> requirements, Set<String> foundRequirements) throws IOException
    {
        List<String> lines = Files.readAllLines(filePath);
        String REQ_COMMENT_PREFIX = "// Req. Nr.: "; // --ignore

        for (int lineNumber = 0; lineNumber < lines.size(); lineNumber++)
        {
            String line = lines.get(lineNumber);
            if (line.contains(REQ_COMMENT_PREFIX))
            {
                if (line.contains("--ignore"))
                {
                    continue;
                }
                String reqNumber = line.substring(line.indexOf(REQ_COMMENT_PREFIX) + REQ_COMMENT_PREFIX.length()).trim();
                String reqEntry = getRequirementText(reqNumber, requirements);
                String combinedEntry = String.format("%s | File: %s | Line: %d", reqEntry, filePath, lineNumber + 1);

                foundRequirements.add(combinedEntry);
            }
        }
    }

    private static String getRequirementText(String reqNumber, List<String> requirements)
    {
        return requirements.stream()
                .filter(req -> req.startsWith(reqNumber + "."))
                .findFirst()
                .orElse("Unknown Requirement");
    }

    private static void printResults(List<String> requirements, Set<String> foundRequirements)
    {
        System.out.println("Found Requirements:");
        foundRequirements.forEach(System.out::println);

        System.out.println("\nNot found Requirements:");
        requirements.stream()
                .filter(req -> foundRequirements.stream().noneMatch(found -> found.contains(req)))
                .forEach(System.out::println);
    }
}
