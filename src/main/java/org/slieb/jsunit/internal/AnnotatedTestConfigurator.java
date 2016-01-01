package org.slieb.jsunit.internal;

import org.codehaus.plexus.util.MatchPatterns;
import org.slieb.closure.dependencies.GoogDependencyCalculator;
import org.slieb.closure.dependencies.GoogResources;
import org.slieb.jsunit.api.JsUnitConfig;
import org.slieb.jsunit.api.TestConfigurator;
import slieb.kute.KuteLambdas;
import slieb.kute.api.Resource;
import slieb.kute.api.ResourcePredicate;

import static org.slieb.jsunit.internal.DefaultTestConfigurator.*;
import static slieb.kute.Kute.filterResources;


public class AnnotatedTestConfigurator implements TestConfigurator {

    private final Resource.Provider defaultProvider, defaultTestProvider;

    private final GoogDependencyCalculator calc;

    private final Integer timeout;

    public AnnotatedTestConfigurator(JsUnitConfig config,
                                     Resource.Provider provider) {
        this.defaultProvider = filterResources(provider, KuteLambdas.all(
                JAVASCRIPT_FILTER,
                DEFAULT_EXCLUDES, getPredicate(config.includes(), config.excludes())));
        this.defaultTestProvider = filterResources(this.defaultProvider,
                KuteLambdas.all(TESTS_FILTER, getPredicate(config.testIncludes(), config.testExcludes())));
        this.calc = GoogResources.getCalculator(this.defaultProvider);
        this.timeout = config.timeout();
    }

    @Override
    public Resource.Provider sources() {
        return defaultProvider;
    }

    @Override
    public Resource.Provider tests() {
        return defaultTestProvider;
    }

    @Override
    public Integer getTimeout() {
        return timeout;
    }

    @Override
    public GoogDependencyCalculator calculator() {
        return calc;
    }

    private static ResourcePredicate<Resource> getPredicate(String[] includes,
                                                            String[] excludes) {

        ResourcePredicate<Resource> predicate = (r) -> true;

        boolean shouldUseInclude = includes.length > 0, shouldUseExclude = excludes.length > 0;
        if (!shouldUseInclude && !shouldUseExclude) {
            return predicate;
        }

        if (shouldUseInclude) {
            MatchPatterns includePatterns = MatchPatterns.from(includes);
            predicate = resource -> includePatterns.matches(resource.getPath(), true);
        }

        if (shouldUseExclude) {
            MatchPatterns excludePatterns = MatchPatterns.from(excludes);
            predicate = KuteLambdas.all(predicate, resource -> !excludePatterns.matches(resource.getPath(), true));
        }

        return predicate;

    }

}

