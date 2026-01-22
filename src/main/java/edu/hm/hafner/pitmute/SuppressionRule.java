package edu.hm.hafner.pitmute;

import java.util.Optional;

/**
 * Represents a suppression rule derived from a {@link SuppressMutation} annotation.
 * Used by the {@link AnnotationExclusionFilter} to determine the scope (class or method) and optionally the type of mutator or line to be ignored.
 *
 * @param className   the name of the class where the annotation is located and applied
 * @param methodName  the name of the method if the annotation is a method-level annotation
 * @param mutatorName the mutator to be ignored if provided as an annotation parameter
 * @param line        the line to be ignored if provided as an annotation parameter
 */
public record SuppressionRule(String className, Optional<String> methodName, Optional<String> mutatorName,
                              Optional<Integer> line) {
}
