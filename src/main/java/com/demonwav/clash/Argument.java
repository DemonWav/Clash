package com.demonwav.clash;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Documented
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Argument {

    String shortName();
    String configName() default "";
    String[] longNames() default {};
    String description() default "";
    boolean required() default true;
    String defaultValue() default "";
    Class<? extends Initializer> initializer() default Initializer.class;
    Class<? extends Creator> defaultCreator() default Creator.class;
}
