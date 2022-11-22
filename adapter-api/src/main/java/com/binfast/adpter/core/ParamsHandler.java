package com.binfast.adpter.core;

import com.alibaba.cola.exception.ExceptionFactory;
import com.binfast.adpter.core.converter.TypeConverter;
import com.binfast.adpter.core.kit.HttpKit;
import com.binfast.adpter.core.kit.StrKit;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartRequest;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.text.ParseException;
import java.util.Date;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;

/**
 * @author 刘斌
 * @date 2022/11/7 10:09 上午
 */
public class ParamsHandler {

    private HttpServletRequest request;
    private HttpServletResponse response;

    private String rawData;
    private String urlPara;
    private String[] urlParaArray;
    private Map<String, String> uriVariables;

    private static final String[] NULL_URL_PARA_ARRAY = new String[0];
    private static final String URL_PARA_SEPARATOR = "-";

    public ParamsHandler(HttpServletRequest request, HttpServletResponse response, Map<String, String> uriVariables) {
        this.request = request;
        this.response = response;
        this.uriVariables = uriVariables;
        this.urlPara = "";
        this.urlParaArray = null;
    }

    /**
     * 判断是否为 json 请求，contentType 包含 "json" 被认定为 json 请求
     */
    public boolean isJsonRequest() {
//        if (request instanceof JsonRequest) {
//            return true;
//        }
        String ct = request.getContentType();
        return ct != null && ct.indexOf("json") != -1;
    }

    /**
     * 获取 http 请求 body 中的原始数据，通常用于接收 json String 这类数据<br>
     * 可多次调用此方法，避免掉了 HttpKit.readData(...) 方式获取该数据时多次调用
     * 引发的异常
     * @return http 请求 body 中的原始数据
     */
    public String getRawData() {
        if (rawData == null) {
            rawData = HttpKit.readData(request);
        }
        return rawData;
    }

    public void setRequest(HttpServletRequest request) {
        this.request = request;
    }

    /**
     * Stores an attribute in this request
     * @param name a String specifying the name of the attribute
     * @param value the Object to be stored
     */
    public ParamsHandler setAttr(String name, Object value) {
        request.setAttribute(name, value);
        return this;
    }

    /**
     * Removes an attribute from this request
     * @param name a String specifying the name of the attribute to remove
     */
    public ParamsHandler removeAttr(String name) {
        request.removeAttribute(name);
        return this;
    }

    /**
     * Stores attributes in this request, key of the map as attribute name and value of the map as attribute value
     * @param attrMap key and value as attribute of the map to be stored
     */
    public ParamsHandler setAttrs(Map<String, Object> attrMap) {
        for (Map.Entry<String, Object> entry : attrMap.entrySet())
            request.setAttribute(entry.getKey(), entry.getValue());
        return this;
    }

    /**
     * Returns the value of a request parameter as a String, or null if the parameter does not exist.
     * <p>
     * You should only use this method when you are sure the parameter has only one value. If the
     * parameter might have more than one value, use getParaValues(java.lang.String).
     * <p>
     * If you use this method with a multivalued parameter, the value returned is equal to the first
     * value in the array returned by getParameterValues.
     * @param name a String specifying the name of the parameter
     * @return a String representing the single value of the parameter
     */
    public String getPara(String name) {
        // return request.getParameter(name);
        String result = checkAndGetPara(name);
        return result != null && result.length() != 0 ? result : null;
    }

    /**
     * Returns the value of a request parameter as a String, or default value if the parameter does not exist.
     * @param name a String specifying the name of the parameter
     * @param defaultValue a String value be returned when the value of parameter is null
     * @return a String representing the single value of the parameter
     */
    public String getPara(String name, String defaultValue) {
        String result = checkAndGetPara(name);
        return result != null && result.length() != 0 ? result : defaultValue;
    }

    private String checkAndGetPara(String name) {
        String result = request.getParameter(name);
        if (result == null || result.length() == 0) {
            if (uriVariables != null) {
                result = uriVariables.get(name);
            }
        }
        return result;
    }

    private String[] checkAndGetParaArr(String name) {
        String[] result = request.getParameterValues(name);
        if (result == null || result.length == 0) {
            if (uriVariables != null) {
                result = new String[]{uriVariables.get(name)};
            }
        }
        return result;
    }

