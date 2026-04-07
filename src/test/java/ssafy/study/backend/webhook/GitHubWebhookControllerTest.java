package ssafy.study.backend.webhook;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.*;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Optional;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import com.fasterxml.jackson.databind.ObjectMapper;

import ssafy.study.backend.domain.edu.assignment.service.AssignmentService;
import ssafy.study.backend.domain.edu.study.entity.DifficultyLevel;
import ssafy.study.backend.domain.edu.study.entity.Study;
import ssafy.study.backend.domain.edu.study.entity.StudyType;
import ssafy.study.backend.domain.edu.study.repository.StudyRepository;
import ssafy.study.backend.global.exception.CustomException;
import ssafy.study.backend.global.exception.error.ErrorCode;

@ExtendWith(MockitoExtension.class)
class GitHubWebhookControllerTest {

	@Mock
	private AssignmentService assignmentService;

	@Mock
	private StudyRepository studyRepository;

	private GitHubWebhookController controller;

	@BeforeEach
	void setUp() {
		controller = new GitHubWebhookController(assignmentService, studyRepository, new ObjectMapper());
	}

	// ===== ISSUE-2: webhookSecret 미설정 시 403 =====

	@Test
	@DisplayName("githubWebhookSecret이 null인 스터디 Webhook 요청 → GITHUB_WEBHOOK_INVALID_SIGNATURE(403)")
	void webhookSecret_null_이면_403() {
		// given
		Study study = Study.builder()
			.name("스터디").description("설명")
			.level(DifficultyLevel.BASIC).type(StudyType.BACKEND)
			.githubOrgName("org").githubRepoName("repo")
			// webhookSecret 미설정 (null)
			.build();
		ReflectionTestUtils.setField(study, "id", 1L);
		given(studyRepository.findById(1L)).willReturn(Optional.of(study));

		// when & then
		assertThatThrownBy(() ->
			controller.handleWebhook(1L, "pull_request", "sha256=invalid", "{}"))
			.isInstanceOf(CustomException.class)
			.hasFieldOrPropertyWithValue("errorCode", ErrorCode.GITHUB_WEBHOOK_INVALID_SIGNATURE);
	}

	@Test
	@DisplayName("githubWebhookSecret이 빈 문자열인 스터디 Webhook 요청 → GITHUB_WEBHOOK_INVALID_SIGNATURE(403)")
	void webhookSecret_blank_이면_403() {
		// given
		Study study = Study.builder()
			.name("스터디").description("설명")
			.level(DifficultyLevel.BASIC).type(StudyType.BACKEND)
			.githubOrgName("org").githubRepoName("repo")
			.githubWebhookSecret("   ")
			.build();
		ReflectionTestUtils.setField(study, "id", 1L);
		given(studyRepository.findById(1L)).willReturn(Optional.of(study));

		// when & then
		assertThatThrownBy(() ->
			controller.handleWebhook(1L, "pull_request", "sha256=invalid", "{}"))
			.isInstanceOf(CustomException.class)
			.hasFieldOrPropertyWithValue("errorCode", ErrorCode.GITHUB_WEBHOOK_INVALID_SIGNATURE);
	}

	@Test
	@DisplayName("잘못된 서명으로 Webhook 요청 시 → GITHUB_WEBHOOK_INVALID_SIGNATURE(403)")
	void 잘못된_서명_403() {
		// given
		Study study = Study.builder()
			.name("스터디").description("설명")
			.level(DifficultyLevel.BASIC).type(StudyType.BACKEND)
			.githubOrgName("org").githubRepoName("repo")
			.githubWebhookSecret("correct-secret")
			.build();
		ReflectionTestUtils.setField(study, "id", 1L);
		given(studyRepository.findById(1L)).willReturn(Optional.of(study));

		// when & then
		assertThatThrownBy(() ->
			controller.handleWebhook(1L, "pull_request", "sha256=wrongsignature", "{}"))
			.isInstanceOf(CustomException.class)
			.hasFieldOrPropertyWithValue("errorCode", ErrorCode.GITHUB_WEBHOOK_INVALID_SIGNATURE);
	}

	@Test
	@DisplayName("올바른 서명 + merged PR + progress_id 존재 시 completeByProgressId 호출")
	void 올바른_서명_merged_PR_처리() throws Exception {
		// given
		String secret = "correct-secret";
		String body = "{\"action\":\"closed\",\"pull_request\":{\"merged\":true,\"body\":\"<!-- progress_id: 42 -->\"}}";
		String signature = computeSignature(body, secret);

		Study study = Study.builder()
			.name("스터디").description("설명")
			.level(DifficultyLevel.BASIC).type(StudyType.BACKEND)
			.githubOrgName("org").githubRepoName("repo")
			.githubWebhookSecret(secret)
			.build();
		ReflectionTestUtils.setField(study, "id", 1L);
		given(studyRepository.findById(1L)).willReturn(Optional.of(study));

		// when
		controller.handleWebhook(1L, "pull_request", signature, body);

		// then
		then(assignmentService).should().completeByProgressId(42L);
	}

	private String computeSignature(String body, String secret) throws Exception {
		Mac mac = Mac.getInstance("HmacSHA256");
		mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
		byte[] hash = mac.doFinal(body.getBytes(StandardCharsets.UTF_8));
		StringBuilder sb = new StringBuilder("sha256=");
		for (byte b : hash) {
			sb.append(String.format("%02x", b));
		}
		return sb.toString();
	}
}
