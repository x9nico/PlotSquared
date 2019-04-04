package com.github.intellectualsites.plotsquared.plot.command_test.binding;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME) @Target(ElementType.PARAMETER)
@SuppressWarnings("WeakerAccess") public @interface Added {}