package org.slieb.jsunit;

import org.junit.runner.Description;
import org.junit.runner.Runner;
import org.junit.runner.notification.Failure;
import org.junit.runner.notification.RunNotifier;
import org.slieb.closure.dependencies.GoogDependencyCalculator;
import org.slieb.closure.dependencies.GoogResources;
import org.slieb.jsunit.internal.TestExecutor;
import slieb.kute.Kute;
import slieb.kute.api.Resource;
import slieb.kute.api.ResourceProvider;

import java.util.function.Predicate;

import static slieb.kute.resources.ResourcePredicates.*;
import static slieb.kute.resources.Resources.filterResources;


public class JSUnitSingleTestRunner extends Runner {

    private Predicate<Resource> filter = all(
            extensionFilter(".js"),
            any(
                    extensionFilter("env.rhino.js"),
                    r -> r.getPath().startsWith("jdk/nashorn"),
                    r -> r.getPath().startsWith("com/google/javascript/jscomp")
            ).negate());


    public final Resource.Readable testResource;

    public final GoogDependencyCalculator calculator;

    public final Integer timeoutSeconds;

    public JSUnitSingleTestRunner(Class<?> testClass) {
        throw new IllegalStateException("not supported yet");
    }

    public JSUnitSingleTestRunner(GoogDependencyCalculator calculator, Resource.Readable testResource, Integer timeoutSeconds) {
        this.calculator = calculator;
        this.testResource = testResource;
        this.timeoutSeconds = timeoutSeconds;

    }

    public JSUnitSingleTestRunner(ResourceProvider<? extends Resource.Readable> resourceProvider, Resource.Readable testResource, Integer timeoutSeconds) {
        this.testResource = testResource;
        this.timeoutSeconds = timeoutSeconds;
        this.calculator = GoogResources.getCalculatorCast(resourceProvider);
    }

    public JSUnitSingleTestRunner(String testPath, Integer timeout) {
        ResourceProvider<? extends Resource.Readable> provider = filterResources(Kute.getDefaultProvider(), filter);
        this.calculator = GoogResources.getCalculatorCast(provider);
        this.testResource = provider.getResourceByName(testPath);
        this.timeoutSeconds = timeout;
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
            TestExecutor executor = new TestExecutor(calculator, testResource, timeoutSeconds);
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
