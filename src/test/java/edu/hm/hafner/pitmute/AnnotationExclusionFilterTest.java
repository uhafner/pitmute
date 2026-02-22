package edu.hm.hafner.pitmute;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.objectweb.asm.tree.AnnotationNode;
import org.pitest.bytecode.analysis.ClassTree;
import org.pitest.bytecode.analysis.MethodTree;
import org.pitest.classinfo.ClassName;
import org.pitest.mutationtest.build.InterceptorType;
import org.pitest.mutationtest.engine.Location;
import org.pitest.mutationtest.engine.Mutater;
import org.pitest.mutationtest.engine.MutationDetails;
import org.pitest.mutationtest.engine.MutationIdentifier;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

class AnnotationExclusionFilterTest {
    private static final String TEST_CLASS_FQCN = "com.example.TestClass";
    private final AnnotationExclusionFilter filter = new AnnotationExclusionFilter();
    private final Mutater mutater = mock(Mutater.class);
    private static final String SUPPRESS_MUTATION_DESC = "Lany/package/SuppressMutation;";
    private static final String SUPPRESS_MUTATIONS_DESC = "Lany/package/SuppressMutations;";
    private static final String PIT_MUTATOR_FQCN = "Ledu/hm/hafner/pitmute/PitMutator;";
    private static final String MATH_MUTATOR_FQCN = "org.pitest.mutationtest.engine.gregor.mutators.MathMutator";
    private static final String PRIMITIVE_RETURNS_MUTATOR_FQCN = "org.pitest.mutationtest.engine.gregor.mutators.returns.PrimitiveReturnsMutator";
    private static final String NEGATE_CONDITIONALS_MUTATOR_FQCN = "org.pitest.mutationtest.engine.gregor.mutators.NegateConditionalsMutator";
    private static final String MUTATOR_NAME = "mutatorName";
    private static final String MUTATOR = "mutator";
    private static final String LINE = "line";
    private static final int FIRST_LINE = 1;
    private static final String ANY_METHOD_DESC = "(II)I";
    private static final String THREE_INT_TO_INT_DESC = "(III)I";
    private static final String EMPTY_TO_VOID_DESC = "()V";

    @ParameterizedTest(name = "{index} => mutatorName: {0}")
    @ValueSource(strings = {"Math", "MathMutator", MATH_MUTATOR_FQCN})
    void shouldOnlySuppressMutationIfClassHasAnnotationWithMatchingMutatorName(final String strings) {
        ClassTree classTree = createClassTree(TEST_CLASS_FQCN);

        var annotations = List.of(createAnnotation(MUTATOR_NAME, strings));
        when(classTree.annotations()).thenReturn(annotations);
        when(classTree.methods()).thenReturn(List.of());

        filter.begin(classTree);

        MutationDetails matchingMutation = createMutation(TEST_CLASS_FQCN, "anyMethod", MATH_MUTATOR_FQCN);
        MutationDetails notMatchingMutation = createMutation(TEST_CLASS_FQCN, "anyMethod", PRIMITIVE_RETURNS_MUTATOR_FQCN);

        Collection<MutationDetails> remainingMutations = filter.intercept(List.of(matchingMutation, notMatchingMutation), mutater);

        assertThat(remainingMutations).containsExactly(notMatchingMutation);
    }

    @Test
    void shouldNotSuppressMutationsInSameNamedClassInDifferentPackage() {
        ClassTree classTree = createClassTree(TEST_CLASS_FQCN);
        ClassTree anotherClassTree = createClassTree("com.example.otherPath.TestClass");

        var annotations = List.of(createAnnotation(MUTATOR_NAME, "Math"));
        when(classTree.annotations()).thenReturn(annotations);
        when(classTree.methods()).thenReturn(List.of());

        when(anotherClassTree.annotations()).thenReturn(List.of());
        when(anotherClassTree.methods()).thenReturn(List.of());

        filter.begin(classTree);
        filter.begin(anotherClassTree);

        MutationDetails mathMutation = createMutation("com.example.otherPath.TestClass", "anyMethod", MATH_MUTATOR_FQCN);

        Collection<MutationDetails> remainingMutations = filter.intercept(List.of(mathMutation), mutater);

        assertThat(remainingMutations).containsExactly(mathMutation);
    }

