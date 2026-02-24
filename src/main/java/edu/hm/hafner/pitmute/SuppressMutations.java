package edu.hm.hafner.pitmute;

import java.lang.annotation.*;

/**
 * Container annotation for repeating the {@link SuppressMutation} annotation.
 */
@Target({ElementType.METHOD, ElementType.TYPE, ElementType.CONSTRUCTOR})
@Retention(RetentionPolicy.RUNTIME)
public @interface SuppressMutations {
    /**
     * The array of {@link SuppressMutation} annotations.
     *
     * @return an array of suppression rules
     */
    SuppressMutation[] value();
}
