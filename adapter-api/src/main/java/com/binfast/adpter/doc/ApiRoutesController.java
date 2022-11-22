package com.binfast.adpter.doc;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.core.LocalVariableTableParameterNameDiscoverer;
import org.springframework.core.ParameterNameDiscoverer;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.util.ClassUtils;
import org.springframework.util.ResourceUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.*;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.StringJoiner;
import java.util.stream.Collectors;

/**
 * @author 刘斌
 * @date 2022/11/14 4:50 下午
 */
@RestController
public class ApiRoutesController {
    private static final Logger logger = LoggerFactory.getLogger(ApiRoutesController.class);

    private final ApplicationContext applicationContext;

    private final ParameterNameDiscoverer nameDiscoverer;

    private String pathPrefix = "/api";

    public ApiRoutesController(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
        this.nameDiscoverer = new LocalVariableTableParameterNameDiscoverer();
    }

    @GetMapping("/router")
    public String router() {
        // 获取RequestMapping路由信息
        List<ApiRoutes> ApiRoutes = scanRequestRoutes();
        // 构造文件数据
        String content = buildRoutes(ApiRoutes);
        // 创建文件
        createFile(content);

        return "...wait a minute and check your resources directory";
    }

    private List<ApiRoutes> scanRequestRoutes() {
        List<ApiRoutes> requestRouteList = new ArrayList<>();
        String[] names = applicationContext.getBeanDefinitionNames();
        for (String name : names) {
            if (name.contains("basicErrorController")) {
                continue;
            }
            Class<?> type = applicationContext.getType(name);
            if (type == null) {
                continue;
            }
            boolean cglibProxy = Proxy.isProxyClass(type) || type.getName().contains("$$");
            if (cglibProxy) {
                type = ClassUtils.getUserClass(type);
            }
            for (Method method : type.getDeclaredMethods()) {
                RequestMapping mapping = AnnotatedElementUtils.findMergedAnnotation(method, RequestMapping.class);
//                // 包装一个API
                if (mapping != null) {
                    RequestMapping beanMapping = type.getAnnotation(RequestMapping.class);
                    ApiRoutes apiRoutes;
                    if (beanMapping != null) {
                        apiRoutes = ApiRoutes.valueOf(name, mapping.method().length == 0 ? null :mapping.method()[0].name(),
                                pathPrefix + (beanMapping.value().length == 0 ? "" : beanMapping.value()[0]) + (mapping.value().length == 0 ? "": mapping.value()[0]),
                                buildMethodFullName(method));
                    } else {
                        apiRoutes = ApiRoutes.valueOf(name, mapping.method().length == 0 ? null :mapping.method()[0].name(),
                                pathPrefix + "" + (mapping.value().length == 0 ? "": mapping.value()[0]),
                                buildMethodFullName(method));
                    }
                    requestRouteList.add(apiRoutes);
                }
            }
        }
        return requestRouteList;
    }

    public String buildMethodFullName(Method method) {
        StringBuilder builder = new StringBuilder();
        builder.append(method.getDeclaringClass().getName());
        builder.append(".");
        builder.append(method.getName());
        builder.append("(");
        int paramsCount = method.getParameterCount();
        if (paramsCount > 0) {
            StringJoiner sj = new StringJoiner(", ");
            Class<?>[] parameterTypes = method.getParameterTypes();
            String[] parameterNames = nameDiscoverer.getParameterNames(method);
            if (parameterNames != null) {
                for (int i = 0 ; i< method.getParameterCount(); i++) {
                    String typeName = parameterTypes[i].getName();
                    int splitIndex = typeName.contains("$") ? typeName.lastIndexOf("$")+1 : typeName.lastIndexOf(".")+1;
                    sj.add(typeName.substring(splitIndex) + " " + parameterNames[i]);
                }
                builder.append(sj);
            }
        }
        builder.append(")");
        return builder.toString();
    }

    private String buildRoutes(List<ApiRoutes> routeList) {
        StringBuilder builder = new StringBuilder();
        routeList = routeList.stream().sorted(Comparator.comparing(ApiRoutes::getBeanName)).collect(Collectors.toList());
        String lastBeanName = "";
        String newLine = "\r\n";
        for (ApiRoutes route : routeList) {
            if (route.getMethod().contains("swagger")) {
                continue;
            }
            if (!lastBeanName.equals(route.getBeanName())) {
                builder.append(newLine)
                        .append("# ")
                        .append(route.getBeanName())
                        .append(newLine);
                lastBeanName = route.getBeanName();
            }
            builder.append("*".equals(route.getHttpMethod()) ? "POST" : route.getHttpMethod()).append("    ")
                    .append(route.getMethod()).append("    ")
                    .append(route.getFullName())
                    .append(newLine);
        }
        return builder.toString();
    }