    @Test
    void shouldSuppressAllMutationsWhenClassHasAnnotationWithoutValues() {
        ClassTree classTree = createClassTree(TEST_CLASS_FQCN);
        var annotations = List.of(createAnnotation());
        when(classTree.annotations()).thenReturn(annotations);
        MethodTree anyMethod = createMethodTree(classTree, "anyMethod");
        MethodTree anyOtherMethod = createMethodTree(classTree, "anyOtherMethod");
        when(classTree.methods()).thenReturn(List.of(anyMethod, anyOtherMethod));

        filter.begin(classTree);

        MutationDetails mutation = createMutation(TEST_CLASS_FQCN, "anyMethod", "AnyMutator");
        MutationDetails otherMutation = createMutation(TEST_CLASS_FQCN, "anyOtherMethod", "AnyOtherMutator");

        Collection<MutationDetails> remainingMutations = filter.intercept(List.of(), mutater);
        assertThat(remainingMutations).isEmpty();

        remainingMutations = filter.intercept(List.of(mutation), mutater);
        assertThat(remainingMutations).isEmpty();

        remainingMutations = filter.intercept(List.of(mutation, otherMutation), mutater);
        assertThat(remainingMutations).isEmpty();
    }

    @Test
    void shouldSuppressAllMutationsInMethodWhenMethodHasAnnotationWithoutValues() {
        ClassTree classTree = createClassTree(TEST_CLASS_FQCN);
        when(classTree.annotations()).thenReturn(List.of());

        MethodTree methodTree = createMethodTree(classTree, "annotatedMethod");
        var annotations = List.of(createAnnotation());
        when(methodTree.annotations()).thenReturn(annotations);

        filter.begin(classTree);
        MutationDetails mutation = createMutation(TEST_CLASS_FQCN, "annotatedMethod", MATH_MUTATOR_FQCN);
        Collection<MutationDetails> remainingMutations = filter.intercept(List.of(mutation), mutater);

        assertThat(remainingMutations).isEmpty();
    }

    @Test
    void shouldSuppressMutationInMethodWhenMethodHasAnnotationWithMatchingMutatorName() {
        ClassTree classTree = createClassTree(TEST_CLASS_FQCN);
        when(classTree.annotations()).thenReturn(List.of());

        MethodTree methodTree = createMethodTree(classTree, "annotatedMethod");
        var annotations = List.of(createAnnotation(MUTATOR_NAME, "Math"));
        when(methodTree.annotations()).thenReturn(annotations);

        filter.begin(classTree);
        MutationDetails matchingMutation = createMutation(TEST_CLASS_FQCN, "annotatedMethod", MATH_MUTATOR_FQCN);
        MutationDetails notMatchingMutation = createMutation(TEST_CLASS_FQCN, "annotatedMethod", PRIMITIVE_RETURNS_MUTATOR_FQCN);
        Collection<MutationDetails> remainingMutations = filter.intercept(List.of(matchingMutation, notMatchingMutation), mutater);

        assertThat(remainingMutations).containsExactly(notMatchingMutation);
    }

