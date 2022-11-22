package com.binfast.adpter.core;

import com.alibaba.cola.dto.Response;
import com.alibaba.cola.exception.BizException;
import com.alibaba.cola.exception.ExceptionFactory;
import com.alibaba.cola.exception.SysException;
import com.alibaba.fastjson2.JSON;
import com.binfast.adpter.core.annotations.ApiMapping;
import com.binfast.adpter.core.paramconvert.ParaProcessor;
import com.binfast.adpter.core.paramconvert.ParaProcessorBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.core.LocalVariableTableParameterNameDiscoverer;
import org.springframework.core.ParameterNameDiscoverer;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.http.HttpStatus;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author 刘斌
 * @date 2022/11/5 5:39 下午
 */
public class ApiServlet implements InitializingBean, ApplicationContextAware {
    private static final Logger logger = LoggerFactory.getLogger(ApiServlet.class);
    private static final String FAIL_CODE = "1";
    private static final Pattern PATH_VARIABLE_PATTERN =  Pattern.compile("\\{[^/]+?\\}");
    private static final String PATH_VARIABLE_REPLACE =  "([^/]+)";

    private Map<ApiRoute, ApiRunnable> registers = new HashMap<>();
    private Map<Integer, ApiRunnable> regexRouteRegisters = new HashMap<>();
    private Pattern regexRoutePatterns;

    private ParameterNameDiscoverer nameDiscoverer;
    private ApplicationContext context;
    private String pathPrefix = "";

    public ApiServlet(String pathPrefix) {
        this.pathPrefix = pathPrefix;
        this.nameDiscoverer = new LocalVariableTableParameterNameDiscoverer();
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        loadApi();
    }

    private void loadApi() {
        String[] names = context.getBeanDefinitionNames();
        int indexes = 1;
        StringBuilder patternBuilders = new StringBuilder("^");
        for (String name : names) {
            Class<?> type = context.getType(name);
            for (Method method : type.getDeclaredMethods()) {
                ApiMapping mapping = AnnotatedElementUtils.findMergedAnnotation(method, ApiMapping.class);
//                // 包装一个API
                if (mapping != null) {
                    ApiMapping beanMapping = type.getAnnotation(ApiMapping.class);
                    String beanValue = "", beanNote = "";
                    if (beanMapping != null) {
                        beanValue = beanMapping.value();
                        beanNote = beanMapping.notes();
                    }
                    indexes = registerApi(name, method, mapping, beanValue, beanNote, indexes, patternBuilders);
                }
            }
        }
        patternBuilders.setCharAt(patternBuilders.length() - 1, '$');
        regexRoutePatterns = Pattern.compile(patternBuilders.toString());
        logger.info("loaded API ");
    }

    /**
     * 注册一个API
     */
    private int registerApi(String beanName, Method method, ApiMapping mapping, String beanValue, String beanNotes, int indexes, StringBuilder patternBuilders) {
        String path = pathPrefix + beanValue + mapping.value();
        ApiRunnable runnable = new ApiRunnable(method, beanName);
        ApiRoute apiRoute = ApiRoute.valueOf(beanName, mapping.method() == null || mapping.method().length == 0 ? null : mapping.method()[0].name(),
                path, buildMethodFullName(method), beanNotes, mapping.notes());

        Matcher matcher = PATH_VARIABLE_PATTERN.matcher(path);
        boolean find = false;
        List<String> uriVariableNames = new ArrayList<>();
        while (matcher.find()) {
            if (!find) {
                find = true;
            }
            String group = matcher.group(0);
            // {id} -> id
            uriVariableNames.add(group.substring(1, group.length() - 1));
        }
        if (find) {
            regexRouteRegisters.put(indexes, runnable);
            runnable.setUriVariableNames(uriVariableNames);
            indexes = indexes + uriVariableNames.size() + 1;
            patternBuilders.append("(").append(matcher.replaceAll(PATH_VARIABLE_REPLACE)).append(")|");
        }
        registers.put(apiRoute, runnable);
        return indexes;
    }

