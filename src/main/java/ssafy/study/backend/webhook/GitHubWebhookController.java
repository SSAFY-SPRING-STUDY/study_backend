package ssafy.study.backend.webhook;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ssafy.study.backend.domain.edu.assignment.service.AssignmentService;
import ssafy.study.backend.domain.edu.study.entity.Study;
import ssafy.study.backend.domain.edu.study.repository.StudyRepository;
import ssafy.study.backend.global.exception.CustomException;
import ssafy.study.backend.global.exception.error.ErrorCode;
import ssafy.study.backend.global.response.ApiResponse;

@Slf4j
@RestController
@RequestMapping("/api/v1/webhook/github")
@RequiredArgsConstructor
@Tag(name = "Webhook", description = "GitHub Webhook 수신 API")
public class GitHubWebhookController {

	private static final Pattern PROGRESS_ID_PATTERN =
		Pattern.compile("<!--\\s*progress_id:\\s*(\\d+)\\s*-->");

	private final AssignmentService assignmentService;
	private final StudyRepository studyRepository;
	private final ObjectMapper objectMapper;

	@PostMapping("/{studyId}")
	@Operation(summary = "GitHub Webhook 수신", description = "GitHub PR Merge 이벤트를 수신하여 과제 완료 처리합니다.")
	@ResponseStatus(HttpStatus.OK)
	public ApiResponse<Void> handleWebhook(
		@PathVariable Long studyId,
		@RequestHeader(value = "X-GitHub-Event", required = false) String event,
		@RequestHeader(value = "X-Hub-Signature-256", required = false) String signature,
		@RequestBody String rawBody
	) {
		Study study = studyRepository.findById(studyId)
			.orElseThrow(() -> new CustomException(ErrorCode.STUDY_NOT_FOUND));

		String webhookSecret = study.getGithubWebhookSecret();
		if (webhookSecret == null || webhookSecret.isBlank()) {
			throw new CustomException(ErrorCode.GITHUB_WEBHOOK_INVALID_SIGNATURE);
		}
		verifySignature(rawBody, signature, webhookSecret);

		if (!"pull_request".equals(event)) {
			return ApiResponse.success("이벤트를 무시합니다.");
		}

		try {
			Map<String, Object> payload = objectMapper.readValue(rawBody, new TypeReference<>() {});
			String action = (String) payload.get("action");
			Map<?, ?> pr = (Map<?, ?>) payload.get("pull_request");

			if (!"closed".equals(action) || pr == null || !Boolean.TRUE.equals(pr.get("merged"))) {
				return ApiResponse.success("Merge된 PR이 아닙니다.");
			}

			String prBody = (String) pr.get("body");
			if (prBody == null) {
				return ApiResponse.success("PR 본문이 없습니다.");
			}

			Matcher matcher = PROGRESS_ID_PATTERN.matcher(prBody);
			if (!matcher.find()) {
				log.warn("PR body에서 progress_id를 찾을 수 없습니다. studyId={}", studyId);
				return ApiResponse.success("progress_id를 파싱할 수 없습니다.");
			}

			Long progressId = Long.parseLong(matcher.group(1));
			assignmentService.completeByProgressId(progressId);

		} catch (CustomException e) {
			throw e;
		} catch (Exception e) {
			log.error("Webhook 처리 중 오류 발생. studyId={}", studyId, e);
		}

		return ApiResponse.success("Webhook 처리가 완료되었습니다.");
	}

	private void verifySignature(String body, String signature, String secret) {
		if (signature == null || !signature.startsWith("sha256=")) {
			throw new CustomException(ErrorCode.GITHUB_WEBHOOK_INVALID_SIGNATURE);
		}
		try {
			Mac mac = Mac.getInstance("HmacSHA256");
			mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
			byte[] expected = mac.doFinal(body.getBytes(StandardCharsets.UTF_8));
			String expectedHex = "sha256=" + bytesToHex(expected);
			// timing-safe 비교
			if (!MessageDigest.isEqual(
				expectedHex.getBytes(StandardCharsets.UTF_8),
				signature.getBytes(StandardCharsets.UTF_8)
			)) {
				throw new CustomException(ErrorCode.GITHUB_WEBHOOK_INVALID_SIGNATURE);
			}
		} catch (CustomException e) {
			throw e;
		} catch (Exception e) {
			throw new CustomException(ErrorCode.GITHUB_WEBHOOK_INVALID_SIGNATURE);
		}
	}

	private String bytesToHex(byte[] bytes) {
		StringBuilder sb = new StringBuilder();
		for (byte b : bytes) {
			sb.append(String.format("%02x", b));
		}
		return sb.toString();
	}
}
