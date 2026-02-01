package edu.hm.hafner.pitmute;

import org.junit.jupiter.api.Test;
import org.pitest.mutationtest.build.InterceptorParameters;
import org.pitest.mutationtest.build.MutationInterceptor;
import org.pitest.plugin.Feature;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

class AnnotationExclusionFilterFactoryTest {
    private final AnnotationExclusionFilterFactory factory = new AnnotationExclusionFilterFactory();

    @Test
    void shouldCreateAnnotationExclusionFilter() {
        InterceptorParameters interceptorParameters = mock(InterceptorParameters.class);
        MutationInterceptor interceptor = factory.createInterceptor(interceptorParameters);

        assertThat(interceptor).isNotNull();
        assertThat(interceptor).isExactlyInstanceOf(AnnotationExclusionFilter.class);
        verifyNoInteractions(interceptorParameters);
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
