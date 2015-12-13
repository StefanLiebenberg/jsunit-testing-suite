package org.slieb.jsunit.internal;


import org.slieb.closure.dependencies.GoogDependencyCalculator;
import org.slieb.closure.dependencies.GoogDependencyNode;
import org.slieb.jsunit.api.TestConfigurator;
import slieb.kute.api.Resource;
import slieb.kute.api.ResourceProvider;

import java.util.Collection;

public class CachedTestConfigurator implements TestConfigurator {

    private final ResourceProvider<Resource.Readable> sourceProvider, testProvider;

    private final GoogDependencyCalculator calculator;

    private final Integer timeout;

    public CachedTestConfigurator(TestConfigurator testConfigurator) {
        this.sourceProvider = testConfigurator.sources();
        this.testProvider = testConfigurator.tests();
        this.calculator = new CachedCalculator(this.sourceProvider);
        this.timeout = testConfigurator.getTimeout();
    }

    public GoogDependencyCalculator calculator() {
        return calculator;
    }

    @Override
    public ResourceProvider<Resource.Readable> tests() {
        return testProvider;
    }

    @Override
    public ResourceProvider<Resource.Readable> sources() {
        return sourceProvider;
    }

    @Override
    public Integer getTimeout() {
        return timeout;
    }
}


class CachedCalculator extends GoogDependencyCalculator {

    private Collection<GoogDependencyNode> cachedNodes;

    public CachedCalculator(Iterable<Resource.Readable> resources) {
        super(resources);
    }

    @Override
    public Collection<GoogDependencyNode> getDependencyNodes() {
        if (cachedNodes == null) {
            cachedNodes = super.getDependencyNodes();
        }
        return cachedNodes;
    }
}
