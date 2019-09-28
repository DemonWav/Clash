package com.demonwav.clash;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import static com.demonwav.clash.Clash.ARGUMENT_DEFAULT_VALUE;

@Documented
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Argument {

    String[] shortNames();
    String configName() default ARGUMENT_DEFAULT_VALUE;
    String[] longNames() default {};
    String description() default ARGUMENT_DEFAULT_VALUE;
    boolean required() default true;
    String defaultValue() default ARGUMENT_DEFAULT_VALUE;
    Class<? extends Initializer> initializer() default Initializer.class;
    Class<? extends Creator> defaultCreator() default Creator.class;
}
