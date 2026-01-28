package edu.hm.hafner.pitmute;

import org.pitest.mutationtest.build.InterceptorParameters;
import org.pitest.mutationtest.build.MutationInterceptor;
import org.pitest.mutationtest.build.MutationInterceptorFactory;
import org.pitest.plugin.Feature;

/**
 * Factory for creating an {@link AnnotationExclusionFilter} instance to exclude mutations in areas
 * marked with the {@link SuppressMutation} annotation.
 *
 * <p>
 * For details on usage and configuration, please refer to the project's README.
 * </p>
 */
public class AnnotationExclusionFilterFactory implements MutationInterceptorFactory {
    @Override
    public MutationInterceptor createInterceptor(InterceptorParameters interceptorParameters) {
        return new AnnotationExclusionFilter();
    }

    @Override
    public Feature provides() {
        return Feature.named("FANNOT")
                .withDescription("Exclude mutations based on annotations")
                .withOnByDefault(false);
    }

    @Override
    public String description() {
        return "Exclude mutations based on annotations";
    }
}

