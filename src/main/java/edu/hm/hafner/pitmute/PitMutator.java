package edu.hm.hafner.pitmute;

/**
 * Represents the mutators available in PIT.
 * This enum maps each mutator name to its fully qualified class name.
 */
public enum PitMutator {
    CONDITIONALS_BOUNDARY("org.pitest.mutationtest.engine.gregor.mutators.ConditionalsBoundaryMutator"),
    CONSTRUCTOR_CALLS("org.pitest.mutationtest.engine.gregor.mutators.ConstructorCallMutator"),
    INCREMENTS("org.pitest.mutationtest.engine.gregor.mutators.IncrementsMutator"),
    INLINE_CONSTS("org.pitest.mutationtest.engine.gregor.mutators.InlineConstantMutator"),
    INVERT_NEGS("org.pitest.mutationtest.engine.gregor.mutators.InvertNegsMutator"),
    MATH("org.pitest.mutationtest.engine.gregor.mutators.MathMutator"),
    VOID_METHOD_CALLS("org.pitest.mutationtest.engine.gregor.mutators.VoidMethodCallMutator"),
    NEGATE_CONDITIONALS("org.pitest.mutationtest.engine.gregor.mutators.NegateConditionalsMutator"),
    NON_VOID_METHOD_CALLS("org.pitest.mutationtest.engine.gregor.mutators.NonVoidMethodCallMutator"),
    FALSE_RETURNS("org.pitest.mutationtest.engine.gregor.mutators.returns.BooleanFalseReturnValsMutator"),
    TRUE_RETURNS("org.pitest.mutationtest.engine.gregor.mutators.returns.BooleanTrueReturnValsMutator"),
    EMPTY_RETURNS("org.pitest.mutationtest.engine.gregor.mutators.returns.EmptyObjectReturnValsMutator"),
    NULL_RETURNS("org.pitest.mutationtest.engine.gregor.mutators.returns.NullReturnValsMutator"),
    PRIMITIVE_RETURNS("org.pitest.mutationtest.engine.gregor.mutators.returns.PrimitiveReturnsMutator"),
    EXPERIMENTAL_ARGUMENT_PROPAGATION("org.pitest.mutationtest.engine.gregor.mutators.experimental.ArgumentPropagationMutator"),
    EXPERIMENTAL_BIG_DECIMAL("org.pitest.mutationtest.engine.gregor.mutators.experimental.BigDecimalMutator"),
    EXPERIMENTAL_BIG_INTEGER("org.pitest.mutationtest.engine.gregor.mutators.experimental.BigIntegerMutator"),
    EXPERIMENTAL_MEMBER_VARIABLE("org.pitest.mutationtest.engine.gregor.mutators.experimental.MemberVariableMutator"),
    EXPERIMENTAL_NAKED_RECEIVER("org.pitest.mutationtest.engine.gregor.mutators.experimental.NakedReceiverMutator"),
    REMOVE_INCREMENTS("org.pitest.mutationtest.engine.gregor.mutators.experimental.RemoveIncrementsMutator"),
//    REMOVE_CONDITIONALS_EQUAL_IF,
//    REMOVE_CONDITIONALS_EQUAL_ELSE,
//    REMOVE_CONDITIONALS_ORDER_IF,
//    REMOVE_CONDITIONALS_ORDER_ELSE,
    EXPERIMENTAL_SWITCH("org.pitest.mutationtest.engine.gregor.mutators.experimental.SwitchMutator"),
    NONE("");

    private final String fqcn;

    /**
     * Constructor to associate a mutator constant with its fqcn.
     *
     * @param fqcn the fully qualified class name of the mutator
     */
    PitMutator(final String fqcn) {
        this.fqcn = fqcn;
    }

    public String getFqcn() {
        return fqcn;
    }
}
