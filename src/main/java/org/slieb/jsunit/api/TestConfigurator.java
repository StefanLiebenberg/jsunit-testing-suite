package org.slieb.jsunit.api;


import org.slieb.closure.dependencies.GoogDependencyCalculator;
import slieb.kute.api.Resource;
import slieb.kute.api.ResourceProvider;

public interface TestConfigurator {


    /**
     * @return A Resource provider that contains all relevant sources.
     */
    ResourceProvider<Resource.Readable> sources();


    /**
     * @return A provider that gives you the tests.
     */
    ResourceProvider<Resource.Readable> tests();


    /**
     * @return A calculator instance.
     */
    GoogDependencyCalculator calculator();

    Integer getTimeout();

}
