package org.slieb.jsunit.internal;

import org.codehaus.plexus.util.MatchPatterns;
import org.slieb.closure.dependencies.GoogDependencyCalculator;
import org.slieb.closure.dependencies.GoogResources;
import org.slieb.jsunit.api.JsUnitConfig;
import org.slieb.jsunit.api.TestConfigurator;
import slieb.kute.api.Resource;
import slieb.kute.api.ResourceProvider;

import java.util.function.Predicate;

import static org.slieb.jsunit.internal.DefaultTestConfigurator.*;
import static slieb.kute.resources.Resources.filterResources;


public class AnnotatedTestConfigurator implements TestConfigurator {

    private final ResourceProvider<? extends Resource.Readable> defaultProvider, defaultTestProvider;

    private final GoogDependencyCalculator calc;

    private final Integer timeout;

    public AnnotatedTestConfigurator(JsUnitConfig config, ResourceProvider<? extends Resource.Readable> provider) {
        this.defaultProvider = filterResources(provider, JAVASCRIPT_FILTER
                .and(DEFAULT_EXCLUDES)
                .and(getPredicate(config.includes(), config.excludes())));
        this.defaultTestProvider = filterResources(this.defaultProvider, TESTS_FILTER.and(getPredicate(config.testIncludes(), config.testExcludes())));
        this.calc = GoogResources.getCalculatorCast(this.defaultProvider);
        this.timeout = config.timeout();
    }

    @Override
    public ResourceProvider<? extends Resource.Readable> sources() {
        return defaultProvider;
    }

    @Override
    public ResourceProvider<? extends Resource.Readable> tests() {
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

    private static Predicate<Resource> getPredicate(String[] includes, String[] excludes) {

        Predicate<Resource> predicate = (r) -> true;

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
            predicate = predicate.and(resource -> !excludePatterns.matches(resource.getPath(), true));
        }

        return predicate;

    }

}

