package com.task04;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.SQSEvent;
import com.syndicate.deployment.annotations.events.SnsEvents;
import com.syndicate.deployment.annotations.events.SqsEvents;
import com.syndicate.deployment.annotations.events.SqsTriggerEventSource;
import com.syndicate.deployment.annotations.lambda.LambdaHandler;
import com.syndicate.deployment.model.RetentionSetting;

import java.util.HashMap;
import java.util.Map;

@LambdaHandler(
    lambdaName = "sqs_handler",
	roleName = "sqs_handler-role",
	isPublishVersion = false,
	logsExpiration = RetentionSetting.SYNDICATE_ALIASES_SPECIFIED
)
@SqsTriggerEventSource(
	targetQueue = "async_queue",
	batchSize = 10)
public class SqsHandler implements RequestHandler<SQSEvent, Void> {
	@Override
	public Void handleRequest(SQSEvent event, Context context) {
		event.getRecords().forEach(record -> {
			String message = record.getBody();
			context.getLogger().log("SQS Message: " + message);
		});
		return null;
	}
}
