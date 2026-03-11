package ssafy.study.backend.global.aws.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import jakarta.validation.constraints.NotBlank;

@ConfigurationProperties(prefix = "custom.aws")
public record AwsProperties(
	@NotBlank String bucket,
	@NotBlank String region
) {
}
