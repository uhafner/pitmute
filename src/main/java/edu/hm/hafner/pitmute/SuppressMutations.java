package edu.hm.hafner.pitmute;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Container annotation for repeating the {@link SuppressMutation} annotation.
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface SuppressMutations {
    /**
     * The array of {@link SuppressMutation} annotations.
     *
     * @return an array of suppression rules
     */
    SuppressMutation[] value();
}
