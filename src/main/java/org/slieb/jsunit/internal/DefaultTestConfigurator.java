package org.slieb.jsunit.internal;

import org.slieb.closure.dependencies.GoogDependencyCalculator;
import org.slieb.closure.dependencies.GoogResources;
import org.slieb.jsunit.api.TestConfigurator;
import slieb.kute.KuteLambdas;
import slieb.kute.api.Resource;
import slieb.kute.api.ResourcePredicate;

import static slieb.kute.Kute.filterResources;
import static slieb.kute.KuteLambdas.extensionFilter;


public class DefaultTestConfigurator implements TestConfigurator {

    public static final ResourcePredicate<Resource> JAVASCRIPT_FILTER = extensionFilter(".js");

    public static final ResourcePredicate<Resource> DEFAULT_EXCLUDES = KuteLambdas.any(
            extensionFilter("env.rhino.js"),
            r -> r.getPath().startsWith("jdk/nashorn"),
            r -> r.getPath().endsWith("load.rhino.js"),
            r -> r.getPath().startsWith("com/google/javascript/jscomp"),
            r -> r.getPath().startsWith("com/google/javascript/refactoring"),
            r -> r.getPath().startsWith("/closure-library") && r.getPath().endsWith("_test.js")
    ).negate()::test;

    public static final ResourcePredicate<Resource> TESTS_FILTER = extensionFilter("_test.js");

    private final Resource.Provider filteredProvider;
    private final Resource.Provider testProvider;

    public DefaultTestConfigurator(Resource.Provider provider) {
        this.filteredProvider = filterResources(provider, KuteLambdas.all(JAVASCRIPT_FILTER, DEFAULT_EXCLUDES));
        this.testProvider = filterResources(this.filteredProvider, TESTS_FILTER);
    }

    @Override
    public Resource.Provider sources() {
        return this.filteredProvider;
    }

    @Override
    public Resource.Provider tests() {
        return this.testProvider;
    }

    @Override
    public GoogDependencyCalculator calculator() {
        return GoogResources.getCalculator(filteredProvider);
    }

    @Override
    public Integer getTimeout() {
        return 30;
    }
}