    /**
     * Returns the values of the request parameters as a Map.
     * @return a Map contains all the parameters name and value
     */
    public Map<String, String[]> getParaMap() {
        return request.getParameterMap();
    }

    /**
     * Returns an Enumeration of String objects containing the names of the parameters
     * contained in this request. If the request has no parameters, the method returns
     * an empty Enumeration.
     * @return an Enumeration of String objects, each String containing the name of
     * 			a request parameter; or an empty Enumeration if the request has no parameters
     */
    public Enumeration<String> getParaNames() {
        return request.getParameterNames();
    }

    /**
     * Returns an array of String objects containing all of the values the given request
     * parameter has, or null if the parameter does not exist. If the parameter has a
     * single value, the array has a length of 1.
     * @param name a String containing the name of the parameter whose value is requested
     * @return an array of String objects containing the parameter's values
     */
    public String[] getParaValues(String name) {
        return checkAndGetParaArr(name);
    }

    /**
     * Returns an array of Integer objects containing all of the values the given request
     * parameter has, or null if the parameter does not exist. If the parameter has a
     * single value, the array has a length of 1.
     * @param name a String containing the name of the parameter whose value is requested
     * @return an array of Integer objects containing the parameter's values
     */
    public Integer[] getParaValuesToInt(String name) {
        String[] values = checkAndGetParaArr(name);
        if (values == null || values.length == 0) {
            return null;
        }
        Integer[] result = new Integer[values.length];
        for (int i=0; i<result.length; i++) {
            result[i] = StrKit.isBlank(values[i]) ? null : Integer.parseInt(values[i]);
        }
        return result;
    }

    public Long[] getParaValuesToLong(String name) {
        String[] values = checkAndGetParaArr(name);
        if (values == null || values.length == 0) {
            return null;
        }
        Long[] result = new Long[values.length];
        for (int i=0; i<result.length; i++) {
            result[i] = StrKit.isBlank(values[i]) ? null : Long.parseLong(values[i]);
        }
        return result;
    }

    /**
     * Returns an Enumeration containing the names of the attributes available to this request.
     * This method returns an empty Enumeration if the request has no attributes available to it.
     * @return an Enumeration of strings containing the names of the request's attributes
     */
    public Enumeration<String> getAttrNames() {
        return request.getAttributeNames();
    }

    /**
     * Returns the value of the named attribute as an Object, or null if no attribute of the given name exists.
     * @param name a String specifying the name of the attribute
     * @return an Object containing the value of the attribute, or null if the attribute does not exist
     */
    public <T> T getAttr(String name) {
        return (T)request.getAttribute(name);
    }

    public <T> T getAttr(String name, T defaultValue) {
        T result = (T)request.getAttribute(name);
        return result != null ? result : defaultValue;
    }

    /**
     * Returns the value of the named attribute as an Object, or null if no attribute of the given name exists.
     * @param name a String specifying the name of the attribute
     * @return an String Object containing the value of the attribute, or null if the attribute does not exist
     */
    public String getAttrForStr(String name) {
        return (String)request.getAttribute(name);
    }

    /**
     * Returns the value of the named attribute as an Object, or null if no attribute of the given name exists.
     * @param name a String specifying the name of the attribute
     * @return an Integer Object containing the value of the attribute, or null if the attribute does not exist
     */
    public Integer getAttrForInt(String name) {
        return (Integer)request.getAttribute(name);
    }

    /**
     * Returns the value of the specified request header as a String.
     */
    public String getHeader(String name) {
        return request.getHeader(name);
    }

    private Integer toInt(String value, Integer defaultValue) {
        try {
            if (StrKit.isBlank(value))
                return defaultValue;
            value = value.trim();
            if (value.startsWith("N") || value.startsWith("n"))
                return -Integer.parseInt(value.substring(1));
            return Integer.parseInt(value);
        }
        catch (Exception e) {
            throw ExceptionFactory.sysException(String.valueOf(400), "Can not parse the parameter \"" + value + "\" to Integer value.");
        }
    }

    /**
     * Returns the value of a request parameter and convert to Integer.
     * @param name a String specifying the name of the parameter
     * @return a Integer representing the single value of the parameter
     */
    public Integer getParaToInt(String name) {
        return toInt(request.getParameter(name), null);
    }

