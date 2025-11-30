package edu.hm.hafner.pit.suppress;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.pitest.mutationtest.build.InterceptorParameters;
import org.pitest.mutationtest.build.MutationInterceptor;
import org.pitest.plugin.Feature;
import org.pitest.plugin.FeatureSetting;

import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class CsvExclusionFilterFactoryTest {
    private final CsvExclusionFilterFactory factory = new CsvExclusionFilterFactory();

    @Test
    void shouldCreateCsvExclusionFilterWhenCsvPathIsProvidedAndExists() {
        InterceptorParameters params = mock(InterceptorParameters.class);
        FeatureSetting settings = mock(FeatureSetting.class);

        when(settings.getString("csvFile")).thenReturn(Optional.of("src/test/resources/validFormattedExclusions.csv"));
        when(params.settings()).thenReturn(Optional.of(settings));

        MutationInterceptor mutationInterceptor = factory.createInterceptor(params);

        assertThat(mutationInterceptor).isInstanceOf(CsvExclusionFilter.class);
    }

    @Test
    void shouldThrowExceptionWhenPathIsInvalid() {
        InterceptorParameters params = mock(InterceptorParameters.class);
        FeatureSetting settings = mock(FeatureSetting.class);

        when(settings.getString("csvFile")).thenReturn(Optional.of("notExistingPath/validFormattedExclusions.csv"));
        when(params.settings()).thenReturn(Optional.of(settings));

        assertThatExceptionOfType(RuntimeException.class).isThrownBy(() -> factory.createInterceptor(params))
                .withMessageContaining("Failed to read CSV file");
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = " ")
    void shouldThrowExceptionWhenCsvPathIsNullOrBlank(final String value) {
        InterceptorParameters params = mock(InterceptorParameters.class);
        FeatureSetting settings = mock(FeatureSetting.class);
        when(settings.getString("csvFile")).thenReturn(Optional.ofNullable(value));
        when(params.settings()).thenReturn(Optional.of(settings));

        assertThatThrownBy(() -> factory.createInterceptor(params))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("CSV", "missing", "empty");
    }

    @Test
    void shouldReturnCorrectEntriesWhenCsvIsValid() throws URISyntaxException {
        List<CsvExclusionEntry> entries = getEntriesFromFile("validFormattedExclusions.csv");

        assertThat(entries)
                .hasSize(8)
                .extracting("className", "mutationName", "startLine", "endLine")
                .containsExactly(
                        tuple("ClassName", Optional.empty(), Optional.empty(), Optional.empty()),
                        tuple("ClassName", Optional.of("Mutator"), Optional.empty(), Optional.empty()),
                        tuple("ClassName", Optional.of("Mutator"), Optional.of(5), Optional.empty()),
                        tuple("ClassName", Optional.of("Mutator"), Optional.empty(), Optional.of(10)),
                        tuple("ClassName", Optional.of("Mutator"), Optional.of(5), Optional.of(10)),
                        tuple("ClassName", Optional.empty(), Optional.of(5), Optional.empty()),
                        tuple("ClassName", Optional.empty(), Optional.empty(), Optional.of(10)),
                        tuple("ClassName", Optional.empty(), Optional.of(5), Optional.of(10)));
    }

    @Test
    void shouldNotAddEntriesWithoutClassName() throws URISyntaxException {
        List<CsvExclusionEntry> entries = getEntriesFromFile("ignoredButValidFormattedExclusions.csv");
        assertThat(entries).isEmpty();
    }

    @Test
    void shouldNotAddEntriesWhenExclusionsAreInvalid() throws URISyntaxException {
        List<CsvExclusionEntry> entries = getEntriesFromFile("invalidExclusions.csv");
        assertThat(entries).isEmpty();

        entries = getEntriesFromFile("wrongFormattedExclusions.csv");
        assertThat(entries).isEmpty();
    }

    @Test
    void shouldTestProvidesFeature() {
        Feature feature = factory.provides();
        assertThat(feature.name()).isEqualToIgnoringCase("FCSV");
        assertThat(feature.description()).contains("CSV file");
        assertThat(feature.isOnByDefault()).isFalse();
    }

    @Test
    void descriptionShouldReturnDescription() {
        assertThat(factory.description()).contains("CSV file");
    }

    List<CsvExclusionEntry> getEntriesFromFile(final String fileName) throws URISyntaxException {
        URL resource = getClass().getClassLoader().getResource(fileName);

        assertThat(resource).isNotNull();
        Path path = Paths.get(resource.toURI());

        return factory.getCsvExclusionEntries(path.toString());
    }
}
