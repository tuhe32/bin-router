package com.binfast.adpter.core.annotations;

import org.springframework.web.bind.annotation.RequestMethod;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author 刘斌
 * @date 2022/11/5 6:15 下午
 */
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface ApiMapping {

    String value() default "";

    RequestMethod[] method() default {};

    String notes() default "";
}