    /**
     * Returns the value of a request parameter and convert to Integer with a default value if it is null.
     * @param name a String specifying the name of the parameter
     * @return a Integer representing the single value of the parameter
     */
    public Integer getParaToInt(String name, Integer defaultValue) {
        return toInt(checkAndGetPara(name), defaultValue);
    }

    private Long toLong(String value, Long defaultValue) {
        try {
            if (StrKit.isBlank(value))
                return defaultValue;
            value = value.trim();
            if (value.startsWith("N") || value.startsWith("n"))
                return -Long.parseLong(value.substring(1));
            return Long.parseLong(value);
        }
        catch (Exception e) {
            throw ExceptionFactory.sysException(String.valueOf(400),  "Can not parse the parameter \"" + value + "\" to Long value.");
        }
    }

    /**
     * Returns the value of a request parameter and convert to Long.
     * @param name a String specifying the name of the parameter
     * @return a Integer representing the single value of the parameter
     */
    public Long getParaToLong(String name) {
        return toLong(request.getParameter(name), null);
    }

    /**
     * Returns the value of a request parameter and convert to Long with a default value if it is null.
     * @param name a String specifying the name of the parameter
     * @return a Integer representing the single value of the parameter
     */
    public Long getParaToLong(String name, Long defaultValue) {
        return toLong(checkAndGetPara(name), defaultValue);
    }

    private Boolean toBoolean(String value, Boolean defaultValue) {
        if (StrKit.isBlank(value))
            return defaultValue;
        value = value.trim().toLowerCase();
        if ("1".equals(value) || "true".equals(value))
            return Boolean.TRUE;
        else if ("0".equals(value) || "false".equals(value))
            return Boolean.FALSE;
        throw ExceptionFactory.sysException(String.valueOf(400), "Can not parse the parameter \"" + value + "\" to Boolean value.");
    }

    /**
     * Returns the value of a request parameter and convert to Boolean.
     * @param name a String specifying the name of the parameter
     * @return true if the value of the parameter is "true" or "1", false if it is "false" or "0", null if parameter is not exists
     */
    public Boolean getParaToBoolean(String name) {
        return toBoolean(request.getParameter(name), null);
    }

    /**
     * Returns the value of a request parameter and convert to Boolean with a default value if it is null.
     * @param name a String specifying the name of the parameter
     * @return true if the value of the parameter is "true" or "1", false if it is "false" or "0", default value if it is null
     */
    public Boolean getParaToBoolean(String name, Boolean defaultValue) {
        return toBoolean(checkAndGetPara(name), defaultValue);
    }

    /**
     * Get all para from url and convert to Boolean
     */
    public Boolean getParaToBoolean() {
        return toBoolean(getPara(), null);
    }

    /**
     * Get para from url and conver to Boolean. The first index is 0
     */
    public Boolean getParaToBoolean(int index) {
        return toBoolean(getPara(index), null);
    }

    /**
     * Get para from url and conver to Boolean with default value if it is null.
     */
    public Boolean getParaToBoolean(int index, Boolean defaultValue) {
        return toBoolean(getPara(index), defaultValue);
    }

    private Date toDate(String value, Date defaultValue) {
        try {
            if (StrKit.isBlank(value))
                return defaultValue;

            // return new java.text.SimpleDateFormat("yyyy-MM-dd").parse(value.trim());
            return (Date) TypeConverter.me().convert(Date.class, value);

        } catch (Exception e) {
            throw ExceptionFactory.sysException(String.valueOf(400), "Can not parse the parameter \"" + value + "\" to Date value.");
        }
    }

    /**
     * Returns the value of a request parameter and convert to Date.
     * @param name a String specifying the name of the parameter
     * @return a Date representing the single value of the parameter
     */
    public Date getParaToDate(String name) {
        return toDate(request.getParameter(name), null);
    }

    /**
     * Returns the value of a request parameter and convert to Date with a default value if it is null.
     * @param name a String specifying the name of the parameter
     * @return a Date representing the single value of the parameter
     */
    public Date getParaToDate(String name, Date defaultValue) {
        return toDate(checkAndGetPara(name), defaultValue);
    }

    /**
     * Get all para from url and convert to Date
     */
    public Date getParaToDate() {
        return toDate(getPara(), null);
    }

    /**
     * Return HttpServletRequest. Do not use HttpServletRequest Object in constructor of Controller
     */
    public HttpServletRequest getRequest() {
        return request;
    }

