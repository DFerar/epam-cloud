package com.task05;

import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.amazonaws.services.dynamodbv2.model.PutItemRequest;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPResponse;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.syndicate.deployment.annotations.environment.EnvironmentVariable;
import com.syndicate.deployment.annotations.environment.EnvironmentVariables;
import com.syndicate.deployment.annotations.lambda.LambdaHandler;
import com.syndicate.deployment.model.RetentionSetting;

import java.awt.event.ItemEvent;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@LambdaHandler(
    lambdaName = "api_handler",
	roleName = "api_handler-role",
	isPublishVersion = false,
	logsExpiration = RetentionSetting.SYNDICATE_ALIASES_SPECIFIED
)
@EnvironmentVariables(value = {
		@EnvironmentVariable(key = "target_table", value = "Events")
})
public class ApiHandler implements RequestHandler<APIGatewayV2HTTPEvent, APIGatewayV2HTTPResponse> {
	private final ObjectMapper objectMapper = new ObjectMapper();
	private static final String DYNAMODB_TABLE_NAME = System.getenv("target_table");

	public APIGatewayV2HTTPResponse handleRequest(APIGatewayV2HTTPEvent request, Context context) {

        Map<String, Object> input;
        try {
            input = objectMapper.readValue(request.getBody(), Map.class);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
        UUID id = UUID.randomUUID();
        String createdAt = LocalDateTime.now().toString();

        Map<String, String> headers = new HashMap<>();
        headers.put("Content-Type", "application/json");

        EventOutput output = new EventOutput(id.toString(), (Integer) input.get("principalId"), createdAt,
                input);

        PutItemRequest putItemRequest = new PutItemRequest()
                .withTableName(DYNAMODB_TABLE_NAME)
                .withItem(Map.of(
                        "id", new AttributeValue().withS(id.toString()),
                        "principalId", new AttributeValue().withN(input.get("principalId").toString()),
                        "createdAt", new AttributeValue().withS(createdAt),
                        "content", new AttributeValue().withS(input.toString())
                ));

        try {
            return APIGatewayV2HTTPResponse.builder()
                    .withStatusCode(200)
                    .withHeaders(headers)
                    .withBody(objectMapper.writeValueAsString(output.getBody()))
                    .build();
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }
}
