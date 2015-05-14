package org.slieb.jsunit.api;

import com.google.inject.Module;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface JsUnitConfig {

    Class<? extends Module>[] guiceModules() default {};

    String[] excludes() default {};

    String[] includes() default {};

    int timeout() default 30;

}