    /**
     * Return HttpServletResponse. Do not use HttpServletResponse Object in constructor of Controller
     */
    public HttpServletResponse getResponse() {
        return response;
    }

    /**
     * Return HttpSession.
     */
    public HttpSession getSession() {
        return request.getSession();
    }

    /**
     * Return HttpSession.
     * @param create a boolean specifying create HttpSession if it not exists
     */
    public HttpSession getSession(boolean create) {
        return request.getSession(create);
    }

    /**
     * Return a Object from session.
     * @param key a String specifying the key of the Object stored in session
     */
    public <T> T getSessionAttr(String key) {
        HttpSession session = request.getSession(false);
        return session != null ? (T)session.getAttribute(key) : null;
    }

    public <T> T getSessionAttr(String key, T defaultValue) {
        T result = getSessionAttr(key);
        return result != null ? result : defaultValue;
    }

    /**
     * Store Object to session.
     * @param key a String specifying the key of the Object stored in session
     * @param value a Object specifying the value stored in session
     */
    public ParamsHandler setSessionAttr(String key, Object value) {
        request.getSession(true).setAttribute(key, value);
        return this;
    }

    /**
     * Remove Object in session.
     * @param key a String specifying the key of the Object stored in session
     */
    public ParamsHandler removeSessionAttr(String key) {
        HttpSession session = request.getSession(false);
        if (session != null)
            session.removeAttribute(key);
        return this;
    }

    /**
     * Get cookie value by cookie name.
     */
    public String getCookie(String name, String defaultValue) {
        Cookie cookie = getCookieObject(name);
        return cookie != null ? cookie.getValue() : defaultValue;
    }

    /**
     * Get cookie value by cookie name.
     */
    public String getCookie(String name) {
        return getCookie(name, null);
    }

    /**
     * Get cookie value by cookie name and convert to Integer.
     */
    public Integer getCookieToInt(String name) {
        String result = getCookie(name);
        return result != null ? Integer.parseInt(result) : null;
    }

    /**
     * Get cookie value by cookie name and convert to Integer.
     */
    public Integer getCookieToInt(String name, Integer defaultValue) {
        String result = getCookie(name);
        return result != null ? Integer.parseInt(result) : defaultValue;
    }

    /**
     * Get cookie value by cookie name and convert to Long.
     */
    public Long getCookieToLong(String name) {
        String result = getCookie(name);
        return result != null ? Long.parseLong(result) : null;
    }

    /**
     * Get cookie value by cookie name and convert to Long.
     */
    public Long getCookieToLong(String name, Long defaultValue) {
        String result = getCookie(name);
        return result != null ? Long.parseLong(result) : defaultValue;
    }

    /**
     * Get cookie object by cookie name.
     */
    public Cookie getCookieObject(String name) {
        Cookie[] cookies = request.getCookies();
        if (cookies != null)
            for (Cookie cookie : cookies)
                if (cookie.getName().equals(name))
                    return cookie;
        return null;
    }

    /**
     * Get all cookie objects.
     */
    public Cookie[] getCookieObjects() {
        Cookie[] result = request.getCookies();
        return result != null ? result : new Cookie[0];
    }

    /**
     * Set Cookie.
     * @param name cookie name
     * @param value cookie value
     * @param maxAgeInSeconds -1: clear cookie when close browser. 0: clear cookie immediately.  n>0 : max age in n seconds.
     * @param isHttpOnly true if this cookie is to be marked as HttpOnly, false otherwise
     */
    public ParamsHandler setCookie(String name, String value, int maxAgeInSeconds, boolean isHttpOnly) {
        return doSetCookie(name, value, maxAgeInSeconds, null, null, isHttpOnly);
    }

    /**
     * Set Cookie.
     * @param name cookie name
     * @param value cookie value
     * @param maxAgeInSeconds -1: clear cookie when close browser. 0: clear cookie immediately.  n>0 : max age in n seconds.
     */
    public ParamsHandler setCookie(String name, String value, int maxAgeInSeconds) {
        return doSetCookie(name, value, maxAgeInSeconds, null, null, null);
    }

    /**
     * Set Cookie to response.
     */
    public ParamsHandler setCookie(Cookie cookie) {
        response.addCookie(cookie);
        return this;
    }

