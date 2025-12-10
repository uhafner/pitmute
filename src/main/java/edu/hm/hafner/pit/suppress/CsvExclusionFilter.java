package edu.hm.hafner.pit.suppress;

import org.pitest.bytecode.analysis.ClassTree;
import org.pitest.mutationtest.build.InterceptorType;
import org.pitest.mutationtest.build.MutationInterceptor;
import org.pitest.mutationtest.engine.Mutater;
import org.pitest.mutationtest.engine.MutationDetails;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * Filters generated PIT mutations based on a list of {@link CsvExclusionEntry} entries.
 * Only mutations matching the criteria specified in the provided entries are suppressed.
 * For more information, please see the README.
 */
public class CsvExclusionFilter implements MutationInterceptor {
    private final List<CsvExclusionEntry> entries;

    /**
     * Constructs a new {@code CsvExclusionFilter} with the given list of exclusion entries.
     *
     * @param entries the list of entries used to exclude mutations.
     */
    public CsvExclusionFilter(final List<CsvExclusionEntry> entries) {
        this.entries = List.copyOf(entries);
    }

    @Override
    public InterceptorType type() {
        return InterceptorType.FILTER;
    }

    @Override
    public void begin(ClassTree classTree) {
        // nothing to do
    }

    @Override
    public Collection<MutationDetails> intercept(final Collection<MutationDetails> mutations, Mutater mutater) {
        if (mutations.isEmpty()) {
            return Collections.emptyList();
        }

        Collection<MutationDetails> filtered = mutations;

        for (CsvExclusionEntry entry : entries) {
            filtered = filtered.stream()
                    .filter(mutation -> !shouldSuppressMutation(mutation, entry))
                    .toList();
        }

        return filtered;
    }

    private boolean shouldSuppressMutation(final MutationDetails mutation, final CsvExclusionEntry entry) {
        boolean classNameMatches = classNameMatches(mutation.getClassName().asJavaName(), entry.className());
        boolean mutationNameMatches = mutationNameMatches(mutation.getMutator(), entry);
        boolean startLineMatches = entry.startLine().isEmpty() || mutation.getLineNumber() >= entry.startLine().get();
        boolean endLineMatches = entry.endLine().isEmpty() || mutation.getLineNumber() <= entry.endLine().get();

        return classNameMatches && mutationNameMatches && startLineMatches && endLineMatches;
    }

    private boolean classNameMatches(final String fqcn, final String entry) {
        if (entry == null) {
            return false; // entry should never be null
        }

        String simpleName = fqcn.substring(fqcn.lastIndexOf('.') + 1);
        String normalized = entry.endsWith(".java") ? entry.substring(0, entry.length() - 5) : entry;
        return entry.equals(fqcn) || entry.equals(simpleName) || normalized.equals(simpleName);
    }

    private boolean mutationNameMatches(final String fqcn, final CsvExclusionEntry entry) {
        if (entry.mutationName().isEmpty()) {
            return true;
        }

        String mutationNameEntry = entry.mutationName().get();
        String mutationName = fqcn.substring(fqcn.lastIndexOf('.') + 1);
        String shortMutationName = mutationName.endsWith("Mutator") ? mutationName.substring(0, mutationName.length() - 7) : mutationName;
        return fqcn.equals(mutationNameEntry) || mutationName.equals(mutationNameEntry) || shortMutationName.equals(mutationNameEntry);
    }

    @Override
    public void end() {
        // nothing to do
    }
}
