package com.task02;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.syndicate.deployment.annotations.lambda.LambdaHandler;
import com.syndicate.deployment.annotations.lambda.LambdaUrlConfig;
import com.syndicate.deployment.model.lambda.url.AuthType;
import com.syndicate.deployment.model.lambda.url.InvokeMode;
import com.syndicate.deployment.model.RetentionSetting;

import java.util.HashMap;
import java.util.Map;

@LambdaHandler(
        lambdaName = "hello_world",
        roleName = "hello_world-role",
        isPublishVersion = false,
        logsExpiration = RetentionSetting.SYNDICATE_ALIASES_SPECIFIED
)
@LambdaUrlConfig(
        authType = AuthType.NONE,
        invokeMode = InvokeMode.BUFFERED
)
public class HelloWorld implements RequestHandler<Map<String, Object>, Map<String, Object>> {

    @Override
    public Map<String, Object> handleRequest(Map<String, Object> request, Context context) {
        System.out.println("Hello from lambda");

        String path = (String) request.get("path");
        String method = (String) request.get("httpMethod");

        Map<String, Object> resultMap = new HashMap<>();

        if ("/hello".equals(path) && "GET".equalsIgnoreCase(method)) {
            resultMap.put("statusCode", 200);
            resultMap.put("message", "{\"statusCode\": 200, \"message\": \"Hello from Lambda\"}");
        } else {
            resultMap.put("statusCode", 400);
            resultMap.put("message", String.format("{\"statusCode\": 400, \"message\": \"Bad request syntax or unsupported method. Request path: %s. HTTP method: %s\"}", path, method));
        }
        return resultMap;
    }
}
