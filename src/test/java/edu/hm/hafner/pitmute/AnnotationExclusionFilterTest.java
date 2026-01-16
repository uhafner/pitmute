package edu.hm.hafner.pitmute;

import org.junit.jupiter.api.Test;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class AnnotationExclusionFilterTest {
    private final AnnotationExclusionFilter filter = new AnnotationExclusionFilter();
    private final Mutater mutater = mock(Mutater.class);
    private static final String SUPPRESS_MUTATION_DESC = "Ledu/hm/hafner/pitmute/SuppressMutation;";
    private static final String MATH_MUTATOR_FQCN = "org.pitest.mutationtest.engine.gregor.mutators.MathMutator";
    private static final String PRIMITIVE_RETURNS_MUTATOR_FQCN = "org.pitest.mutationtest.engine.gregor.mutators.returns.PrimitiveReturnsMutator";
    private static final String NEGATE_CONDITIONALS_MUTATOR_FQCN = "org.pitest.mutationtest.engine.gregor.mutators.NegateConditionalsMutator";

    @Test
    void shouldOnlySuppressMutationIfClassHasAnnotationWithMatchingMutatorValue() {
        ClassTree classTree = createClassTree("com.example.TestClass");

        var annotations = createAnnotations(List.of(List.of("mutator", "Math")));
        when(classTree.annotations()).thenReturn(annotations);
        when(classTree.methods()).thenReturn(List.of());

        filter.begin(classTree);

        MutationDetails matchingMutation = createMutation("com.example.TestClass", "anyMethod", MATH_MUTATOR_FQCN);
        MutationDetails notMatchingMutation = createMutation("com.example.TestClass", "anyMethod", PRIMITIVE_RETURNS_MUTATOR_FQCN);

        Collection<MutationDetails> remainingMutations = filter.intercept(List.of(matchingMutation, notMatchingMutation), mutater);

        assertThat(remainingMutations).containsExactly(notMatchingMutation);
    }

    @Test
    void shouldNotSuppressMutationsInSameNamedClassInDifferentPackage() {
        ClassTree classTree = createClassTree("com.example.TestClass");

        var annotations = createAnnotations(List.of(List.of("mutator", "Math")));
        when(classTree.annotations()).thenReturn(annotations);
        when(classTree.methods()).thenReturn(List.of());

        filter.begin(classTree);

        MutationDetails mutationMathMutator = createMutation("com.example.otherPath.TestClass", "anyMethod", MATH_MUTATOR_FQCN);

        Collection<MutationDetails> remainingMutations = filter.intercept(List.of(mutationMathMutator), mutater);

        assertThat(remainingMutations).containsExactly(mutationMathMutator);
    }

    @Test
    void shouldSuppressAllMutationsWhenClassHasAnnotationWithoutValues() {
        ClassTree classTree = createClassTree("com.example.TestClass");
        var annotations = createAnnotations(List.of());
        when(classTree.annotations()).thenReturn(annotations);
        MethodTree anyMethod = createMethodTree(classTree, "anyMethod");
        MethodTree anyOtherMethod = createMethodTree(classTree, "anyOtherMethod");
        when(classTree.methods()).thenReturn(List.of(anyMethod, anyOtherMethod));

        filter.begin(classTree);

        MutationDetails mutation = createMutation("com.example.TestClass", "anyMethod", "AnyMutator");
        MutationDetails otherMutation = createMutation("com.example.TestClass", "anyOtherMethod", "AnyOtherMutator");

        Collection<MutationDetails> remainingMutations = filter.intercept(List.of(), mutater);
        assertThat(remainingMutations).isEmpty();

        remainingMutations = filter.intercept(List.of(mutation), mutater);
        assertThat(remainingMutations).isEmpty();

        remainingMutations = filter.intercept(List.of(mutation, otherMutation), mutater);
        assertThat(remainingMutations).isEmpty();
    }

    @Test
    void shouldSuppressAllMutationsInMethodWhenMethodHasAnnotationWithoutValues() {
        ClassTree classTree = createClassTree("com.example.TestClass");
        when(classTree.annotations()).thenReturn(List.of());

        MethodTree methodTree = createMethodTree(classTree, "annotatedMethod");
        var annotations = createAnnotations(List.of());
        when(methodTree.annotations()).thenReturn(annotations);

        filter.begin(classTree);
        MutationDetails mutation = createMutation("com.example.TestClass", "annotatedMethod", MATH_MUTATOR_FQCN);
        Collection<MutationDetails> remainingMutations = filter.intercept(List.of(mutation), mutater);

        assertThat(remainingMutations).isEmpty();
    }

    @Test
    void shouldSuppressMutationInMethodWhenMethodHasAnnotationWithMatchingMutator() {
        ClassTree classTree = createClassTree("com.example.TestClass");
        when(classTree.annotations()).thenReturn(List.of());

        MethodTree methodTree = createMethodTree(classTree, "annotatedMethod");
        var annotations = createAnnotations(List.of(List.of("mutator", "Math")));
        when(methodTree.annotations()).thenReturn(annotations);

        filter.begin(classTree);
        MutationDetails matchingMutation = createMutation("com.example.TestClass", "annotatedMethod", MATH_MUTATOR_FQCN);
        MutationDetails notMatchingMutation = createMutation("com.example.TestClass", "annotatedMethod", PRIMITIVE_RETURNS_MUTATOR_FQCN);
        Collection<MutationDetails> remainingMutations = filter.intercept(List.of(matchingMutation, notMatchingMutation), mutater);

        assertThat(remainingMutations).containsExactly(notMatchingMutation);
    }

    @Test
    void shouldSuppressMutationInMethodCorrectlyForMethodWithMultipleAnnotations() {
        ClassTree classTree = createClassTree("com.example.TestClass");
        when(classTree.annotations()).thenReturn(List.of());

        MethodTree methodTree = createMethodTree(classTree, "annotatedMethod");
        var annotations = createAnnotations(List.of(List.of("mutator", "Math"),  List.of("mutator", "PrimitiveReturns")));
        when(methodTree.annotations()).thenReturn(annotations);

        filter.begin(classTree);
        MutationDetails mutationMathMutator = createMutation("com.example.TestClass", "annotatedMethod", MATH_MUTATOR_FQCN);
        MutationDetails mutationPrimitiveReturnsMutator = createMutation("com.example.TestClass", "annotatedMethod", PRIMITIVE_RETURNS_MUTATOR_FQCN);
        Collection<MutationDetails> remainingMutations = filter.intercept(List.of(mutationMathMutator, mutationPrimitiveReturnsMutator), mutater);

        assertThat(remainingMutations).isEmpty();
    }

    @Test
    void shouldNotSuppressMutationFromOtherNotAnnotatedMethodWhenMutatorIsMatching() {
        ClassTree classTree = createClassTree("com.example.TestClass");
        when(classTree.annotations()).thenReturn(List.of());

        MethodTree methodTree = createMethodTree(classTree, "annotatedMethod");
        var annotations = createAnnotations(List.of(List.of("mutator", "Math")));
        when(methodTree.annotations()).thenReturn(annotations);

        filter.begin(classTree);
        MutationDetails mutationInOtherMethod = createMutation("com.example.TestClass", "otherMethod", MATH_MUTATOR_FQCN);
        Collection<MutationDetails> remainingMutations = filter.intercept(List.of(mutationInOtherMethod), mutater);

        assertThat(remainingMutations).containsExactly(mutationInOtherMethod);
    }

    @Test
    void shouldSuppressMutationsWithClassAndMethodLevelAnnotations() {
        ClassTree classTree = createClassTree("com.example.TestClass");
        var classAnnotations = createAnnotations(List.of(List.of("mutator", "Math")));
        when(classTree.annotations()).thenReturn(classAnnotations);

        MethodTree methodTree = createMethodTree(classTree, "annotatedMethod");
        var annotations = createAnnotations(List.of(List.of("mutator", "Math"), List.of("mutator", "PrimitiveReturns"), List.of("mutator", "NegateConditionals")));
        when(methodTree.annotations()).thenReturn(annotations);

        filter.begin(classTree);
        MutationDetails mutationMathMutator = createMutation("com.example.TestClass", "annotatedMethod", MATH_MUTATOR_FQCN);
        MutationDetails mutationPrimitiveReturnsMutator = createMutation("com.example.TestClass", "annotatedMethod", PRIMITIVE_RETURNS_MUTATOR_FQCN);
        MutationDetails mutationNegateConditionalsMutator = createMutation("com.example.TestClass", "otherMethod", NEGATE_CONDITIONALS_MUTATOR_FQCN);
        MutationDetails mutationMathMutatorInOtherMethod = createMutation("com.example.TestClass", "otherMethod", MATH_MUTATOR_FQCN);
        Collection<MutationDetails> remainingMutations = filter.intercept(List.of(mutationMathMutator, mutationPrimitiveReturnsMutator, mutationNegateConditionalsMutator, mutationMathMutatorInOtherMethod), mutater);

        assertThat(remainingMutations).containsExactly(mutationNegateConditionalsMutator);
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
        MethodTree methodTree = mock(MethodTree.class);
        Location location = mock(Location.class);
        when(methodTree.asLocation()).thenReturn(location);
        when(location.getMethodName()).thenReturn(methodName);

        when(classTree.methods()).thenReturn(List.of(methodTree));

        return methodTree;
    }

    private static List<AnnotationNode> createAnnotations(final List<List<Object>> values) {
        if (values.isEmpty()) {
            return List.of(new AnnotationNode(SUPPRESS_MUTATION_DESC));
        }

        List<AnnotationNode> annotationNodes = new ArrayList<>();
        for (List<Object> value : values) {
            var annotation = new AnnotationNode(SUPPRESS_MUTATION_DESC);

            annotation.values = value;
            annotationNodes.add(annotation);
        }

        return annotationNodes;
    }

    private MutationDetails createMutation(final String className, final String methodName, final String mutatorFqcn) {
        var id = new MutationIdentifier(
                Location.location(ClassName.fromString(className), methodName, "desc"), 0, mutatorFqcn
        );
        return new MutationDetails(id, "File.java", "desc", 1, 0);
    }
}
