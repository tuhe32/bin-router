package com.binfast.autoconfigure;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * @author liubin
 */
@ConfigurationProperties(prefix = AdapterApiProperties.API_PREFIX)
public class AdapterApiProperties {
    public static final String API_PREFIX = "adapter-api";

    /**
     * user api
     */
    private String apiPrefix = "/api";

    /**
     * doc 默认访问地址
     */
    private String docUrlPrefix = "/api/doc";

    public String getApiPrefix() {
        return apiPrefix;
    }

    public void setApiPrefix(String apiPrefix) {
        this.apiPrefix = apiPrefix;
    }

    public String getDocUrlPrefix() {
        return docUrlPrefix;
    }

    public void setDocUrlPrefix(String docUrlPrefix) {
        this.docUrlPrefix = docUrlPrefix;
    }
}
