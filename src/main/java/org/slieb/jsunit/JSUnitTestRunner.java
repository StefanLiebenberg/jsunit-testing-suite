package org.slieb.jsunit;

import org.junit.runner.Description;
import org.junit.runner.notification.RunNotifier;
import org.junit.runners.ParentRunner;
import org.junit.runners.model.InitializationError;
import org.slieb.jsunit.api.JsUnitConfig;
import org.slieb.jsunit.internal.AnnotatedTestConfigurator;
import org.slieb.jsunit.internal.CachedTestConfigurator;
import org.slieb.jsunit.internal.DefaultTestConfigurator;
import slieb.kute.Kute;
import slieb.kute.api.Resource;

import java.util.List;
import java.util.function.Function;

import static java.util.stream.Collectors.toList;

/**
 * Runs a jsunit javascript file.
 */
public class JSUnitTestRunner extends ParentRunner<JSUnitSingleTestRunner> {

    private final CachedTestConfigurator testConfigurator;

    public JSUnitTestRunner(Class<?> testClass) throws InitializationError {
        super(testClass);
        this.testConfigurator =
                new CachedTestConfigurator(testClass.isAnnotationPresent(JsUnitConfig.class) ?
                                                   new AnnotatedTestConfigurator(
                                                           testClass.getAnnotation(JsUnitConfig.class),
                                                           Kute.asReadableProvider(Kute.getDefaultProvider())) :
                                                   new DefaultTestConfigurator(
                                                           Kute.asReadableProvider(Kute.getDefaultProvider())));
    }

    protected List<JSUnitSingleTestRunner> getChildren() {
        return testConfigurator.tests().stream().map(testRunnerFactory()).collect(toList());
    }

    public Function<Resource.Readable, JSUnitSingleTestRunner> testRunnerFactory() {
        return (testResource) -> new JSUnitSingleTestRunner(testConfigurator, testResource);
    }

    @Override
    protected Description describeChild(JSUnitSingleTestRunner child) {
        return child.getDescription();
    }

    @Override
    protected void runChild(JSUnitSingleTestRunner child,
                            RunNotifier notifier) {
        child.run(notifier);
    }
}
