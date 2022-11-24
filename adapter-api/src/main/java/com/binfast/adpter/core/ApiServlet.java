package com.binfast.adpter.core;

import com.alibaba.cola.exception.BizException;
import com.alibaba.cola.exception.ExceptionFactory;
import com.alibaba.cola.exception.SysException;
import com.alibaba.fastjson2.JSON;
import com.binfast.adpter.core.annotations.ApiMapping;
import com.binfast.adpter.core.factory.GlobalExceptionResolver;
import com.binfast.adpter.core.factory.ResponseFactory;
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
import org.springframework.util.ClassUtils;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.method.annotation.ExceptionHandlerMethodResolver;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author 刘斌
 * @date 2022/11/5 5:39 下午
 */
public class ApiServlet implements InitializingBean, ApplicationContextAware {
    private static final Logger logger = LoggerFactory.getLogger(ApiServlet.class);
    private static final String FAIL_CODE = "SYS_ERROR";
    private static final Pattern PATH_VARIABLE_PATTERN = Pattern.compile("\\{[^/]+?\\}");
    private static final String PATH_VARIABLE_REPLACE = "([^/]+)";

    private final Map<ApiRoute, ApiRunnable> registers = new HashMap<>();
    private final Map<Integer, ApiRunnable> regexRouteRegisters = new HashMap<>();
    private Pattern regexRoutePatterns;
    private final Map<Object, ExceptionHandlerMethodResolver> exceptionHandlerCache =
            new ConcurrentHashMap<>(64);

    private final ParameterNameDiscoverer nameDiscoverer;
    private ApplicationContext context;
    private String pathPrefix = "";
    private ResponseFactory responseFactory;

    public ApiServlet(String pathPrefix) {
        this.pathPrefix = pathPrefix;
        this.nameDiscoverer = new LocalVariableTableParameterNameDiscoverer();
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        loadApi();
        loadExceptionResolver();
        responseFactory = new ResponseFactory(context);
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

    private void loadExceptionResolver() {
        Map<String, GlobalExceptionResolver> beansOfType = context.getBeansOfType(GlobalExceptionResolver.class);
        beansOfType.values().forEach(beanType -> {
            Class<?> adviceBean = beanType.getClass();
            boolean cglibProxy = Proxy.isProxyClass(adviceBean) || adviceBean.getName().contains("$$");
            if (cglibProxy) {
                adviceBean = ClassUtils.getUserClass(adviceBean);
            }
            ExceptionHandlerMethodResolver resolver = new ExceptionHandlerMethodResolver(adviceBean);
            if (resolver.hasExceptionMappings()) {
                this.exceptionHandlerCache.put(beanType, resolver);
            }
        });
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

    /**
     * 路由解析入口
     */
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
                    for (i = 1; matcher.group(i) == null; i++) ;
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
            writeResult(responseFactory.buildSuccess(result), resp);
        } catch (SysException e) {
            logger.error("系统警告：{}", e.getMessage());
            resp.setStatus(Integer.parseInt(e.getErrCode()));
            writeResult(responseFactory.buildFailure(e.getErrCode(), e.getMessage()), resp);
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
            writeResult(responseFactory.buildFailure(exception.getErrCode(), exception.getMessage()), resp);
        } else if (e.getTargetException() instanceof SysException) {
            SysException exception = (SysException) e.getTargetException();
            logger.error("系统警告：{}", exception.getMessage());
            resp.setStatus(Integer.parseInt(exception.getErrCode()));
            writeResult(responseFactory.buildFailure(exception.getErrCode(), exception.getMessage()), resp);
        } else if (e.getTargetException() instanceof IllegalArgumentException) {
            IllegalArgumentException exception = (IllegalArgumentException) e.getTargetException();
            logger.error("系统警告：{}", exception.getMessage());
            resp.setStatus(HttpStatus.OK.value());
            writeResult(responseFactory.buildFailure(FAIL_CODE, exception.getMessage()), resp);
        } else if (e.getTargetException() instanceof MethodArgumentNotValidException) {
            MethodArgumentNotValidException exception = (MethodArgumentNotValidException) e.getTargetException();
            logger.error("系统警告：{}", exception.getMessage());
            StringBuilder message = new StringBuilder();
            List<FieldError> fieldErrors = exception.getBindingResult().getFieldErrors();
            for (FieldError error : fieldErrors) {
                message.append(error.getField()).append(":").append(error.getDefaultMessage()).append(",");
            }
            String msg = "";
            if (message.length() > 0) {
                msg = message.substring(0, message.length() - 1);
            }
            resp.setStatus(HttpStatus.BAD_GATEWAY.value());
            writeResult(responseFactory.buildFailure(FAIL_CODE, msg), resp);
        } else if (e.getTargetException() instanceof BindException) {
            BindException exception = (BindException) e.getTargetException();
            logger.error("系统警告：{}", exception.getMessage());
            StringBuilder message = new StringBuilder();
            List<FieldError> fieldErrors = exception.getBindingResult().getFieldErrors();
            for (FieldError error : fieldErrors) {
                message.append(error.getField()).append(":").append(error.getDefaultMessage()).append(",");
            }
            String msg = "";
            if (message.length() > 0) {
                msg = message.substring(0, message.length() - 1);
            }
            resp.setStatus(HttpStatus.BAD_GATEWAY.value());
            writeResult(responseFactory.buildFailure(FAIL_CODE, msg), resp);
        } else {
            Exception exception = (Exception) e.getTargetException();
            logger.error("服务响应错误: {}", exception.getMessage());
            Method method = null;
            for (Map.Entry<Object, ExceptionHandlerMethodResolver> resolverEntry : exceptionHandlerCache.entrySet()) {
                method = resolverEntry.getValue().resolveMethod(exception);
                if (method != null) {
                    try {
                        ResponseStatus annotation = method.getAnnotation(ResponseStatus.class);
                        Object invokeResult = method.invoke(resolverEntry.getKey(), exception);
                        resp.setStatus(annotation != null ? annotation.value().value() : HttpStatus.INTERNAL_SERVER_ERROR.value());
                        writeResult(responseFactory.buildFailure(invokeResult), resp);
                        break;
                    } catch (IllegalAccessException | InvocationTargetException ex) {
                        ex.printStackTrace();
                    }
                }
            }
            if (method == null) {
                resp.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
                exception.printStackTrace();
                writeResult(responseFactory.buildFailure(FAIL_CODE, "服务响应错误"), resp);
            }
        }
    }

    public class ApiRunnable {
        Method method;
        Object target;
        String beanName;
        private Object[] args;
        private List<String> uriVariableNames;

        private final ParaProcessor parameterGetter;

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
