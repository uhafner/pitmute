package edu.hm.hafner.pit.suppress;

import org.pitest.mutationtest.build.InterceptorParameters;
import org.pitest.mutationtest.build.MutationInterceptor;
import org.pitest.mutationtest.build.MutationInterceptorFactory;
import org.pitest.plugin.Feature;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Factory for creating a {@link CsvExclusionFilter} using exclusion rules defined in a CSV file.
 *
 * <p>
 * The CSV is parsed and each valid row is mapped to a {@link CsvExclusionEntry}.
 * These entries are then used to suppress generated PIT mutations according to the configured rules.
 * </p>
 *
 * <p>
 * The path to the CSV file must be specified in the PIT feature configuration within the <code>pom.xml</code>.
 * For more information on the expected CSV format, please refer to the project's README file.
 * </p>
 */
public class CsvExclusionFilterFactory implements MutationInterceptorFactory {
    private static final String CSV_SEPARATOR = ",";
    private static final int MIN_FIELDS = 4;
    private static final Logger LOGGER = Logger.getLogger(CsvExclusionFilterFactory.class.getName());

    @Override
    public MutationInterceptor createInterceptor(final InterceptorParameters params) {
        String csvPath = params.settings()
                .flatMap(settings -> settings.getString("csvFile"))
                .orElse(null);

        if (csvPath == null || csvPath.isBlank()) {
            throw new IllegalStateException("Path to CSV file is missing or empty");
        }
        return new CsvExclusionFilter(getCsvExclusionEntries(csvPath));
    }

    List<CsvExclusionEntry> getCsvExclusionEntries(final String csvPath) {
        List<CsvExclusionEntry> entries = new ArrayList<>();

        try (BufferedReader br = Files.newBufferedReader(Paths.get(csvPath), StandardCharsets.UTF_8)) {
            String line = br.readLine();
            int lineNumber = 0;
            while (line != null) {
                String[] fields = line.split(CSV_SEPARATOR, -1);

                lineNumber++;
                if (fields.length >= MIN_FIELDS) {
                    Optional<String> classNameOptional = normalize(fields[0]);
                    if (classNameOptional.isEmpty()) {
                        LOGGER.log(Level.WARNING, "Skipping line because class name is missing: ", lineNumber);
                        line = br.readLine();
                        continue;
                    }

                    try {
                        entries.add(new CsvExclusionEntry(
                                classNameOptional.get(),
                                normalize(fields[1]),
                                tryParseInteger(fields[2]),
                                tryParseInteger(fields[3])
                        ));
                    }
                    catch (IllegalArgumentException e) {
                        LOGGER.log(Level.WARNING, "Skipping invalid line: ", lineNumber);
                    }
                }
                else {
                    LOGGER.log(Level.WARNING, "Line should contain four fields (className, mutator (optional), startLine (optional), endLine (optional)). Skipping invalid line: ", lineNumber);
                }
                line = br.readLine();
            }
            return entries;
        }
        catch (IOException e) {
            throw new IllegalStateException("Failed to read CSV file. Please verify that the path is correct and the file exists: " + csvPath, e);
        }
    }

    private Optional<String> normalize(final String string) {
        return (string == null || string.isBlank()) ? Optional.empty() : Optional.of(string.trim());
    }

    private Optional<Integer> tryParseInteger(final String string) {
        if (string == null || string.isBlank()) {
            return Optional.empty();
        }
        try {
            return Optional.of(Integer.parseInt(string.trim()));
        }
        catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid integer: " + string, e);
        }
    }

    @Override
    public Feature provides() {
        return Feature.named("FCSV")
                .withDescription("Exclude mutations based on CSV file")
                .withOnByDefault(false);
    }

    @Override
    public String description() {
        return "Exclude mutations based on CSV file";
    }
}
