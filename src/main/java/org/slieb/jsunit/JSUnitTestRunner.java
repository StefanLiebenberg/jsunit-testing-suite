package org.slieb.jsunit;

import com.google.common.collect.ImmutableList;
import org.junit.runner.Description;
import org.junit.runner.notification.RunNotifier;
import org.junit.runners.ParentRunner;
import org.junit.runners.model.InitializationError;
import slieb.kute.Kute;
import slieb.kute.api.Resource;
import slieb.kute.api.ResourceProvider;
import slieb.kute.resources.ResourcePredicates;

import java.lang.annotation.*;
import java.util.List;
import java.util.function.Predicate;

import static slieb.kute.resources.ResourcePredicates.extensionFilter;
import static slieb.kute.resources.Resources.filterResources;

/**
 * Runs a jsunit javascript file.
 */
public class JSUnitTestRunner extends ParentRunner<JSUnitSingleTestRunner> {

    private static final Predicate<Resource> JAVASCRIPT_FILTER = extensionFilter(".js");

    private static final Predicate<Resource> DEFAULT_EXCLUDES = ResourcePredicates.any(
            extensionFilter("env.rhino.js"),
            r -> r.getPath().startsWith("jdk/nashorn"),
            r -> r.getPath().endsWith("load.rhino.js"),
            r -> r.getPath().startsWith("com/google/javascript/jscomp"),
            r -> r.getPath().startsWith("com/google/javascript/refactoring"),
            r -> r.getPath().startsWith("/closure-library") && r.getPath().endsWith("_test.js")
    ).negate();

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.TYPE)
    public @interface Configuration {

        String[] includes() default {};

        String[] excludes() default {};

        int timeoutSeconds() default 30;
    }

    public JSUnitTestRunner(Class<?> testClass) throws InitializationError {
        super(testClass);
    }

    @Override
    protected List<JSUnitSingleTestRunner> getChildren() {
        ResourceProvider<? extends Resource.Readable> resources =
                filterResources(Kute.getDefaultProvider(), ResourcePredicates.all(JAVASCRIPT_FILTER, DEFAULT_EXCLUDES));


        ImmutableList.Builder<JSUnitSingleTestRunner> builder = new ImmutableList.Builder<>();
        for (Annotation annotation : getRunnerAnnotations()) {
            if (Configuration.class.isAssignableFrom(annotation.getClass())) {
                Configuration pathsAnnotation = (Configuration) annotation;

                getTestProvider(resources).stream().forEach(testResource -> {
                    System.out.println(testResource);
                    builder.add(new JSUnitSingleTestRunner(resources, testResource, pathsAnnotation.timeoutSeconds()));
                });
            }
        }
        return builder.build();
    }

    private ResourceProvider<? extends Resource.Readable> getTestProvider(ResourceProvider<? extends Resource.Readable> provider) {
        return filterResources(provider, extensionFilter("_test.js"));
    }

    @Override
    protected Description describeChild(JSUnitSingleTestRunner child) {
        return child.getDescription();
    }

    @Override
    protected void runChild(JSUnitSingleTestRunner child, RunNotifier notifier) {
        child.run(notifier);
    }
}