    @Test
    void shouldSuppressMutationsInMethodCorrectlyForMethodWithMultipleAnnotations() {
        ClassTree classTree = createClassTree(TEST_CLASS_FQCN);
        when(classTree.annotations()).thenReturn(List.of());

        MethodTree methodTree = createMethodTree(classTree, "annotatedMethod");
        var annotations = List.of(createContainerAnnotation(List.of(List.of(MUTATOR_NAME, "Math"), List.of(MUTATOR_NAME, "PrimitiveReturns"))));
        when(methodTree.annotations()).thenReturn(annotations);

        filter.begin(classTree);
        MutationDetails mathMutation = createMutation(TEST_CLASS_FQCN, "annotatedMethod", MATH_MUTATOR_FQCN);
        MutationDetails primitiveReturnsMutation = createMutation(TEST_CLASS_FQCN, "annotatedMethod", PRIMITIVE_RETURNS_MUTATOR_FQCN);
        Collection<MutationDetails> remainingMutations = filter.intercept(List.of(mathMutation, primitiveReturnsMutation), mutater);

        assertThat(remainingMutations).isEmpty();
    }

    @Test
    void shouldNotSuppressMutationFromOtherNotAnnotatedMethodWhenMutatorNameIsMatching() {
        ClassTree classTree = createClassTree(TEST_CLASS_FQCN);
        when(classTree.annotations()).thenReturn(List.of());

        MethodTree methodTree = createMethodTree(classTree, "annotatedMethod");
        var annotations = List.of(createAnnotation(MUTATOR_NAME, "Math"));
        when(methodTree.annotations()).thenReturn(annotations);

        filter.begin(classTree);
        MutationDetails mutationInOtherMethod = createMutation(TEST_CLASS_FQCN, "otherMethod", MATH_MUTATOR_FQCN);
        Collection<MutationDetails> remainingMutations = filter.intercept(List.of(mutationInOtherMethod), mutater);

        assertThat(remainingMutations).containsExactly(mutationInOtherMethod);
    }

    @Test
    void shouldSuppressMutationsWithClassAndMethodLevelAnnotations() {
        ClassTree classTree = createClassTree(TEST_CLASS_FQCN);
        var classAnnotations = List.of(createAnnotation(MUTATOR_NAME, "Math"));
        when(classTree.annotations()).thenReturn(classAnnotations);

        MethodTree methodTree = createMethodTree(classTree, "annotatedMethod");
        var annotations = List.of(createContainerAnnotation(List.of(List.of(MUTATOR_NAME, "Math"), List.of(MUTATOR_NAME, "PrimitiveReturns"), List.of(MUTATOR_NAME, "NegateConditionals"))));
        when(methodTree.annotations()).thenReturn(annotations);

        filter.begin(classTree);
        MutationDetails mathMutation = createMutation(TEST_CLASS_FQCN, "annotatedMethod", MATH_MUTATOR_FQCN);
        MutationDetails primitiveReturnsMutation = createMutation(TEST_CLASS_FQCN, "annotatedMethod", PRIMITIVE_RETURNS_MUTATOR_FQCN);
        MutationDetails negateConditionalsMutation = createMutation(TEST_CLASS_FQCN, "otherMethod", NEGATE_CONDITIONALS_MUTATOR_FQCN);
        MutationDetails mathMutationInOtherMethod = createMutation(TEST_CLASS_FQCN, "otherMethod", MATH_MUTATOR_FQCN);
        Collection<MutationDetails> remainingMutations = filter.intercept(List.of(mathMutation, primitiveReturnsMutation, negateConditionalsMutation, mathMutationInOtherMethod), mutater);

        assertThat(remainingMutations).containsExactly(negateConditionalsMutation);
    }

