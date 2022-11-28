package com.binfast.adpter.doc;

import com.binfast.adpter.core.ApiRoute;
import com.binfast.adpter.core.ApiServlet;
import com.binfast.adpter.core.annotations.ApiMapping;
import com.binfast.adpter.core.kit.ClassKit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.ClassUtils;
import org.springframework.util.ResourceUtils;
import org.springframework.web.bind.annotation.RequestMapping;

import java.io.*;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author 刘斌
 * @date 2022/11/4 4:57 下午
 */
@Service
public class ApiDocServlet {
    private static final Logger logger = LoggerFactory.getLogger(ApiDocServlet.class);

    private final ApiServlet apiServlet;

    private final ApplicationContext applicationContext;

    public void scanRoutes() {
        // 获取项目路由信息
        Map<ApiRoute, ApiServlet.ApiRunnable> registers = apiServlet.getRegisters();
        // 获取RequestMapping路由信息
        List<ApiRoute> requestRoutes = scanRequestRoutes();
        ArrayList<ApiRoute> apiRoutes = new ArrayList<>(registers.keySet());
        if (requestRoutes.size() > 0) {
            apiRoutes.addAll(requestRoutes);
        }
        // 构造文件数据
        String content = buildRoutes(apiRoutes);
        // 创建文件
        createFile(content);
    }

    private List<ApiRoute> scanRequestRoutes() {
        List<ApiRoute> requestRouteList = new ArrayList<>();
        String[] names = applicationContext.getBeanDefinitionNames();
        for (String name : names) {
            if (name.contains("basicErrorController")) {
                continue;
            }
            Class<?> type = ClassKit.getCurrentByType(applicationContext, name);
            if (type == null) {
                continue;
            }
            for (Method method : type.getDeclaredMethods()) {
                RequestMapping mapping = AnnotatedElementUtils.findMergedAnnotation(method, RequestMapping.class);
//                // 包装一个API
                if (mapping != null) {
                    RequestMapping beanMapping = type.getAnnotation(RequestMapping.class);
                    ApiRoute apiRoute;
                    if (beanMapping != null) {
                        apiRoute = ApiRoute.valueOf(name, mapping.method().length == 0 ? null :mapping.method()[0].name(),
                                apiServlet.getPathPrefix() + (beanMapping.value().length == 0 ? "" : beanMapping.value()[0]) + (mapping.value().length == 0 ? "": mapping.value()[0]),
                                apiServlet.buildMethodFullName(method), "", "");
                    } else {
                        apiRoute = ApiRoute.valueOf(name, mapping.method().length == 0 ? null :mapping.method()[0].name(),
                                apiServlet.getPathPrefix() + "" + (mapping.value().length == 0 ? "": mapping.value()[0]),
                                apiServlet.buildMethodFullName(method), "", "");
                    }
                    requestRouteList.add(apiRoute);
                }
            }
        }
        return requestRouteList;
    }

    private String buildRoutes(List<ApiRoute> routeList) {
        StringBuilder builder = new StringBuilder();
        routeList = routeList.stream().sorted(Comparator.comparing(ApiRoute::getBeanName)).collect(Collectors.toList());
        String lastBeanName = "";
        String newLine = "\r\n";
        for (ApiRoute route : routeList) {
            if (route.getMethod().contains("swagger")) {
                continue;
            }
            if (!lastBeanName.equals(route.getBeanName())) {
                builder.append(newLine)
                        .append("# ")
                        .append(route.getBeanName())
                        .append("  ")
                        .append(route.getBeanNotes())
                        .append(newLine);
                lastBeanName = route.getBeanName();
            }
            if (route.getNotes().length() > 0) {
                builder.append("# ")
                        .append(route.getNotes())
                        .append(newLine);
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

    public ApiDocServlet(ApiServlet apiServlet, ApplicationContext applicationContext) {
        this.apiServlet = apiServlet;
        this.applicationContext = applicationContext;
    }
}
