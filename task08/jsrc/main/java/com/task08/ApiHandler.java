package com.task08;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.models.dynamodb.AttributeValue;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.syndicate.deployment.annotations.environment.EnvironmentVariable;
import com.syndicate.deployment.annotations.environment.EnvironmentVariables;
import com.syndicate.deployment.annotations.lambda.LambdaHandler;
import com.syndicate.deployment.annotations.lambda.LambdaLayer;
import com.syndicate.deployment.annotations.lambda.LambdaUrlConfig;
import com.syndicate.deployment.model.Architecture;
import com.syndicate.deployment.model.ArtifactExtension;
import com.syndicate.deployment.model.DeploymentRuntime;
import com.syndicate.deployment.model.RetentionSetting;
import com.syndicate.deployment.model.lambda.url.AuthType;
import com.syndicate.deployment.model.lambda.url.InvokeMode;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@LambdaHandler(
    lambdaName = "api_handler",
	roleName = "api_handler-role",
	isPublishVersion = true,
	aliasName = "${lambdas_alias_name}",
	logsExpiration = RetentionSetting.SYNDICATE_ALIASES_SPECIFIED
)
@LambdaLayer(
		layerName = "open-meteo-api-layer",
		libraries = {"lib/httpclient-4.5.13.jar", "lib/httpcore-4.4.13.jar"},
		runtime = DeploymentRuntime.JAVA17,
		architectures = {Architecture.ARM64},
		artifactExtension = ArtifactExtension.ZIP
)
@LambdaUrlConfig(
		authType = AuthType.NONE,
		invokeMode = InvokeMode.BUFFERED
)
public class ApiHandler implements RequestHandler<Void, String> {

	private final ObjectMapper objectMapper = new ObjectMapper();

	private static final String URL = "https://api.open-meteo.com/v1/forecast?latitude=52.52&longitude=13.41&current=temperature_2m,wind_speed_10m&hourly=temperature_2m,relative_humidity_2m,wind_speed_10m";


	@Override
	public String handleRequest(Void event, Context context) {
		var response = HttpClient.executeGet(URL);
		try {
			var weatherResponse = objectMapper.readValue(response, WeatherResponse.class);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		return response;
	}

	private Map<String, AttributeValue> buildBodyValue(WeatherResponse weatherResponse) {
		var attributesMapForecast = new HashMap<String, AttributeValue>();
		attributesMapForecast.put("elevation", new AttributeValue().withN(String.valueOf(weatherResponse.elevation())));
		attributesMapForecast.put("generationtime_ms", new AttributeValue().withN(String.valueOf(weatherResponse.generationtime_ms())));

		var attributesMapHourly = new HashMap<String, AttributeValue>();
		attributesMapHourly.put("temperature_2m", new AttributeValue().withL(
				weatherResponse.hourly().temperature_2m().stream().map(value -> new AttributeValue().withN(String.valueOf(value))).toList())
		);
		attributesMapHourly.put("time", new AttributeValue().withL(
				weatherResponse.hourly().time().stream().map(AttributeValue::new).toList())
		);
		attributesMapForecast.put("hourly", new AttributeValue().withM(attributesMapHourly));
		var attributesMapHourlyUnits = new HashMap<String, AttributeValue>();
		attributesMapHourlyUnits.put("temperature_2m", new AttributeValue(weatherResponse.hourly_units().temperature_2m()));
		attributesMapHourlyUnits.put("time", new AttributeValue(weatherResponse.hourly_units().time()));
		attributesMapForecast.put("hourly_units", new AttributeValue().withM(attributesMapHourlyUnits));

		attributesMapForecast.put("latitude", new AttributeValue().withN(String.valueOf(weatherResponse.latitude())));
		attributesMapForecast.put("longitude", new AttributeValue().withN(String.valueOf(weatherResponse.longitude())));
		attributesMapForecast.put("timezone", new AttributeValue(String.valueOf(weatherResponse.timezone())));
		attributesMapForecast.put("timezone_abbreviation",new AttributeValue(String.valueOf(weatherResponse.timezone_abbreviation())));
		attributesMapForecast.put("utc_offset_seconds",new AttributeValue().withN(String.valueOf(weatherResponse.utc_offset_seconds())));
		return attributesMapForecast;
	}

	public record Hourly(List<Number> temperature_2m, List<String> time, List<Number> relative_humidity_2m, List<Number> wind_speed_10m) {

	}

	public record HourlyUnits(String temperature_2m, String time, String relative_humidity_2m, String wind_speed_10m) {

	}

	public record CurrentUnits(Number temperature, String time, String interval, String temperature_2m, String wind_speed_10m) {

	}

	public record Current(Number temperature, String time, String interval, String temperature_2m, String wind_speed_10m) {

	}

	public record WeatherResponse(Number latitude, Number longitude, Number utc_offset_seconds, Number elevation, Number generationtime_ms,
								  CurrentUnits current_units, Current current,
								  Hourly hourly, HourlyUnits hourly_units,
								  String timezone, String timezone_abbreviation) {
	}
}