    @Test
    void shouldNotSuppressMutationsForInvalidAnnotationsWithOddValuesSize() {
        ClassTree classTree = createClassTree(TEST_CLASS_FQCN);
        when(classTree.annotations()).thenReturn(List.of());

        MethodTree methodTreeWithInvalidContainer = createMethodTree(classTree, "methodWithInvalidContainer");
        var invalidContainerAnnotation = List.of(createContainerAnnotation(List.of(List.of(MUTATOR_NAME))));
        when(methodTreeWithInvalidContainer.annotations()).thenReturn(invalidContainerAnnotation);

        assertThatExceptionOfType(IllegalStateException.class).isThrownBy(() -> filter.begin(classTree))
                .withMessageContaining("Invalid ASM AnnotationNode");

        invalidContainerAnnotation = List.of(createContainerAnnotation(List.of(List.of(MUTATOR_NAME, "NegateConditionals", LINE))));
        when(methodTreeWithInvalidContainer.annotations()).thenReturn(invalidContainerAnnotation);

        assertThatExceptionOfType(IllegalStateException.class).isThrownBy(() -> filter.begin(classTree))
                .withMessageContaining("Invalid ASM AnnotationNode");
    }

    @Test
    void shouldSuppressMutationsCorrectlyForAnnotationsWithMutatorNameAndLineParameter() {
        ClassTree classTree = createClassTree(TEST_CLASS_FQCN);
        when(classTree.annotations()).thenReturn(List.of());

        MethodTree methodTree = createMethodTree(classTree, "annotatedMethod");
        var annotations = List.of(createContainerAnnotation(List.of(List.of(MUTATOR_NAME, "Math", LINE, 5), List.of(LINE, 2, MUTATOR_NAME, "PrimitiveReturns"), List.of(LINE, 3))));
        when(methodTree.annotations()).thenReturn(annotations);

        filter.begin(classTree);
        MutationDetails mathMutation = createMutation(TEST_CLASS_FQCN, "annotatedMethod", MATH_MUTATOR_FQCN, 5);
        MutationDetails primitiveReturnsMutation = createMutation(TEST_CLASS_FQCN, "annotatedMethod", PRIMITIVE_RETURNS_MUTATOR_FQCN, 7);
        MutationDetails negateConditionalsMutation = createMutation(TEST_CLASS_FQCN, "annotatedMethod", NEGATE_CONDITIONALS_MUTATOR_FQCN, 3);
        Collection<MutationDetails> remainingMutations = filter.intercept(List.of(mathMutation, primitiveReturnsMutation, negateConditionalsMutation), mutater);

        assertThat(remainingMutations).containsExactly(primitiveReturnsMutation);
    }

    @Test
    void shouldSuppressMutationsCorrectlyForAnnotationsWithLineParameter() {
        ClassTree classTree = createClassTree(TEST_CLASS_FQCN);
        when(classTree.annotations()).thenReturn(List.of());

        MethodTree methodTree = createMethodTree(classTree, "annotatedMethod");
        var annotations = List.of(createContainerAnnotation(List.of(List.of(LINE, 5), List.of(MUTATOR_NAME, "Math", LINE, 5),
                List.of(LINE, 10), List.of(MUTATOR_NAME, "Math", LINE, 3))));
        when(methodTree.annotations()).thenReturn(annotations);

        filter.begin(classTree);
        MutationDetails mathMutation = createMutation(TEST_CLASS_FQCN, "annotatedMethod", MATH_MUTATOR_FQCN, 5);
        MutationDetails negateConditionalsMutation = createMutation(TEST_CLASS_FQCN, "annotatedMethod", NEGATE_CONDITIONALS_MUTATOR_FQCN, 5);
        MutationDetails otherMathMutation = createMutation(TEST_CLASS_FQCN, "annotatedMethod", MATH_MUTATOR_FQCN, 3);
        MutationDetails primitiveReturnsMutation = createMutation(TEST_CLASS_FQCN, "otherMethod", PRIMITIVE_RETURNS_MUTATOR_FQCN, 10);
        Collection<MutationDetails> remainingMutations = filter.intercept(List.of(mathMutation, negateConditionalsMutation, primitiveReturnsMutation, otherMathMutation), mutater);

        assertThat(remainingMutations).containsExactly(primitiveReturnsMutation);
    }