    /**
     * Set Cookie to response.
     * @param name cookie name
     * @param value cookie value
     * @param maxAgeInSeconds -1: clear cookie when close browser. 0: clear cookie immediately.  n>0 : max age in n seconds.
     * @param path see Cookie.setPath(String)
     * @param isHttpOnly true if this cookie is to be marked as HttpOnly, false otherwise
     */
    public ParamsHandler setCookie(String name, String value, int maxAgeInSeconds, String path, boolean isHttpOnly) {
        return doSetCookie(name, value, maxAgeInSeconds, path, null, isHttpOnly);
    }

    /**
     * Set Cookie to response.
     * @param name cookie name
     * @param value cookie value
     * @param maxAgeInSeconds -1: clear cookie when close browser. 0: clear cookie immediately.  n>0 : max age in n seconds.
     * @param path see Cookie.setPath(String)
     */
    public ParamsHandler setCookie(String name, String value, int maxAgeInSeconds, String path) {
        return doSetCookie(name, value, maxAgeInSeconds, path, null, null);
    }

    /**
     * Set Cookie to response.
     * @param name cookie name
     * @param value cookie value
     * @param maxAgeInSeconds -1: clear cookie when close browser. 0: clear cookie immediately.  n>0 : max age in n seconds.
     * @param path see Cookie.setPath(String)
     * @param domain the domain name within which this cookie is visible; form is according to RFC 2109
     * @param isHttpOnly true if this cookie is to be marked as HttpOnly, false otherwise
     */
    public ParamsHandler setCookie(String name, String value, int maxAgeInSeconds, String path, String domain, boolean isHttpOnly) {
        return doSetCookie(name, value, maxAgeInSeconds, path, domain, isHttpOnly);
    }

    /**
     * Remove Cookie.
     */
    public ParamsHandler removeCookie(String name) {
        return doSetCookie(name, null, 0, null, null, null);
    }

    /**
     * Remove Cookie.
     */
    public ParamsHandler removeCookie(String name, String path) {
        return doSetCookie(name, null, 0, path, null, null);
    }

    /**
     * Remove Cookie.
     */
    public ParamsHandler removeCookie(String name, String path, String domain) {
        return doSetCookie(name, null, 0, path, domain, null);
    }

    protected ParamsHandler doSetCookie(String name, String value, int maxAgeInSeconds, String path, String domain, Boolean isHttpOnly) {
        Cookie cookie = new Cookie(name, value);
        cookie.setMaxAge(maxAgeInSeconds);
        // set the default path value to "/"
        if (StrKit.isBlank(path)) {
            path = "/";
        }
        cookie.setPath(path);

        if (domain != null) {
            cookie.setDomain(domain);
        }
        if (isHttpOnly != null) {
            cookie.setHttpOnly(isHttpOnly);
        }
        response.addCookie(cookie);
        return this;
    }

    // --------

    /**
     * Get all para with separator char from url
     */
    public String getPara() {
        if ("".equals(urlPara))	// urlPara maybe is "" see ActionMapping.getAction(String)
            urlPara = null;
        return urlPara;
    }

    /**
     * Get para from url. The index of first url para is 0.
     */
    public String getPara(int index) {
        if (index < 0)
            return getPara();

        if (urlParaArray == null) {
            if (urlPara == null || "".equals(urlPara))	// urlPara maybe is "" see ActionMapping.getAction(String)
                urlParaArray = NULL_URL_PARA_ARRAY;
            else
                urlParaArray = urlPara.split(URL_PARA_SEPARATOR);

            for (int i=0; i<urlParaArray.length; i++)
                if ("".equals(urlParaArray[i]))
                    urlParaArray[i] = null;
        }
        return urlParaArray.length > index ? urlParaArray[index] : null;
    }

    /**
     * Get para from url with default value if it is null or "".
     */
    public String getPara(int index, String defaultValue) {
        String result = getPara(index);
        return result != null && result.length() != 0 ? result : defaultValue;
    }

    /**
     * Get para from url and conver to Integer. The first index is 0
     */
    public Integer getParaToInt(int index) {
        return toInt(getPara(index), null);
    }

    /**
     * Get para from url and conver to Integer with default value if it is null.
     */
    public Integer getParaToInt(int index, Integer defaultValue) {
        return toInt(getPara(index), defaultValue);
    }

    /**
     * Get para from url and conver to Long.
     */
    public Long getParaToLong(int index) {
        return toLong(getPara(index), null);
    }

