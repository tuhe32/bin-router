package com.binfast.adpter.core.annotations;

import org.springframework.core.annotation.AliasFor;
import org.springframework.web.bind.annotation.RequestMethod;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author εζ
 * @date 2022/11/5 6:56 δΈε
 */
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@ApiMapping(
        method = RequestMethod.PUT
)
public @interface PutApiMapping {

    @AliasFor(
            annotation = ApiMapping.class
    )
    String value();

    @AliasFor(
            annotation = ApiMapping.class
    )
    String notes() default "";
}