    @Test
    void shouldDistinguishOverloadedMethodsByDescriptor() {
        ClassTree classTree = createClassTree(TEST_CLASS_FQCN);
        when(classTree.annotations()).thenReturn(List.of());

        MethodTree methodTree = createMethodTree(classTree, "method", THREE_INT_TO_INT_DESC);
        var annotation = List.of(createContainerAnnotation(List.of(List.of(MUTATOR_NAME, "Math"), List.of(MUTATOR_NAME, "NegateConditionals"))));
        when(methodTree.annotations()).thenReturn(annotation);

        MethodTree methodTreeWithoutAnnotations = createMethodTree(classTree, "method", EMPTY_TO_VOID_DESC);
        when(methodTreeWithoutAnnotations.annotations()).thenReturn(List.of());

        when(classTree.methods()).thenReturn(List.of(methodTree, methodTreeWithoutAnnotations));

        filter.begin(classTree);
        MutationDetails mathMutation = createMutation(TEST_CLASS_FQCN, "method", MATH_MUTATOR_FQCN, 10, THREE_INT_TO_INT_DESC);
        MutationDetails mathMutationInOtherMethod = createMutation(TEST_CLASS_FQCN, "method", MATH_MUTATOR_FQCN, 50, EMPTY_TO_VOID_DESC);
        MutationDetails negateConditionalsMutationInOtherMethod = createMutation(TEST_CLASS_FQCN, "method", NEGATE_CONDITIONALS_MUTATOR_FQCN, 51, EMPTY_TO_VOID_DESC);
        Collection<MutationDetails> remainingMutations = filter.intercept(List.of(mathMutation, mathMutationInOtherMethod, negateConditionalsMutationInOtherMethod), mutater);

        assertThat(remainingMutations).containsExactly(mathMutationInOtherMethod, negateConditionalsMutationInOtherMethod);
    }

    @Test
    void shouldIgnoreMutatorNameIfBothMutatorNameAndMutatorAreProvided() {
        ClassTree classTree = createClassTree(TEST_CLASS_FQCN);
        when(classTree.annotations()).thenReturn(List.of());

        MethodTree methodTree = createMethodTree(classTree, "method", ANY_METHOD_DESC);
        String[] mathEnum = {PIT_MUTATOR_FQCN, String.valueOf(PitMutator.MATH)};
        var annotations = List.of(createContainerAnnotation(List.of(List.of(MUTATOR_NAME, "NegateConditionals", MUTATOR, mathEnum))));
        when(methodTree.annotations()).thenReturn(annotations);

        filter.begin(classTree);
        MutationDetails mathMutation = createMutation(TEST_CLASS_FQCN, "method", MATH_MUTATOR_FQCN, 10, ANY_METHOD_DESC);
        MutationDetails negateConditionalsMutationInOtherMethod = createMutation(TEST_CLASS_FQCN, "method", NEGATE_CONDITIONALS_MUTATOR_FQCN, 15, ANY_METHOD_DESC);
        Collection<MutationDetails> remainingMutations = filter.intercept(List.of(mathMutation, negateConditionalsMutationInOtherMethod), mutater);

        assertThat(remainingMutations).containsExactly(negateConditionalsMutationInOtherMethod);
    }

    @Test
    void shouldNotRemoveMutationIfInvalidIntegerIsProvided() {
        ClassTree classTree = createClassTree(TEST_CLASS_FQCN);
        when(classTree.annotations()).thenReturn(List.of());

        MethodTree methodTree = createMethodTree(classTree, "method", ANY_METHOD_DESC);
        String[] mathEnum = {PIT_MUTATOR_FQCN, String.valueOf(PitMutator.MATH)};
        var annotations = List.of(createContainerAnnotation(List.of(List.of(LINE, "InvalidInteger"),
                List.of(MUTATOR, mathEnum, LINE, "otherInvalidInteger"), List.of(MUTATOR_NAME, "NegateConditionals", LINE, "A"))));
        when(methodTree.annotations()).thenReturn(annotations);

        filter.begin(classTree);
        MutationDetails mathMutation = createMutation(TEST_CLASS_FQCN, "method", MATH_MUTATOR_FQCN, 10, ANY_METHOD_DESC);
        MutationDetails negateConditionalsMutationInOtherMethod = createMutation(TEST_CLASS_FQCN, "method", NEGATE_CONDITIONALS_MUTATOR_FQCN, 15, ANY_METHOD_DESC);
        Collection<MutationDetails> remainingMutations = filter.intercept(List.of(mathMutation, negateConditionalsMutationInOtherMethod), mutater);

        assertThat(remainingMutations).containsExactly(mathMutation, negateConditionalsMutationInOtherMethod);
    }

