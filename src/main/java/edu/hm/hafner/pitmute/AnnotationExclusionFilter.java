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
            String methodNameWithDesc = method.asLocation().getMethodName() + method.asLocation().getMethodDesc();
            extractSuppressionRules(method.annotations(), suppressionRules, className, Optional.of(methodNameWithDesc));
        }
    }

    private void extractSuppressionRules(final List<AnnotationNode> annotations, final List<SuppressionRule> suppressionRules, final String className, final Optional<String> methodName) {
        if (annotations == null || annotations.isEmpty()) {
            return;
        }

        for (AnnotationNode annotation : annotations) {
            if (SUPPRESS_MUTATION_DESC.equals(annotation.desc)) {
                addSuppressionRuleForAnnotation(annotation, suppressionRules, className, methodName);
            }
            else if (SUPPRESS_MUTATIONS_DESC.equals(annotation.desc)) {
                List<AnnotationNode> repeatedAnnotations = getAnnotationsFromContainer(annotation);
                for (AnnotationNode singleAnnotation : repeatedAnnotations) {
                    addSuppressionRuleForAnnotation(singleAnnotation, suppressionRules, className, methodName);
                }
            }
        }
    }

    private List<AnnotationNode> getAnnotationsFromContainer(final AnnotationNode containerAnnotation) {
        for (int i = 0; i < containerAnnotation.values.size() - 1; i += 2) {
            String annotationName = containerAnnotation.values.get(i).toString();
            Object value = containerAnnotation.values.get(i + 1);

            if ("value".equals(annotationName) && value instanceof List<?> list) {
                return list.stream()
                        .filter(AnnotationNode.class::isInstance)
                        .map(AnnotationNode.class::cast)
                        .collect(Collectors.toList());
            }
        }
        return List.of();
    }

    private static void addSuppressionRuleForAnnotation(final AnnotationNode annotation, final List<SuppressionRule> suppressionRules, final String className, final Optional<String> methodName) {
        List<Object> values = annotation.values;
        if (values == null || values.isEmpty()) {
            suppressionRules.add(new SuppressionRule(className, methodName, Optional.empty(), Optional.empty()));
            return;
        }
        else if (values.size() % 2 == 1) {
            throw new IllegalStateException("Invalid annotation parameters: " + values);
        }

        String elementNameMutator = "mutator";
        String elementNameLine = "line";
        Optional<String> mutatorName = Optional.empty();
        Optional<Integer> line = Optional.empty();
        for (int i = 0; i < values.size() - 1; i += 2) {
            String annotationElement = values.get(i).toString();
            Object value = values.get(i + 1);
            if (annotationElement.equals(elementNameMutator)) {
                mutatorName = Optional.of(value.toString());
            }
            else if (annotationElement.equals(elementNameLine)) {
                line = Optional.of(Integer.parseInt(value.toString()));
            }
        }
        suppressionRules.add(new SuppressionRule(className, methodName, mutatorName, line));
    }

    @Override
    public Collection<MutationDetails> intercept(final Collection<MutationDetails> mutations, Mutater mutater) {
        return mutations.stream().filter(mutation -> !shouldSuppress(mutation)).collect(Collectors.toList());
    }

    private boolean shouldSuppress(final MutationDetails mutation) {
        String className = mutation.getClassName().asJavaName();
        List<SuppressionRule> rulesDefinedInClass = suppressionByClass.getOrDefault(className, List.of());

        for (SuppressionRule rule : rulesDefinedInClass) {
            String methodNameWithDesc = mutation.getMethod() + mutation.getId().getLocation().getMethodDesc();
            boolean methodNameMatches = rule.methodName().isEmpty() || rule.methodName().get().equals(methodNameWithDesc);
            boolean mutatorNameMatches = rule.mutatorName().isEmpty() || mutatorMatches(mutation.getMutator(), rule.mutatorName().get());
            boolean lineMatches = rule.line().isEmpty() || rule.line().get() == mutation.getLineNumber();
            if (methodNameMatches && mutatorNameMatches && lineMatches) {
                return true;
            }
        }
        return false;
    }

    private static boolean mutatorMatches(final String fqcn, final String mutatorNameEntry) {
        String mutatorName = fqcn.substring(fqcn.lastIndexOf('.') + 1);
        String shortMutatorName = mutatorName.endsWith("Mutator") ? mutatorName.substring(0, mutatorName.length() - 7) : mutatorName;
        return fqcn.equals(mutatorNameEntry) || mutatorName.equals(mutatorNameEntry) || shortMutatorName.equals(mutatorNameEntry);
    }

    @Override
    public void end() {
        // nothing to do
    }
}
