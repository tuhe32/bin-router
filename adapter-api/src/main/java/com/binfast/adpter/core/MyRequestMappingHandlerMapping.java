package com.binfast.adpter.core;

import com.binfast.adpter.core.annotations.ApiMapping;
import org.springframework.context.ApplicationContext;
import org.springframework.context.EmbeddedValueResolverAware;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringValueResolver;
import org.springframework.web.accept.ContentNegotiationManager;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.handler.MatchableHandlerMapping;
import org.springframework.web.servlet.handler.RequestMatchResult;
import org.springframework.web.servlet.mvc.condition.RequestCondition;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.RequestMappingInfoHandlerMapping;
import org.springframework.web.util.UrlPathHelper;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Predicate;

/**
 * @author 刘斌
 * @date 2022/11/8 9:35 上午
 */
public class MyRequestMappingHandlerMapping extends RequestMappingInfoHandlerMapping
        implements MatchableHandlerMapping, EmbeddedValueResolverAware {

    private boolean useSuffixPatternMatch = false;

    private boolean useRegisteredSuffixPatternMatch = false;

    private boolean useTrailingSlashMatch = true;

    private String pathPrefix = "";

    private Map<String, Predicate<Class<?>>> pathPrefixes = Collections.emptyMap();

    private ContentNegotiationManager contentNegotiationManager = new ContentNegotiationManager();

    @Nullable
    private StringValueResolver embeddedValueResolver;

    private RequestMappingInfo.BuilderConfiguration config = new RequestMappingInfo.BuilderConfiguration();

    public MyRequestMappingHandlerMapping(String pathPrefix) {
        this.pathPrefix = pathPrefix;
    }

    public void setUseTrailingSlashMatch(boolean useTrailingSlashMatch) {
        this.useTrailingSlashMatch = useTrailingSlashMatch;
        if (getPatternParser() != null) {
            getPatternParser().setMatchOptionalTrailingSeparator(useTrailingSlashMatch);
        }
    }

    public void setPathPrefixes(Map<String, Predicate<Class<?>>> prefixes) {
        this.pathPrefixes = (!prefixes.isEmpty() ?
                Collections.unmodifiableMap(new LinkedHashMap<>(prefixes)) :
                Collections.emptyMap());
    }

    public Map<String, Predicate<Class<?>>> getPathPrefixes() {
        return this.pathPrefixes;
    }

    public void setContentNegotiationManager(ContentNegotiationManager contentNegotiationManager) {
        Assert.notNull(contentNegotiationManager, "ContentNegotiationManager must not be null");
        this.contentNegotiationManager = contentNegotiationManager;
    }

    /**
     * Return the configured {@link ContentNegotiationManager}.
     */
    public ContentNegotiationManager getContentNegotiationManager() {
        return this.contentNegotiationManager;
    }

    @Override
    public void setEmbeddedValueResolver(StringValueResolver resolver) {
        this.embeddedValueResolver = resolver;
    }

    @Override
    @SuppressWarnings("deprecation")
    public void afterPropertiesSet() {
        this.config = new RequestMappingInfo.BuilderConfiguration();
        this.config.setTrailingSlashMatch(useTrailingSlashMatch());
        this.config.setContentNegotiationManager(getContentNegotiationManager());

//        if (getPatternParser() != null) {
//            this.config.setPatternParser(getPatternParser());
//            Assert.isTrue(!this.useSuffixPatternMatch && !this.useRegisteredSuffixPatternMatch,
//                    "Suffix pattern matching not supported with PathPatternParser.");
//        }
//        else {
            this.config.setSuffixPatternMatch(useSuffixPatternMatch());
            this.config.setRegisteredSuffixPatternMatch(useRegisteredSuffixPatternMatch());
            this.config.setPathMatcher(getPathMatcher());
//        }

        super.afterPropertiesSet();
    }

    @Deprecated
    public boolean useSuffixPatternMatch() {
        return this.useSuffixPatternMatch;
    }

    @Deprecated
    public boolean useRegisteredSuffixPatternMatch() {
        return this.useRegisteredSuffixPatternMatch;
    }

    public boolean useTrailingSlashMatch() {
        return this.useTrailingSlashMatch;
    }

    public RequestMappingInfo.BuilderConfiguration getBuilderConfiguration() {
        return this.config;
    }

    @Override
    protected boolean isHandler(Class<?> beanType) {
        return AnnotatedElementUtils.hasAnnotation(beanType, ApiMapping.class);
    }

    @Override
    protected HandlerMethod lookupHandlerMethod(String lookupPath, HttpServletRequest request) throws Exception {
        ApplicationContext context = getApplicationContext();
        ApiServlet handlerBean = context.getBean(ApiServlet.class);
        Method targetMethod = ApiServlet.class.getDeclaredMethod("doDispatch", HttpServletRequest.class, HttpServletResponse.class);
        HandlerMethod method = createHandlerMethod(handlerBean, targetMethod);
        if (lookupPath.startsWith(pathPrefix)){
//            log.info("deal with path: {} ", lookupPath);
            request.setAttribute(BEST_MATCHING_HANDLER_ATTRIBUTE, method);
            request.setAttribute(BEST_MATCHING_PATTERN_ATTRIBUTE, lookupPath);
            return method;
        }
        return super.lookupHandlerMethod(lookupPath, request);
    }

    @Override
    protected RequestMappingInfo getMappingForMethod(Method method, Class<?> handlerType) {
        RequestMappingInfo info = createRequestMappingInfo(method);
        if (info != null) {
            RequestMappingInfo typeInfo = createRequestMappingInfo(handlerType);
            if (typeInfo != null) {
                info = typeInfo.combine(info);
            }
            String prefix = getPathPrefix(handlerType);
            if (prefix != null) {
                info = RequestMappingInfo.paths(prefix).options(this.config).build().combine(info);
            }
        }
        return info;
    }

    @Nullable
    String getPathPrefix(Class<?> handlerType) {
        for (Map.Entry<String, Predicate<Class<?>>> entry : this.pathPrefixes.entrySet()) {
            if (entry.getValue().test(handlerType)) {
                String prefix = entry.getKey();
                if (this.embeddedValueResolver != null) {
                    prefix = this.embeddedValueResolver.resolveStringValue(prefix);
                }
                return prefix;
            }
        }
        return null;
    }

    @Nullable
    private RequestMappingInfo createRequestMappingInfo(AnnotatedElement element) {
        ApiMapping apiMapping = AnnotatedElementUtils.findMergedAnnotation(element, ApiMapping.class);
        RequestCondition<?> condition = (element instanceof Class ?
                getCustomTypeCondition((Class<?>) element) : getCustomMethodCondition((Method) element));
        return (apiMapping != null ? createRequestMappingInfo(apiMapping, condition) : null);
    }

    @Nullable
    protected RequestCondition<?> getCustomTypeCondition(Class<?> handlerType) {
        return null;
    }

    @Nullable
    protected RequestCondition<?> getCustomMethodCondition(Method method) {
        return null;
    }

    protected RequestMappingInfo createRequestMappingInfo(
            ApiMapping apiMapping, @Nullable RequestCondition<?> customCondition) {

        RequestMappingInfo.Builder builder = RequestMappingInfo
                .paths(resolveEmbeddedValuesInPatterns(new String[]{apiMapping.value()}))
                .methods(apiMapping.method());
//                .params(requestMapping.params())
//                .headers(requestMapping.headers())
//                .consumes(requestMapping.consumes())
//                .produces(requestMapping.produces())
//                .mappingName(requestMapping.name());
        if (customCondition != null) {
            builder.customCondition(customCondition);
        }
        return builder.options(this.config).build();
    }

    protected String[] resolveEmbeddedValuesInPatterns(String[] patterns) {
        if (this.embeddedValueResolver == null) {
            return patterns;
        }
        else {
            String[] resolvedPatterns = new String[patterns.length];
            for (int i = 0; i < patterns.length; i++) {
                resolvedPatterns[i] = this.embeddedValueResolver.resolveStringValue(patterns[i]);
            }
            return resolvedPatterns;
        }
    }

    @Override
    public void registerMapping(RequestMappingInfo mapping, Object handler, Method method) {
        super.registerMapping(mapping, handler, method);
        updateConsumesCondition(mapping, method);
    }

    @Override
    protected void registerHandlerMethod(Object handler, Method method, RequestMappingInfo mapping) {
        super.registerHandlerMethod(handler, method, mapping);
        updateConsumesCondition(mapping, method);
    }

    private void updateConsumesCondition(RequestMappingInfo info, Method method) {
//        ConsumesRequestCondition condition = info.getConsumesCondition();
//        if (!condition.isEmpty()) {
//            for (Parameter parameter : method.getParameters()) {
//                MergedAnnotation<RequestBody> annot = MergedAnnotations.from(parameter).get(RequestBody.class);
//                if (annot.isPresent()) {
//                    condition.setBodyRequired(annot.getBoolean("required"));
//                    break;
//                }
//            }
//        }
    }

    @Override
    public RequestMatchResult match(HttpServletRequest request, String pattern) {
        Assert.isNull(getPatternParser(), "This HandlerMapping requires a PathPattern");
        RequestMappingInfo info = RequestMappingInfo.paths(pattern).options(this.config).build();
        RequestMappingInfo match = info.getMatchingCondition(request);
        return (match != null && match.getPatternsCondition() != null ?
                new RequestMatchResult(
                        match.getPatternsCondition().getPatterns().iterator().next(),
                        UrlPathHelper.getResolvedLookupPath(request),
                        getPathMatcher()) : null);
    }

    @Override
    protected CorsConfiguration initCorsConfiguration(Object handler, Method method, RequestMappingInfo mappingInfo) {
        HandlerMethod handlerMethod = createHandlerMethod(handler, method);
        Class<?> beanType = handlerMethod.getBeanType();
        CrossOrigin typeAnnotation = AnnotatedElementUtils.findMergedAnnotation(beanType, CrossOrigin.class);
        CrossOrigin methodAnnotation = AnnotatedElementUtils.findMergedAnnotation(method, CrossOrigin.class);

        if (typeAnnotation == null && methodAnnotation == null) {
            return null;
        }

        CorsConfiguration config = new CorsConfiguration();
        updateCorsConfig(config, typeAnnotation);
        updateCorsConfig(config, methodAnnotation);

        if (CollectionUtils.isEmpty(config.getAllowedMethods())) {
            for (RequestMethod allowedMethod : mappingInfo.getMethodsCondition().getMethods()) {
                config.addAllowedMethod(allowedMethod.name());
            }
        }
        return config.applyPermitDefaultValues();
    }

    private void updateCorsConfig(CorsConfiguration config, @Nullable CrossOrigin annotation) {
        if (annotation == null) {
            return;
        }
        for (String origin : annotation.origins()) {
            config.addAllowedOrigin(resolveCorsAnnotationValue(origin));
        }
        for (String patterns : annotation.originPatterns()) {
            config.addAllowedOriginPattern(resolveCorsAnnotationValue(patterns));
        }
        for (RequestMethod method : annotation.methods()) {
            config.addAllowedMethod(method.name());
        }
        for (String header : annotation.allowedHeaders()) {
            config.addAllowedHeader(resolveCorsAnnotationValue(header));
        }
        for (String header : annotation.exposedHeaders()) {
            config.addExposedHeader(resolveCorsAnnotationValue(header));
        }

        String allowCredentials = resolveCorsAnnotationValue(annotation.allowCredentials());
        if ("true".equalsIgnoreCase(allowCredentials)) {
            config.setAllowCredentials(true);
        }
        else if ("false".equalsIgnoreCase(allowCredentials)) {
            config.setAllowCredentials(false);
        }
        else if (!allowCredentials.isEmpty()) {
            throw new IllegalStateException("@CrossOrigin's allowCredentials value must be \"true\", \"false\", " +
                    "or an empty string (\"\"): current value is [" + allowCredentials + "]");
        }

        if (annotation.maxAge() >= 0 ) {
            config.setMaxAge(annotation.maxAge());
        }
    }

    private String resolveCorsAnnotationValue(String value) {
        if (this.embeddedValueResolver != null) {
            String resolved = this.embeddedValueResolver.resolveStringValue(value);
            return (resolved != null ? resolved : "");
        }
        else {
            return value;
        }
    }

}
