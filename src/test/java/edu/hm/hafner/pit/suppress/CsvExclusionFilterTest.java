package edu.hm.hafner.pit.suppress;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.pitest.classinfo.ClassName;
import org.pitest.mutationtest.build.InterceptorType;
import org.pitest.mutationtest.engine.Mutater;
import org.pitest.mutationtest.engine.MutationDetails;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class CsvExclusionFilterTest {
    private static final String MAIN_FQCN = "com.example.Main";
    private static final String MATH_MUTATOR_FQCN = "org.pitest.mutationtest.engine.gregor.mutators.MathMutator";
    private static final String PRIMITIVE_RETURNS_MUTATOR_FQCN = "org.pitest.mutationtest.engine.gregor.mutators.returns.PrimitiveReturnsMutator";
    private final Mutater mutater = mock(Mutater.class);

    private MutationDetails createMutation(final String className, final String mutator, final int lineNumber) {
        var mutation = mock(MutationDetails.class);
        when(mutation.getClassName()).thenReturn(ClassName.fromString(className));
        when(mutation.getMutator()).thenReturn(mutator);
        when(mutation.getLineNumber()).thenReturn(lineNumber);
        return mutation;
    }

    @Test
    void shouldReturnCorrectType() {
        var csvExclusionFilter = new CsvExclusionFilter(List.of());
        assertThat(csvExclusionFilter.type()).isEqualTo(InterceptorType.FILTER);
    }

    @Test
    void interceptShouldReturnEmptyListWhenInputListIsEmpty() {
        var csvExclusionFilter = new CsvExclusionFilter(List.of());
        assertThat(csvExclusionFilter.intercept(List.of(), mutater)).isEmpty();
    }

    @Test
    void interceptShouldNotRemoveMutationsWhenNoExclusionRulesExist() {
        var csvExclusionFilter = new CsvExclusionFilter(List.of());
        var mutation = createMutation(MAIN_FQCN, MATH_MUTATOR_FQCN, 5);

        Collection<MutationDetails> filteredMutations = csvExclusionFilter.intercept(List.of(mutation), mutater);

        assertThat(filteredMutations).containsExactly(mutation);

        // Contains two mutations
        var mutation2 = createMutation(MAIN_FQCN, PRIMITIVE_RETURNS_MUTATOR_FQCN, 7);

        filteredMutations = csvExclusionFilter.intercept(List.of(mutation, mutation2), mutater);

        assertThat(filteredMutations).containsExactly(mutation, mutation2);
    }

    @Test
    void interceptShouldRemoveMutationInOneOfTwoClassesWithSameName() {
        var exclusionEntry = new CsvExclusionEntry(MAIN_FQCN, Optional.of("MathMutator"), Optional.of(5), Optional.of(5));
        var csvExclusionFilter = new CsvExclusionFilter(List.of(exclusionEntry));

        var mutation = createMutation(MAIN_FQCN, MATH_MUTATOR_FQCN, 5);
        var mutation2 = createMutation("com.example.otherPackage.Main", MATH_MUTATOR_FQCN, 5);

        Collection<MutationDetails> filteredMutations = csvExclusionFilter.intercept(List.of(mutation, mutation2), mutater);

        assertThat(filteredMutations).containsExactly(mutation2);
    }

    @ParameterizedTest(name = "{index} => className: \"{0}\", mutationName: \"{1}\", startLine: \"{2}\", endLine: \"{3}\"")
    @CsvSource(value = {
            "com.example.Main, null, null, null",
            "com.example.Main, org.pitest.mutationtest.engine.gregor.mutators.MathMutator, null, null",
            "com.example.Main, MathMutator, null, null",
            "com.example.Main, Math, null, null",
            "com.example.Main, null, 5, null",
            "com.example.Main, null, 4, null",
            "com.example.Main, null, 5, 5",
            "com.example.Main, null, null, 5",
            "com.example.Main, null, null, 6",
            "com.example.Main, MathMutator, 5, 5"
    }, nullValues = "null")
    void interceptShouldReturnEmptyCollectionAsMutationMatchesExclusionEntry(final String className, final String mutationName, final Integer startLine, final Integer endLine) {
        var exclusionEntry = new CsvExclusionEntry(className, Optional.ofNullable(mutationName), Optional.ofNullable(startLine), Optional.ofNullable(endLine));
        var csvExclusionFilter = new CsvExclusionFilter(List.of(exclusionEntry));
        var mutation = createMutation(MAIN_FQCN, MATH_MUTATOR_FQCN, 5);

        Collection<MutationDetails> filteredMutations = csvExclusionFilter.intercept(List.of(mutation), mutater);

        assertThat(filteredMutations).isEmpty();
    }

    @ParameterizedTest(name = "{index} => className: \"{0}\", mutationName: \"{1}\", startLine: \"{2}\", endLine: \"{3}\"")
    @CsvSource(value = {
            "null, null, null, null",
            "com.example.Main, null, 6, null",
            "com.example.Main, null, null, 4",
            "com.example.Main, null, 1, 4",
            "com.example.Main, null, 6, 10",
            "com.example.Main, org.pitest.mutationtest.engine.gregor.mutators.returns.PrimitiveReturnsMutator, null, null",
            "com.example.Main, PrimitiveReturnsMutator, null, null"
    }, nullValues = "null")
    void interceptShouldKeepMutationAsExclusionEntryDoesNotMatch(final String className, final String mutationName, final Integer startLine, final Integer endLine) {
        var exclusionEntry = new CsvExclusionEntry(className, Optional.ofNullable(mutationName), Optional.ofNullable(startLine), Optional.ofNullable(endLine));
        var csvExclusionFilter = new CsvExclusionFilter(List.of(exclusionEntry));

        var mutation = createMutation(MAIN_FQCN, MATH_MUTATOR_FQCN, 5);

        Collection<MutationDetails> filteredMutations = csvExclusionFilter.intercept(List.of(mutation), mutater);

        assertThat(filteredMutations).containsExactly(mutation);
    }

    @Test
    void shouldHandleMultipleExclusionEntriesCorrectly() {
        var entry1 = new CsvExclusionEntry(MAIN_FQCN, Optional.of("MathMutator"), Optional.of(5), Optional.of(5));
        var entry2 = new CsvExclusionEntry(MAIN_FQCN, Optional.of("PrimitiveReturnsMutator"), Optional.of(1), Optional.of(5));
        var entry3 = new CsvExclusionEntry(MAIN_FQCN, Optional.of("PrimitiveReturnsMutator"), Optional.of(4), Optional.of(7));

        var filter = new CsvExclusionFilter(List.of(entry1, entry2, entry3));

        var mutation1 = createMutation(MAIN_FQCN, MATH_MUTATOR_FQCN, 5);
        var mutation2 = createMutation(MAIN_FQCN, PRIMITIVE_RETURNS_MUTATOR_FQCN, 5);
        var mutation3 = createMutation(MAIN_FQCN, PRIMITIVE_RETURNS_MUTATOR_FQCN, 10);

        var result = filter.intercept(List.of(mutation1, mutation2, mutation3), mutater);
        assertThat(result).containsExactly(mutation3);
    }

    @Test
    void interceptShouldIgnoreProvidedMutater() {
        var mutation = createMutation(MAIN_FQCN, MATH_MUTATOR_FQCN, 5);

        var csvExclusionFilter = new CsvExclusionFilter(List.of());
        Collection<MutationDetails> filteredMutations = csvExclusionFilter.intercept(List.of(mutation), mutater);

        verifyNoInteractions(mutater);
        assertThat(filteredMutations).containsExactly(mutation);
    }

    @ParameterizedTest(name = "{index} => className: \"{0}\"")
    @CsvSource(value = {
            "com.example.Main",
            "Main.java",
            "Main"
    }, nullValues = "null")
    void interceptShouldMatchClassNames(final String className) {
        var exclusionEntry = new CsvExclusionEntry(className, Optional.of("MathMutator"), Optional.empty(), Optional.empty());
        var csvExclusionFilter = new CsvExclusionFilter(List.of(exclusionEntry));
        var mutation = createMutation(MAIN_FQCN, MATH_MUTATOR_FQCN, 5);

        Collection<MutationDetails> filteredMutations = csvExclusionFilter.intercept(List.of(mutation), mutater);

        assertThat(filteredMutations).isEmpty();
    }

    @ParameterizedTest(name = "{index} => className: \"{0}\"")
    @CsvSource(value = {
            "com.example.Main.java",
            "src.main.java.com.example.Main",
            "OtherMain.java",
            "OtherMain",
            "MainClass",
            ".java"
    }, nullValues = "null")
    void interceptShouldNotMatchWithInvalidClassNameFormats(final String className) {
        var exclusionEntry = new CsvExclusionEntry(className, Optional.of("MathMutator"), Optional.empty(), Optional.empty());
        var csvExclusionFilter = new CsvExclusionFilter(List.of(exclusionEntry));
        var mutation = createMutation(MAIN_FQCN, MATH_MUTATOR_FQCN, 5);

        Collection<MutationDetails> filteredMutations = csvExclusionFilter.intercept(List.of(mutation), mutater);

        assertThat(filteredMutations).containsExactly(mutation);
    }
}
