package edu.hm.hafner.pit.suppress;

import java.util.Optional;

/**
 * Represents an exclusion entry from the CSV file.
 * An entry specifies which mutations should be ignored by PIT.
 *
 * <p>
 * Each entry contains the class name (required) and optional fields such as a specific mutator name and an optional line range.
 * For more information, please see the README.
 * </p>
 *
 * @param className    The fully qualified class name or just the file name for which mutations should be ignored.
 * @param mutationName Optional name of the mutator whose mutations should be ignored.
 * @param startLine    Optional start line (inclusive) of the range for ignored mutations.
 * @param endLine      Optional end line (inclusive) of the range for ignored mutations.
 */
public record CsvExclusionEntry(String className, Optional<String> mutationName, Optional<Integer> startLine,
                                Optional<Integer> endLine) {
}