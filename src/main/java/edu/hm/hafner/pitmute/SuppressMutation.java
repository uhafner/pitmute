package edu.hm.hafner.pitmute;

import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;


/**
 * Suppresses specific mutations when the feature defined in {@link AnnotationExclusionFilterFactory} is enabled.
 *
 * <p>
 * This annotation can be applied to classes or methods. When used without parameters all mutations in that scope are suppressed.
 * </p>
 */
@Retention(RetentionPolicy.RUNTIME)
@Repeatable(SuppressMutations.class)
public @interface SuppressMutation {
    /**
     * Specifies a mutator to be ignored. If left empty, all mutators in the scope are suppressed.
     *
     * @return the name of the mutator to suppress
     */
    String mutator() default "";

    /**
     * Limits the suppression to a specific line number.
     *
     * @return the line number to suppress, or -1 for all lines
     */
    int line() default -1;
}
