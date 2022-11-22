package com.binfast.adpter.doc;

import com.binfast.adpter.openapi.OpenApiBuilder;
import com.binfast.adpter.openapi.model.ApiConfig;
import com.binfast.adpter.openapi.model.SourceCodePath;
import org.springframework.util.ResourceUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.io.FileNotFoundException;

/**
 * @author 刘斌
 * @date 2022/11/4 9:03 下午
 */
@RestController
public class ApiController {

    private final ApiDocServlet apiDocServlet;

    public ApiController(ApiDocServlet apiDocServlet) {
        this.apiDocServlet = apiDocServlet;
    }


    @GetMapping("/doc/router")
    public String router() {
        apiDocServlet.scanRoutes();
        return "...wait a minute and check your resources directory";
    }

    @GetMapping("/doc/api-json/{projectName}")
    public String apiJson(@PathVariable String projectName) {
        ApiConfig apiConfig = new ApiConfig();
        String path = null;
        try {
            path = ResourceUtils.getURL("classpath:").getPath();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        path = path.substring(0, path.indexOf("target"));
        apiConfig.setProjectName(projectName);
        apiConfig.setFramework("spring-adapter");
        apiConfig.setOutPath(path + "src/main/resources");
        apiConfig.setSourceCodePaths(SourceCodePath.builder()
                .setPath(path+ "src/main/java")
                );
        OpenApiBuilder.buildOpenApi(apiConfig);
        return "You can check openapi.json in your resources directory...for "+projectName;
    }
}