    @ParameterizedTest
    @MethodSource("provideTestCases")
    void shouldSuppressMutationCorrectlyForAnnotations(final AnnotationNode methodAnnotations, final List<MutationDetails> mutations, final List<MutationDetails> expectedRemainingMutations) {
        ClassTree classTree = createClassTree(TEST_CLASS_FQCN);
        when(classTree.annotations()).thenReturn(List.of());

        MethodTree methodTree = createMethodTree(classTree, "method", THREE_INT_TO_INT_DESC);
        when(methodTree.annotations()).thenReturn(List.of(methodAnnotations));
        when(classTree.methods()).thenReturn(List.of(methodTree));

        filter.begin(classTree);
        Collection<MutationDetails> remainingMutations = filter.intercept(mutations, mutater);

        assertThat(remainingMutations).containsExactlyElementsOf(expectedRemainingMutations);
    }

    private static Stream<Arguments> provideTestCases() {
        int matchLine = 5;
        int otherLine = 6;
        MutationDetails mathMutation = createMutation(TEST_CLASS_FQCN, "method", MATH_MUTATOR_FQCN, matchLine, THREE_INT_TO_INT_DESC);
        MutationDetails negateConditionalsMutation = createMutation(TEST_CLASS_FQCN, "method", NEGATE_CONDITIONALS_MUTATOR_FQCN, matchLine, THREE_INT_TO_INT_DESC);

        var mutations = List.of(mathMutation, negateConditionalsMutation);
        var bothMutations = List.of(mathMutation, negateConditionalsMutation);
        var emptyList = List.of();
        var onlyNegateMutation = List.of(negateConditionalsMutation);

        String[] matchMutator = {PIT_MUTATOR_FQCN, String.valueOf(PitMutator.MATH)};
        String[] otherMutator = {PIT_MUTATOR_FQCN, String.valueOf(PitMutator.PRIMITIVE_RETURNS)};
        String matchName = MATH_MUTATOR_FQCN;
        String otherName = "PrimitiveReturns";

        return Stream.of(
                Arguments.of(createAnnotation(), mutations, emptyList),
                Arguments.of(createAnnotation(LINE, matchLine), mutations, emptyList),
                Arguments.of(createAnnotation(LINE, otherLine), mutations, bothMutations),
                Arguments.of(createAnnotation(LINE, matchLine, MUTATOR_NAME, matchName), mutations, onlyNegateMutation),
                Arguments.of(createAnnotation(LINE, matchLine, MUTATOR_NAME, otherName), mutations, bothMutations),
                Arguments.of(createAnnotation(LINE, otherLine, MUTATOR_NAME, matchName), mutations, bothMutations),
                Arguments.of(createAnnotation(LINE, otherLine, MUTATOR_NAME, otherName), mutations, bothMutations),
                Arguments.of(createAnnotation(LINE, matchLine, MUTATOR_NAME, matchName, MUTATOR, matchMutator), mutations, onlyNegateMutation),
                Arguments.of(createAnnotation(LINE, matchLine, MUTATOR_NAME, matchName, MUTATOR, otherMutator), mutations, bothMutations),
                Arguments.of(createAnnotation(LINE, matchLine, MUTATOR_NAME, otherName, MUTATOR, matchMutator), mutations, onlyNegateMutation),
                Arguments.of(createAnnotation(LINE, matchLine, MUTATOR_NAME, otherName, MUTATOR, otherMutator), mutations, bothMutations),
                Arguments.of(createAnnotation(LINE, otherLine, MUTATOR_NAME, matchName, MUTATOR, matchMutator), mutations, bothMutations),
                Arguments.of(createAnnotation(LINE, otherLine, MUTATOR_NAME, matchName, MUTATOR, otherMutator), mutations, bothMutations),
                Arguments.of(createAnnotation(LINE, otherLine, MUTATOR_NAME, otherName, MUTATOR, matchMutator), mutations, bothMutations),
                Arguments.of(createAnnotation(LINE, otherLine, MUTATOR_NAME, otherName, MUTATOR, otherMutator), mutations, bothMutations),
                Arguments.of(createAnnotation(MUTATOR_NAME, matchName, MUTATOR, matchMutator), mutations, onlyNegateMutation),
                Arguments.of(createAnnotation(MUTATOR_NAME, matchName, MUTATOR, otherMutator), mutations, bothMutations),
                Arguments.of(createAnnotation(MUTATOR_NAME, otherName, MUTATOR, matchMutator), mutations, onlyNegateMutation),
                Arguments.of(createAnnotation(MUTATOR_NAME, otherName, MUTATOR, otherMutator), mutations, bothMutations)
        );
    }