    // 获取方法的全限定名
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
                for (int i = 0; i < method.getParameterCount(); i++) {
                    String typeName = parameterTypes[i].getName();
                    int splitIndex = typeName.contains("$") ? typeName.lastIndexOf("$") + 1 : typeName.lastIndexOf(".") + 1;
                    sj.add(typeName.substring(splitIndex) + " " + parameterNames[i]);
                }
                builder.append(sj);
            }
        }
        builder.append(")");
        return builder.toString();
    }

    // ！！入口！！
    private void doDispatch(HttpServletRequest req, HttpServletResponse resp) {
        String method = req.getMethod();
        ApiRoute apiRoute = ApiRoute.valueOf(method, req.getServletPath());
        try {
            ApiRunnable runnable = registers.get(apiRoute);
            Map<String, String> uriVariables = null;
            if (runnable == null) {
                uriVariables = new LinkedHashMap<>();
                Matcher matcher = regexRoutePatterns.matcher(req.getServletPath());
                if (matcher.matches()) {
                    int i;
                    for (i = 1; matcher.group(i) == null; i++);
                    runnable = regexRouteRegisters.get(i);

                    // find path variable
                    String uriVariable;
                    int j = 0;
                    while (++i <= matcher.groupCount() && (uriVariable = matcher.group(i)) != null) {
                        uriVariables.put(runnable.getUriVariableNames().get(j++), uriVariable);
                    }
                }
                if (runnable == null) {
                    throw ExceptionFactory.sysException("400", "未找到路由");
                }
            }
            ParamsHandler paramsHandler = buildParam(uriVariables, req, resp);
            Object result = runnable.run(paramsHandler);
            resp.setStatus(HttpStatus.OK.value());
            writeResult(result, resp);
        } catch (SysException e) {
            logger.error("系统警告：{}", e.getMessage());
            resp.setStatus(Integer.parseInt(e.getErrCode()));
            writeResult(Response.buildFailure(e.getErrCode(), e.getMessage()), resp);
        } catch (InvocationTargetException e) {
            handleInvocationTargetException(e, resp);
        }
    }

    private ParamsHandler buildParam(Map<String, String> uriVariables, HttpServletRequest req, HttpServletResponse resp) {
        ParamsHandler paramsHandler = new ParamsHandler(req, resp, uriVariables);
        if (paramsHandler.isJsonRequest()) {
            paramsHandler.setRequest(new JsonRequest(paramsHandler.getRawData(), req));

        }
        return paramsHandler;
//        paramsHandler.getRawData();
//        Class<?>[] types = apiRunnable.method.getParameterTypes(); //参数类别
//        String[] names = nameDiscoverer.getParameterNames(apiRunnable.method);
//
//
//        Object[] args = new Object[names.length];
//        // 转成map
//        Map<String, Object> map = null;
//        try {
//            map = JSON.parseObject("parameter", Map.class);
//        } catch (IllegalArgumentException e) {
////            throw new ApiException(ApiException.ERROR_CODE_CLIENT_ERROR, "调用失败：json字符串格式异常，请检查params参数 ");
//            throw new RuntimeException("调用失败：json字符串格式异常，请检查params参数 ");
//        }
//
//        for (Map.Entry<String, Object> m : map.entrySet()) {
//            if (!Stream.of(names).anyMatch(n -> n.equals(m.getKey()))) {
////                throw new ApiException(ApiException.ERROR_CODE_CLIENT_ERROR, "调用失败：接口不存在‘" + m.getKey() + "’参数");
//                throw new RuntimeException("调用失败：接口不存在‘" + m.getKey() + "’参数");
//            }
//        }
//
//        for (int i = 0; i < names.length; i++) {
//            if (!map.containsKey(names[i])) {
//                continue;
//            }
//            Object arg = null;
//            try {
//                arg = convertJsonToBean(map.get(names[i]), types[i]);
//            } catch (IllegalArgumentException e) {
////                throw new ApiException(ApiException.ERROR_CODE_CLIENT_ERROR, String.format("调用失败：json字符串格式异常，请检查%s参数 ", names[i]));
//                throw new RuntimeException(String.format("调用失败：json字符串格式异常，请检查%s参数 ", names[i]));
//
//            }
//            args[i] = arg;
//        }
//        return args;
        // -parameters
        // 不确定
    }

    /**
     * 写回结果
     */
    private void writeResult(Object result, HttpServletResponse response) {
        if (result != null) {
            response.setCharacterEncoding("UTF-8");
            response.setContentType("text/html/json;charset=utf-8");
            response.setHeader("Cache-Control", "no-cache");
            response.setHeader("Pragma", "no-cache");
            response.setDateHeader("Expires", 0);
            try {
                response.getWriter().write(JSON.toJSONString(result));
            } catch (IOException e) {
                logger.error("网络错误", e);
            }
        }
    }

    private void handleInvocationTargetException(InvocationTargetException e, HttpServletResponse resp) {
        if (e.getTargetException() instanceof BizException) {
            BizException exception = (BizException) e.getTargetException();
            logger.error("系统警告：{}", exception.getMessage());
            resp.setStatus(HttpStatus.OK.value());
            writeResult(Response.buildFailure(exception.getErrCode(), exception.getMessage()), resp);
        } else if (e.getTargetException() instanceof SysException) {
            SysException exception = (SysException) e.getTargetException();
            logger.error("系统警告：{}", exception.getMessage());
            resp.setStatus(Integer.parseInt(exception.getErrCode()));
            writeResult(Response.buildFailure(exception.getErrCode(), exception.getMessage()), resp);
        } else if (e.getTargetException() instanceof IllegalArgumentException) {
            IllegalArgumentException exception = (IllegalArgumentException) e.getTargetException();
            logger.error("系统警告：{}", exception.getMessage());
            resp.setStatus(HttpStatus.OK.value());
            writeResult(Response.buildFailure(FAIL_CODE, exception.getMessage()), resp);
        } else if (e.getTargetException() instanceof MethodArgumentNotValidException) {
            MethodArgumentNotValidException exception = (MethodArgumentNotValidException) e.getTargetException();
            logger.error("系统警告：{}", exception.getMessage());
            StringBuilder message = new StringBuilder();
            List<FieldError> fieldErrors = exception.getBindingResult().getFieldErrors();
            for(FieldError error:fieldErrors){
                message.append(error.getField()).append(":").append(error.getDefaultMessage()).append(",");
            }
            String msg = "";
            if(message.length()> 0) {
                msg = message.substring(0,message.length()-1);
            }
            resp.setStatus(HttpStatus.BAD_GATEWAY.value());
            writeResult(Response.buildFailure(FAIL_CODE, msg), resp);
        } else if (e.getTargetException() instanceof BindException) {
            BindException exception = (BindException) e.getTargetException();
            logger.error("系统警告：{}", exception.getMessage());
            StringBuilder message = new StringBuilder();
            List<FieldError> fieldErrors = exception.getBindingResult().getFieldErrors();
            for(FieldError error:fieldErrors){
                message.append(error.getField()).append(":").append(error.getDefaultMessage()).append(",");
            }
            String msg = "";
            if(message.length()> 0) {
                msg = message.substring(0,message.length()-1);
            }
            resp.setStatus(HttpStatus.BAD_GATEWAY.value());
            writeResult(Response.buildFailure(FAIL_CODE, msg), resp);
        } else {
            Exception exception = (Exception) e.getTargetException();
            logger.error("服务响应错误: {}", exception.getMessage());
            resp.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
            exception.printStackTrace();
            writeResult(Response.buildFailure(FAIL_CODE, "服务响应错误"), resp);
        }
    }

    private <T> Object convertJsonToBean(Object val, Class<T> targetClass) {
        Object result = null;
        if (val == null) {
            return null;
        } else if (Integer.class.equals(targetClass)) {
            result = Integer.parseInt(val.toString());
        } else if (Long.class.equals(targetClass)) {
            result = Long.parseLong(val.toString());
        } else if (Date.class.equals(targetClass)) {
            if (val.toString().matches("[0-9]+")) {
                result = new Date(Long.parseLong(val.toString()));
            } else {
                throw new IllegalArgumentException("日期必须是长整型的时间戳");
            }
        } else if (String.class.equals(targetClass)) {
            if (val instanceof String) {
                result = val;
            } else {
                throw new IllegalArgumentException("转换目标类型为字符串");
            }
        } else {
            result = UtilJson.convertValue(val, targetClass);
        }
        return result;
    }

    public class ApiRunnable {
        Method method;
        Object target;
        String beanName;
        private Object[] args;
        private List<String> uriVariableNames;

        private final ParaProcessor parameterGetter;

//        public ApiRunnable(Method method, String beanName, Object... args) {
//            this.method = method;
//            this.beanName = beanName;
//            this.parameterGetter = ParaProcessorBuilder.me.build(nameDiscoverer, method);
//            this.args = args;
//        }

        public ApiRunnable(Method method, String beanName) {
            this.method = method;
            this.beanName = beanName;
            this.parameterGetter = ParaProcessorBuilder.me.build(nameDiscoverer, method);
        }

        public Object run(ParamsHandler handler) throws InvocationTargetException {
            if (target == null) { //并发的问题？
                target = context.getBean(beanName);
            }
            try {
                this.args = parameterGetter.get(handler);
                return method.invoke(target, args);
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        }

        public List<String> getUriVariableNames() {
            return uriVariableNames;
        }

        public void setUriVariableNames(List<String> uriVariableNames) {
            this.uriVariableNames = uriVariableNames;
        }
    }

    public Map<ApiRoute, ApiRunnable> getRegisters() {
        return registers;
    }

    public String getPathPrefix() {
        return pathPrefix;
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.context = applicationContext;
    }
}
