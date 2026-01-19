package edu.hm.hafner.pitmute;

import org.objectweb.asm.tree.AnnotationNode;
import org.pitest.bytecode.analysis.ClassTree;
import org.pitest.bytecode.analysis.MethodTree;
import org.pitest.mutationtest.build.InterceptorType;
import org.pitest.mutationtest.build.MutationInterceptor;
import org.pitest.mutationtest.engine.Mutater;
import org.pitest.mutationtest.engine.MutationDetails;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Filters generated PIT mutations by inspecting bytecode for {@link SuppressMutation} annotations on classes and methods.
 *
 * <p>
 * Mutations are excluded based on the presence of an annotation and its optional parameter.
 * For more information, please see the README.
 * </p>
 */
public class AnnotationExclusionFilter implements MutationInterceptor {
    private final Map<String, List<SuppressionRule>> suppressionByClass = new HashMap<>();
    private static final String SUPPRESS_MUTATION_DESC = "Ledu/hm/hafner/pitmute/SuppressMutation;";
    private static final String SUPPRESS_MUTATIONS_DESC = "Ledu/hm/hafner/pitmute/SuppressMutations;";

    @Override
    public InterceptorType type() {
        return InterceptorType.FILTER;
    }

    @Override
    public void begin(final ClassTree classTree) {
        String className = classTree.name().asJavaName();
        List<SuppressionRule> suppressionRules = suppressionByClass.computeIfAbsent(className, k -> new ArrayList<>());

        extractSuppressionRules(classTree.annotations(), suppressionRules, className, Optional.empty());

        for (MethodTree method : classTree.methods()) {
            String methodName = method.asLocation().getMethodName();
            extractSuppressionRules(method.annotations(), suppressionRules, className, Optional.of(methodName));
        }
    }

    private void extractSuppressionRules(final List<AnnotationNode> annotations, final List<SuppressionRule> suppressionRules, final String className, final Optional<String> methodName) {
        if (annotations == null || annotations.isEmpty()) {
            return;
        }

        for (AnnotationNode annotation : annotations) {
            if (SUPPRESS_MUTATION_DESC.equals(annotation.desc)) {
                addRules(suppressionRules, className, methodName, annotation);
            }
            else if (SUPPRESS_MUTATIONS_DESC.equals(annotation.desc)) {
                List<AnnotationNode> repeatedAnnotations = getRepeatedAnnotations(annotation);
                for (AnnotationNode singleAnnotation : repeatedAnnotations) {
                    addRules(suppressionRules, className, methodName, singleAnnotation);
                }
            }
        }
    }

    private List<AnnotationNode> getRepeatedAnnotations(final AnnotationNode containerAnnotation) {
        List<AnnotationNode> annotations = new ArrayList<>();

        for (int i = 0; i < containerAnnotation.values.size() - 1; i += 2) {
            String annotationName = containerAnnotation.values.get(i).toString();
            Object value = containerAnnotation.values.get(i + 1);

            if ("value".equals(annotationName) && value instanceof List) {
                return (List<AnnotationNode>) value;
            }
        }
        return annotations;
    }

    private static void addRules(final List<SuppressionRule> suppressionRules, final String className, final Optional<String> methodName, final AnnotationNode annotation) {
        List<Object> values = annotation.values;
        if (values == null || values.isEmpty()) {
            suppressionRules.add(new SuppressionRule(className, methodName, Optional.empty()));
            return;
        }

        String elementNameMutator = "mutator";
        for (int i = 0; i < values.size(); i += 2) {
            String annotationElement = values.get(i).toString();
            Object value = values.get(i + 1);
            if (annotationElement.equals(elementNameMutator)) {
                suppressionRules.add(new SuppressionRule(className, methodName, Optional.of(value.toString())));
            }
        }
    }

    @Override
    public Collection<MutationDetails> intercept(final Collection<MutationDetails> mutations, Mutater mutater) {
        return mutations.stream().filter(mutation -> !shouldSuppress(mutation)).collect(Collectors.toList());
    }

    private boolean shouldSuppress(final MutationDetails mutation) {
        String className = mutation.getClassName().asJavaName();
        List<SuppressionRule> rulesDefinedInClass = suppressionByClass.getOrDefault(className, List.of());

        for (SuppressionRule rule : rulesDefinedInClass) {
            if ((rule.methodName().isEmpty() || rule.methodName().get().equals(mutation.getMethod()))
                    && (rule.mutatorName().isEmpty() || mutatorMatches(mutation.getMutator(), rule.mutatorName().get()))) {
                return true;
            }
        }
        return false;
    }

    private static boolean mutatorMatches(final String fqcn, final String mutatorNameEntry) {
        if (mutatorNameEntry == null || mutatorNameEntry.isBlank()) {
            return true;
        }

        String mutatorName = fqcn.substring(fqcn.lastIndexOf('.') + 1);
        String shortMutatorName = mutatorName.endsWith("Mutator") ? mutatorName.substring(0, mutatorName.length() - 7) : mutatorName;
        return fqcn.equals(mutatorNameEntry) || mutatorName.equals(mutatorNameEntry) || shortMutatorName.equals(mutatorNameEntry);
    }

    @Override
    public void end() {
        // nothing to do
    }
}