    private void createFile(String fileContent) {
        try {
            String fileName = "routes";
            String path = ResourceUtils.getURL("classpath:").getPath();
            path = path.substring(0, path.indexOf("target"));
            path = path + "src/main/resources";
            logger.info("host:" + path);

            File file = new File(path, fileName);
            // 判断文件是否存在
            if (!file.exists()) {
                try {
                    // 如果文件不存在创建文件
                    boolean newFile = file.createNewFile();
                    logger.info(newFile ? "创建文件" : "修改文件");
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            FileOutputStream fos ;
            BufferedWriter bw ;
            try {
                // 文件输出流 追加
                fos = new FileOutputStream(file);
                bw = new BufferedWriter(new OutputStreamWriter(fos, StandardCharsets.UTF_8));
                bw.write(fileContent);
                logger.info("写入文件内容******");
                bw.flush();
                bw.close();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static class ApiRoutes {
        private final static String SEPARATOR = "#";
        private final static String DEFAULT_HTTP_METHOD = "*";
        private final static String DEFAULT_METHOD = "/";

        private String httpMethod = DEFAULT_HTTP_METHOD;
        private String method = DEFAULT_METHOD;

        private String routeUniqueIdentity;
        private String beanName;
        private String fullName;

        public static ApiRoutes valueOf (String beanName, String httpMethod, String method, String fullName) {
            ApiRoutes ApiRoutes = new ApiRoutes();
            ApiRoutes.setBeanName(beanName);
            ApiRoutes.setHttpMethod(StringUtils.hasText(httpMethod) ? httpMethod : DEFAULT_HTTP_METHOD);
            ApiRoutes.setMethod(StringUtils.hasText(method) ? method : DEFAULT_METHOD);
            ApiRoutes.setFullName(fullName);
            ApiRoutes.setRouteUniqueIdentity(ApiRoutes.getUniqueIdentity());
            return ApiRoutes;
        }

        public static ApiRoutes valueOf (String httpMethod, String method) {
            ApiRoutes ApiRoutes = new ApiRoutes();
            ApiRoutes.setHttpMethod(StringUtils.hasText(httpMethod) ? httpMethod : DEFAULT_HTTP_METHOD);
            ApiRoutes.setMethod(StringUtils.hasText(method) ? method : DEFAULT_METHOD);
            ApiRoutes.setRouteUniqueIdentity(ApiRoutes.getUniqueIdentity());
            return ApiRoutes;
        }

        public String getUniqueIdentity() {
            return httpMethod + SEPARATOR + method;
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
//        result = prime * result + ((beanName == null) ? 0 : beanName.hashCode());
            result = prime * result + ((routeUniqueIdentity == null) ? 0 : routeUniqueIdentity.hashCode());
            return result;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) return true;
            if (obj == null) return false;
            if (getClass() != obj.getClass()) return false;
            ApiRoutes other = (ApiRoutes) obj;
//        if (beanName == null) {
//            if (other.beanName != null) return false;
//        } else if (!beanName.equals(other.beanName)) return false;
            if (routeUniqueIdentity == null) {
                if (other.routeUniqueIdentity != null) return false;
            } else if (!routeUniqueIdentity.equals(other.routeUniqueIdentity)) return false;
            return true;
        }

        @Override
        public String toString() {
            return "ApiRoutes [httpMethod=" + httpMethod + ", method=" + method + "]";
        }

        public String getHttpMethod() {
            return httpMethod;
        }

        public void setHttpMethod(String httpMethod) {
            this.httpMethod = httpMethod;
        }

        public String getMethod() {
            return method;
        }

        public void setMethod(String method) {
            this.method = method;
        }

        public String getBeanName() {
            return beanName;
        }

        public void setBeanName(String beanName) {
            this.beanName = beanName;
        }

        public String getRouteUniqueIdentity() {
            return routeUniqueIdentity;
        }

        public void setRouteUniqueIdentity(String routeUniqueIdentity) {
            this.routeUniqueIdentity = routeUniqueIdentity;
        }

        public String getFullName() {
            return fullName;
        }

        public void setFullName(String fullName) {
            this.fullName = fullName;
        }
    }
}
