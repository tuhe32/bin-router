package com.binfast.adpter.openapi.constants;

/**
 * @author 刘斌
 * @date 2022/11/18 3:34 下午
 */
public interface AdapterAnnotations {

    String API_MAPPING = "ApiMapping";

    String API_MAPPING_FULLY = "com.binfast.adpter.core.annotations.ApiMapping";

    String GET_API_MAPPING = "GetApiMapping";

    String POST_API_MAPPING = "PostApiMapping";

    String PUT_API_MAPPING = "PutApiMapping";

    String PATCH_API_MAPPING = "PatchApiMapping";

    String DELETE_API_MAPPING = "DeleteApiMapping";

    String SERVICE = "org.springframework.stereotype.Service";

    String REQUEST_HERDER = "RequestHeader";

    String REQUEST_PARAM = "RequestParam";

    String REQUEST_BODY = "RequestBody";

    String CONTROLLER = "Controller";

    String REST_CONTROLLER = "RestController";

    String PATH_VARIABLE = "PathVariable";

    String SESSION_ATTRIBUTE = "SessionAttribute";

    String REQUEST_ATTRIBUTE = "RequestAttribute";

    String REQUEST_BODY_FULLY = "org.springframework.web.bind.annotation.RequestBody";
}
