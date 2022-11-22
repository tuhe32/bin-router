package com.binfast.adpter.core;

import org.springframework.util.StringUtils;

/**
 * @author 刘斌
 * @date 2022/11/5 8:36 下午
 */
public class ApiRoute {
    private final static String SEPARATOR = "#";
    private final static String DEFAULT_HTTP_METHOD = "*";
    private final static String DEFAULT_METHOD = "/";

    private String httpMethod = DEFAULT_HTTP_METHOD;
    private String method = DEFAULT_METHOD;

    private String routeUniqueIdentity;
    private String beanName;
    private String fullName;
    private String beanNotes;
    private String notes;

    public static ApiRoute valueOf (String beanName, String httpMethod, String method, String fullName, String beanNotes, String notes) {
        ApiRoute apiRoute = new ApiRoute();
        apiRoute.setBeanName(beanName);
        apiRoute.setHttpMethod(StringUtils.hasText(httpMethod) ? httpMethod : DEFAULT_HTTP_METHOD);
        apiRoute.setMethod(StringUtils.hasText(method) ? method : DEFAULT_METHOD);
        apiRoute.setFullName(fullName);
        apiRoute.setBeanNotes(beanNotes);
        apiRoute.setNotes(notes);
        apiRoute.setRouteUniqueIdentity(apiRoute.getUniqueIdentity());
        return apiRoute;
    }

    public static ApiRoute valueOf (String httpMethod, String method) {
        ApiRoute apiRoute = new ApiRoute();
        apiRoute.setHttpMethod(StringUtils.hasText(httpMethod) ? httpMethod : DEFAULT_HTTP_METHOD);
        apiRoute.setMethod(StringUtils.hasText(method) ? method : DEFAULT_METHOD);
        apiRoute.setRouteUniqueIdentity(apiRoute.getUniqueIdentity());
        return apiRoute;
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
        ApiRoute other = (ApiRoute) obj;
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
        return "ApiRoute [httpMethod=" + httpMethod + ", method=" + method + "]";
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

    public String getBeanNotes() {
        return beanNotes;
    }

    public void setBeanNotes(String beanNotes) {
        this.beanNotes = beanNotes;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }
}
