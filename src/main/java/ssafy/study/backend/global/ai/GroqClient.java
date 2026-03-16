package ssafy.study.backend.global.ai;

import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class GroqClient {

	private static final String BASE_URL = "https://api.groq.com/openai/v1";

	private final RestClient restClient;
	private final String model;

	public GroqClient(
		@Value("${groq.api-key}") String apiKey,
		@Value("${groq.model}") String model
	) {
		this.model = model;
		this.restClient = RestClient.builder()
			.baseUrl(BASE_URL)
			.defaultHeader("Authorization", "Bearer " + apiKey)
			.build();
	}

	public String chat(String systemContent, String userContent) {
		ChatRequest request = new ChatRequest(
			model,
			List.of(
				new Message("system", systemContent),
				new Message("user", userContent)
			),
			0.3,
			new ResponseFormat("json_object")
		);

		ChatResponse response = restClient.post()
			.uri("/chat/completions")
			.contentType(MediaType.APPLICATION_JSON)
			.body(request)
			.retrieve()
			.body(ChatResponse.class);

		return response.choices().get(0).message().content();
	}

	private record ChatRequest(
		String model,
		List<Message> messages,
		double temperature,
		@JsonProperty("response_format") ResponseFormat responseFormat
	) {}

	private record Message(String role, String content) {}

	private record ResponseFormat(String type) {}

	private record ChatResponse(List<Choice> choices) {}

	private record Choice(Message message) {}
}
