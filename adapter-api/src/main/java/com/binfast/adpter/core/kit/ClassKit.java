package com.binfast.adpter.core.kit;

import org.springframework.context.ApplicationContext;
import org.springframework.util.ClassUtils;

import java.lang.reflect.Proxy;

/**
 * @author εζ
 * @date 2022/11/28 11:28 δΈε
 */
public class ClassKit {

    public static Class<?> getCurrentByType(ApplicationContext applicationContext, String name) {
        Class<?> type = applicationContext.getType(name);
        if (type == null) {
            return null;
        }
        boolean cglibProxy = Proxy.isProxyClass(type) || type.getName().contains("$$");
        if (cglibProxy) {
            type = ClassUtils.getUserClass(type);
        }
        return type;
    }
}