    /**
     * Get para from url and conver to Long with default value if it is null.
     */
    public Long getParaToLong(int index, Long defaultValue) {
        return toLong(getPara(index), defaultValue);
    }

    /**
     * Get all para from url and convert to Integer
     */
    public Integer getParaToInt() {
        return toInt(getPara(), null);
    }

    /**
     * Get all para from url and convert to Long
     */
    public Long getParaToLong() {
        return toLong(getPara(), null);
    }

    /**
     * Get model from http request.
     */
//    public <T> T getModel(Class<T> modelClass) {
//        return (T)Injector.injectModel(modelClass, request, false);
//    }
//
//    public <T> T getModel(Class<T> modelClass, boolean skipConvertError) {
//        return (T)Injector.injectModel(modelClass, request, skipConvertError);
//    }

    /**
     * Get model from http request.
     */
//    public <T> T getModel(Class<T> modelClass, String modelName) {
//        return (T)Injector.injectModel(modelClass, modelName, request, false);
//    }
//
//    public <T> T getModel(Class<T> modelClass, String modelName, boolean skipConvertError) {
//        return (T)Injector.injectModel(modelClass, modelName, request, skipConvertError);
//    }

    public <T> T getBean(Class<T> beanClass) {
        return (T)Injector.injectBean(beanClass, request, false);
    }

    public <T> T getBean(Class<T> beanClass, boolean skipConvertError) {
        return (T)Injector.injectBean(beanClass, request, skipConvertError);
    }

    public <T> T getBean(Class<T> beanClass, String beanName) {
        return (T)Injector.injectBean(beanClass, beanName, request, false);
    }

    public <T> T getBean(Class<T> beanClass, String beanName, boolean skipConvertError) {
        return (T)Injector.injectBean(beanClass, beanName, request, skipConvertError);
    }

    // --------

    /**
     * Get upload file from multipart request.
     */
    public List<MultipartFile> getFiles(String uploadPath, long maxPostSize, String encoding) {
//        if (request instanceof MultipartRequest == false)
//            request = new MultipartRequest(request, uploadPath, maxPostSize, encoding);
        return ((MultipartRequest)request).getFiles(uploadPath);
    }

    public MultipartFile getFile(String parameterName, String uploadPath, long maxPostSize, String encoding) {
        getFiles(uploadPath, maxPostSize, encoding);
        return getFile(parameterName);
    }

    public List<MultipartFile> getFiles(String uploadPath, long maxPostSize) {
//        if (request instanceof MultipartRequest == false)
//            request = new MultipartRequest(request, uploadPath, maxPostSize);
        return ((MultipartRequest)request).getFiles(uploadPath);
    }

    public MultipartFile getFile(String parameterName, String uploadPath, long maxPostSize) {
        getFiles(uploadPath, maxPostSize);
        return getFile(parameterName);
    }

    public List<MultipartFile> getFiles(String uploadPath) {
//        if (request instanceof MultipartRequest == false)
//            request = new MultipartRequest(request, uploadPath);
        return ((MultipartRequest)request).getFiles(uploadPath);
    }

    public MultipartFile getFile(String parameterName, String uploadPath) {
        getFiles(uploadPath);
        return getFile(parameterName);
    }

    public List<MultipartFile> getFiles() {
//        if (request instanceof MultipartRequest == false)
//            request = new MultipartRequest(request);
//        return ((MultipartRequest)request).getFiles();
        return null;
    }

    public MultipartFile getFile() {
        List<MultipartFile> uploadFiles = getFiles();
        return uploadFiles.size() > 0 ? uploadFiles.get(0) : null;
    }

    public MultipartFile getFile(String parameterName) {
        List<MultipartFile> uploadFiles = getFiles();
        for (MultipartFile uploadFile : uploadFiles) {
            if (uploadFile.getName().equals(parameterName)) {
                return uploadFile;
            }
        }
        return null;
    }

    /**
     * Keep all parameter's value except model value
     */
    public ParamsHandler keepPara() {
        Map<String, String[]> map = request.getParameterMap();
        for (Map.Entry<String, String[]> e: map.entrySet()) {
            String[] values = e.getValue();
            if (values != null && values.length == 1)
                request.setAttribute(e.getKey(), values[0]);
            else
                request.setAttribute(e.getKey(), values);
        }
        return this;
    }

