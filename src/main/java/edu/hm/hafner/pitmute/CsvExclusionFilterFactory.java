package edu.hm.hafner.pitmute;

import edu.hm.hafner.util.VisibleForTesting;
import org.pitest.mutationtest.build.InterceptorParameters;
import org.pitest.mutationtest.build.MutationInterceptor;
import org.pitest.mutationtest.build.MutationInterceptorFactory;
import org.pitest.plugin.Feature;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
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
    private final Logger logger;

    /**
     * Creates a {@code CsvExclusionFilterFactory} and initializes the logger.
     */
    public CsvExclusionFilterFactory() {
        this.logger = Logger.getLogger(CsvExclusionFilterFactory.class.getName());
    }

    /**
     * Constructor for testing purposes.
     * Allows injection of a mock logger.
     *
     * @param logger the logger to use
     */
    @VisibleForTesting
    CsvExclusionFilterFactory(final Logger logger) {
        this.logger = logger;
    }

    @Override
    public MutationInterceptor createInterceptor(final InterceptorParameters params) {
        String csvPath = params.settings()
                .flatMap(settings -> settings.getString("csvFile"))
                .orElse("");

        if (csvPath.isBlank()) {
            throw new IllegalStateException("Missing or empty feature parameter \"csvFile\". Please provide the path "
                    + "to a CSV file, e.g. +FCSV(csvFile[src/main/resources/exclusions.csv]).");
        }
        return new CsvExclusionFilter(getCsvExclusionEntries(csvPath));
    }

    List<CsvExclusionEntry> getCsvExclusionEntries(final String csvPath) {
        List<CsvExclusionEntry> entries = new ArrayList<>();

        try {
            List<String> lines = Files.readAllLines(Paths.get(csvPath), StandardCharsets.UTF_8);
            int lineNumber = 0;

            for (String line : lines) {
                if (line.isBlank() || line.trim().startsWith("#")) {
                    continue;
                }

                String[] fields = line.split(CSV_SEPARATOR, -1);
                if (fields.length > MIN_FIELDS) {
                    logger.log(Level.WARNING, "Skipping invalid line {0}: it contains too many fields. "
                            + "A line may contain a maximum of four fields (className, mutator (optional), "
                            + "startLine (optional), endLine (optional)).", lineNumber);
                    continue;
                }
                fields = Arrays.copyOf(fields, MIN_FIELDS);

                lineNumber++;
                Optional<String> classNameOptional = normalize(fields[0]);
                if (classNameOptional.isEmpty()) {
                    logger.log(Level.WARNING, "Skipping line because class name is missing: {0}", lineNumber);
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
                    logger.log(Level.WARNING, "Skipping invalid line: {0}", lineNumber);
                }
            }
            return entries;
        }
        catch (IOException e) {
            throw new IllegalStateException("Failed to read CSV file. Please verify that the path is correct and the "
                    + "file exists: " + csvPath, e);
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