    @Test
    void shouldReturnCorrectType() {
        assertThat(filter.type()).isEqualTo(InterceptorType.FILTER);
    }

    private static ClassTree createClassTree(final String fqcn) {
        ClassTree classTree = mock(ClassTree.class);
        when(classTree.name()).thenReturn(ClassName.fromString(fqcn));

        return classTree;
    }

    private static MethodTree createMethodTree(final ClassTree classTree, final String methodName) {
        return createMethodTree(classTree, methodName, ANY_METHOD_DESC);
    }

    private static MethodTree createMethodTree(final ClassTree classTree, final String methodName, final String descriptor) {
        MethodTree methodTree = mock(MethodTree.class);
        Location location = mock(Location.class);
        when(methodTree.asLocation()).thenReturn(location);
        when(location.getMethodName()).thenReturn(methodName);
        when(location.getMethodDesc()).thenReturn(descriptor);

        when(classTree.methods()).thenReturn(List.of(methodTree));

        return methodTree;
    }

    private static AnnotationNode createAnnotation(final Object... values) {
        var annotation = new AnnotationNode(SUPPRESS_MUTATION_DESC);
        annotation.values = List.of(values);
        return annotation;
    }

    private static AnnotationNode createContainerAnnotation(final List<List<Object>> values) {
        var container = new AnnotationNode(SUPPRESS_MUTATIONS_DESC);
        container.values = new ArrayList<>();
        container.values.add("value");

        List<AnnotationNode> annotations = new ArrayList<>();
        for (List<Object> value : values) {
            annotations.add(createAnnotation(value.toArray()));
        }

        container.values.add(annotations);
        return container;
    }

    private MutationDetails createMutation(final String className, final String methodName, final String mutatorFqcn) {
        return createMutation(className, methodName, mutatorFqcn, FIRST_LINE, ANY_METHOD_DESC);
    }

    private MutationDetails createMutation(final String className, final String methodName, final String mutatorFqcn, final int lineNumber) {
        return createMutation(className, methodName, mutatorFqcn, lineNumber, ANY_METHOD_DESC);
    }

    private static MutationDetails createMutation(final String className, final String methodName, final String mutatorFqcn, final int lineNumber, final String descriptor) {
        var id = new MutationIdentifier(
                Location.location(ClassName.fromString(className), methodName, descriptor), 0, mutatorFqcn
        );
        return new MutationDetails(id, "File.java", "desc", lineNumber, 0);
    }
}
