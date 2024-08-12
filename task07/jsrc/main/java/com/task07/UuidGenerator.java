package com.task07;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.CloudWatchLogsEvent;
import com.amazonaws.services.lambda.runtime.events.ScheduledEvent;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.syndicate.deployment.annotations.EventSource;
import com.syndicate.deployment.annotations.environment.EnvironmentVariable;
import com.syndicate.deployment.annotations.environment.EnvironmentVariables;
import com.syndicate.deployment.annotations.events.RuleEventSource;
import com.syndicate.deployment.annotations.lambda.LambdaHandler;
import com.syndicate.deployment.model.EventSourceType;
import com.syndicate.deployment.model.RetentionSetting;

import java.time.Instant;
import java.util.*;

@LambdaHandler(
        lambdaName = "uuid_generator",
        roleName = "uuid_generator-role",
        isPublishVersion = false,
        logsExpiration = RetentionSetting.SYNDICATE_ALIASES_SPECIFIED
)
@EnvironmentVariables(value = {
        @EnvironmentVariable(key = "target_bucket", value = "${target_bucket}")
})
@EventSource(eventType = EventSourceType.CLOUDWATCH_RULE_TRIGGER)
@RuleEventSource(
        targetRule = "uuid_trigger"
)
public class UuidGenerator implements RequestHandler<ScheduledEvent, String> {
    private final AmazonS3 s3Client = AmazonS3ClientBuilder.defaultClient();
    private final ObjectMapper objectMapper = new ObjectMapper();

    public String handleRequest(ScheduledEvent event, Context context) {

        List<String> uuids = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            uuids.add(UUID.randomUUID().toString());
        }

        Map<String, List<String>> data = new HashMap<>();
        data.put("ids", uuids);

        try {
            String jsonContent = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(data);

            String fileName = Instant.now().toString() + ".json";

            String bucketName = System.getenv("target_bucket");

            s3Client.putObject(bucketName, fileName, jsonContent);

            return "File " + fileName + " created successfully in bucket " + bucketName;
        } catch (Exception e) {
            context.getLogger().log("Error while generating UUIDs or uploading to S3: " + e.getMessage());
            return "Error while generating UUIDs or uploading to S3";
        }
    }
}
