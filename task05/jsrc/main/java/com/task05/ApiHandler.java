package com.task05;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.document.DynamoDB;
import com.amazonaws.services.dynamodbv2.document.Item;
import com.amazonaws.services.dynamodbv2.document.Table;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.syndicate.deployment.annotations.environment.EnvironmentVariable;
import com.syndicate.deployment.annotations.environment.EnvironmentVariables;
import com.syndicate.deployment.annotations.lambda.LambdaHandler;
import com.syndicate.deployment.model.RetentionSetting;

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
        @EnvironmentVariable(key = "target_table", value = "${target_table}")
})
public class ApiHandler implements RequestHandler<Map<String, Object>, Map<String, Object>> {
    private final AmazonDynamoDB client = AmazonDynamoDBClientBuilder.standard().build();
    private final DynamoDB dynamoDb = new DynamoDB(client);
    private final String DYNAMODB_TABLE_NAME = System.getenv("target_table");
    private final Table table = dynamoDb.getTable(DYNAMODB_TABLE_NAME);;

    public Map<String, Object> handleRequest(Map<String, Object> input, Context context) {
        String id = UUID.randomUUID().toString();
        int principalId = (int) input.get("principalId");
        String createdAt = LocalDateTime.now().toString();
        Map<String, String> content = (Map<String, String>) input.get("content");

        Item item = new Item()
                .withPrimaryKey("id", id)
                .withInt("principalId", principalId)
                .withString("createdAt", createdAt)
                .withMap("body", content);
        table.putItem(item);

        Map<String, Object> response = new HashMap<>();
        response.put("statusCode", 201);
        response.put("event", item);

        return response;
    }
}
