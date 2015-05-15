package org.slieb.jsunit;

import org.junit.runner.Description;
import org.junit.runner.Runner;
import org.junit.runner.notification.Failure;
import org.junit.runner.notification.RunNotifier;
import org.slieb.closure.dependencies.GoogDependencyCalculator;
import org.slieb.closure.dependencies.GoogResources;
import org.slieb.jsunit.internal.CachedTestConfigurator;
import slieb.kute.api.Resource;
import slieb.kute.api.ResourceProvider;

import java.util.function.Predicate;

import static slieb.kute.resources.ResourcePredicates.*;


public class JSUnitSingleTestRunner extends Runner {

    private final CachedTestConfigurator testConfigurator;

    private Predicate<Resource> filter = all(
            extensionFilter(".js"),
            any(
                    extensionFilter("env.rhino.js"),
                    r -> r.getPath().startsWith("jdk/nashorn"),
                    r -> r.getPath().startsWith("com/google/javascript/jscomp")
            ).negate());


    public final Resource.Readable testResource;

    public final GoogDependencyCalculator calculator;


    public JSUnitSingleTestRunner(Class<?> testClass) {
        throw new IllegalStateException("not supported yet");
    }

    public JSUnitSingleTestRunner(CachedTestConfigurator configurator, Resource.Readable testResource) {
        this.testConfigurator = configurator;
        this.testResource = testResource;
        this.calculator = configurator.calculator();

    }

    public JSUnitSingleTestRunner(ResourceProvider<? extends Resource.Readable> resourceProvider, Resource.Readable testResource) {
        this.testResource = testResource;
        this.calculator = GoogResources.getCalculatorCast(resourceProvider);
        this.testConfigurator = new CachedTestConfigurator(null);
    }

    public Description getDescription() {
        String path = testResource.getPath();
        String name = path.replace("_test.js", "").replaceAll("\\.", "_").replaceAll("/", ".");
        return Description.createSuiteDescription(name, path);
    }

    @Override
    public void run(RunNotifier notifier) {
        Description description = getDescription();
        try {
            notifier.fireTestStarted(description);
            TestExecutor executor = new TestExecutor(testConfigurator.calculator(), testResource, testConfigurator.getTimeout());
            executor.execute();
            if (!executor.isSuccess()) {
                notifier.fireTestFailure(new Failure(description, new RuntimeException()));
            }
        } catch (Exception e) {
            notifier.fireTestFailure(new Failure(description, e));
        } finally {
            notifier.fireTestFinished(description);
        }
    }
}