    /**
     * Keep parameter's value names pointed, model value can not be kept
     */
    public ParamsHandler keepPara(String... names) {
        for (String name : names) {
            String[] values = request.getParameterValues(name);
            if (values != null) {
                if (values.length == 1)
                    request.setAttribute(name, values[0]);
                else
                    request.setAttribute(name, values);
            }
        }
        return this;
    }

    /**
     * Convert para to special type and keep it
     */
    public ParamsHandler keepPara(Class type, String name) {
        String[] values = request.getParameterValues(name);
        if (values != null) {
            if (values.length == 1)
                try {
                    request.setAttribute(name, TypeConverter.me().convert(type, values[0]));
                } catch (ParseException e) {

                }
            else
                request.setAttribute(name, values);
        }
        return this;
    }

    public ParamsHandler keepPara(Class type, String... names) {
        if (type == String.class)
            return keepPara(names);

        if (names != null)
            for (String name : names)
                keepPara(type, name);
        return this;
    }

//    public ParamsHandler keepModel(Class<? extends com.jfinal.plugin.activerecord.Model> modelClass, String modelName) {
//        if (StrKit.notBlank(modelName)) {
//            Object model = Injector.injectModel(modelClass, modelName, request, true);
//            request.setAttribute(modelName, model);
//        } else {
//            keepPara();
//        }
//        return this;
//    }
//
//    public Controller keepModel(Class<? extends com.jfinal.plugin.activerecord.Model> modelClass) {
//        String modelName = StrKit.firstCharToLowerCase(modelClass.getSimpleName());
//        keepModel(modelClass, modelName);
//        return this;
//    }

    public ParamsHandler keepBean(Class<?> beanClass, String beanName) {
        if (StrKit.notBlank(beanName)) {
            Object bean = Injector.injectBean(beanClass, beanName, request, true);
            request.setAttribute(beanName, bean);
        } else {
            keepPara();
        }
        return this;
    }

    public ParamsHandler keepBean(Class<?> beanClass) {
        String beanName = StrKit.firstCharToLowerCase(beanClass.getSimpleName());
        keepBean(beanClass, beanName);
        return this;
    }

//    /**
//     * Create a token.
//     * @param tokenName the token name used in view
//     * @param secondsOfTimeOut the seconds of time out, secondsOfTimeOut >= Const.MIN_SECONDS_OF_TOKEN_TIME_OUT
//     */
//    public String createToken(String tokenName, int secondsOfTimeOut) {
//        return com.jfinal.token.TokenManager.createToken(this, tokenName, secondsOfTimeOut);
//    }
//
//    /**
//     * Create a token with default token name and with default seconds of time out.
//     */
//    public String createToken() {
//        return createToken(Const.DEFAULT_TOKEN_NAME, Const.DEFAULT_SECONDS_OF_TOKEN_TIME_OUT);
//    }
//
//    /**
//     * Create a token with default seconds of time out.
//     * @param tokenName the token name used in view
//     */
//    public String createToken(String tokenName) {
//        return createToken(tokenName, Const.DEFAULT_SECONDS_OF_TOKEN_TIME_OUT);
//    }
//
//    /**
//     * Check token to prevent resubmit.
//     * @param tokenName the token name used in view's form
//     * @return true if token is correct
//     */
//    public boolean validateToken(String tokenName) {
//        return com.jfinal.token.TokenManager.validateToken(this, tokenName);
//    }
//
//    /**
//     * Check token to prevent resubmit  with default token key ---> "JFINAL_TOKEN_KEY"
//     * @return true if token is correct
//     */
//    public boolean validateToken() {
//        return validateToken(Const.DEFAULT_TOKEN_NAME);
//    }

    /**
     * Return true if the para value is blank otherwise return false
     */
    public boolean isParaBlank(String paraName) {
        return StrKit.isBlank(request.getParameter(paraName));
    }

    /**
     * Return true if the urlPara value is blank otherwise return false
     */
    public boolean isParaBlank(int index) {
        return StrKit.isBlank(getPara(index));
    }

    /**
     * Return true if the para exists otherwise return false
     */
    public boolean isParaExists(String paraName) {
        return request.getParameterMap().containsKey(paraName);
    }

    /**
     * Return true if the urlPara exists otherwise return false
     */
    public boolean isParaExists(int index) {
        return getPara(index) != null;
    }



}
