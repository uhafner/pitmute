package edu.hm.hafner.pitmute;

import org.junit.jupiter.api.Test;
import org.pitest.mutationtest.build.MutationInterceptor;
import org.pitest.plugin.Feature;

import static org.assertj.core.api.Assertions.*;

class AnnotationExclusionFilterFactoryTest {
    private final AnnotationExclusionFilterFactory factory = new AnnotationExclusionFilterFactory();

    @Test
    void shouldCreateAnnotationExclusionFilter() {
        MutationInterceptor interceptor = factory.createInterceptor(null);

        assertThat(interceptor).isNotNull();
        assertThat(interceptor).isExactlyInstanceOf(AnnotationExclusionFilter.class);
    }

    @Test
    void shouldProvideCorrectFeature() {
        Feature feature = factory.provides();

        assertThat(feature.name()).isEqualToIgnoringCase("FANNOT");
        assertThat(feature.description()).contains("annotation");
        assertThat(feature.isOnByDefault()).isFalse();
    }

    @Test
    void descriptionShouldReturnDescription() {
        assertThat(factory.description()).contains("annotation");
    }
}
