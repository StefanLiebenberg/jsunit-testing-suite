package org.slieb.jsunit.internal;

import org.slieb.closure.dependencies.GoogDependencyCalculator;
import org.slieb.closure.dependencies.GoogResources;
import org.slieb.jsunit.api.TestConfigurator;
import slieb.kute.api.Resource;
import slieb.kute.api.ResourceProvider;
import slieb.kute.resources.ResourcePredicates;
import slieb.kute.resources.Resources;

import java.util.function.Predicate;

import static slieb.kute.resources.ResourcePredicates.extensionFilter;


public class DefaultTestConfigurator implements TestConfigurator {

    public static final Predicate<Resource> JAVASCRIPT_FILTER = extensionFilter(".js");

    public static final Predicate<Resource> DEFAULT_EXCLUDES = ResourcePredicates.any(
            extensionFilter("env.rhino.js"),
            r -> r.getPath().startsWith("jdk/nashorn"),
            r -> r.getPath().endsWith("load.rhino.js"),
            r -> r.getPath().startsWith("com/google/javascript/jscomp"),
            r -> r.getPath().startsWith("com/google/javascript/refactoring"),
            r -> r.getPath().startsWith("/closure-library") && r.getPath().endsWith("_test.js")
    ).negate();

    public static final Predicate<Resource> TESTS_FILTER = extensionFilter("_test.js");

    private final ResourceProvider<? extends Resource.Readable> filteredProvider;
    private final ResourceProvider<? extends Resource.Readable> testProvider;

    public DefaultTestConfigurator(ResourceProvider<? extends Resource.Readable> provider) {
        this.filteredProvider = Resources.filterResources(provider, JAVASCRIPT_FILTER.and(DEFAULT_EXCLUDES));
        this.testProvider = Resources.filterResources(this.filteredProvider, TESTS_FILTER);
    }

    @Override
    public ResourceProvider<? extends Resource.Readable> sources() {
        return this.filteredProvider;
    }

    @Override
    public ResourceProvider<? extends Resource.Readable> tests() {
        return this.testProvider;
    }

    @Override
    public GoogDependencyCalculator calculator() {
        return GoogResources.getCalculatorCast(filteredProvider);
    }

    @Override
    public Integer getTimeout() {
        return 30;
    }
}
