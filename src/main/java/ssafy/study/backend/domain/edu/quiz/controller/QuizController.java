package ssafy.study.backend.domain.edu.quiz.controller;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import ssafy.study.backend.domain.edu.quiz.controller.dto.request.QuizSubmitRequest;
import ssafy.study.backend.domain.edu.quiz.controller.dto.response.QuizAttemptSummaryResponse;
import ssafy.study.backend.domain.edu.quiz.controller.dto.response.QuizResponse;
import ssafy.study.backend.domain.edu.quiz.controller.dto.response.QuizResultResponse;
import ssafy.study.backend.domain.edu.quiz.service.QuizService;
import ssafy.study.backend.global.response.ApiResponse;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
@Tag(name = "Quiz", description = "퀴즈 관련 API")
public class QuizController {

	private final QuizService quizService;

	@PostMapping("/posts/{postId}/quiz/generate")
	@PreAuthorize("hasRole('ADMIN')")
	@ResponseStatus(HttpStatus.CREATED)
	@Operation(
		summary = "퀴즈 생성 (관리자 전용)",
		description = "게시글 내용을 기반으로 Gemini AI가 퀴즈를 생성합니다. 이미 퀴즈가 존재하면 409를 반환합니다."
	)
	public ApiResponse<QuizResponse> generateQuiz(@PathVariable Long postId) {
		return ApiResponse.success("퀴즈가 성공적으로 생성되었습니다.", quizService.generateQuiz(postId));
	}

	@GetMapping("/posts/{postId}/quiz")
	@ResponseStatus(HttpStatus.OK)
	@Operation(
		summary = "퀴즈 조회",
		description = "게시글에 연결된 퀴즈를 조회합니다. 정답은 포함되지 않습니다."
	)
	public ApiResponse<QuizResponse> getQuiz(@PathVariable Long postId) {
		return ApiResponse.success("퀴즈가 성공적으로 조회되었습니다.", quizService.getQuiz(postId));
	}

	@GetMapping("/posts/{postId}/quiz/attempts/me")
	@ResponseStatus(HttpStatus.OK)
	@Operation(
		summary = "내 최근 시도 결과 조회",
		description = "사용자의 가장 최근 퀴즈 시도 결과를 조회합니다. 시도 이력이 없으면 404를 반환합니다."
	)
	public ApiResponse<QuizResultResponse> getMyLatestAttempt(
		@PathVariable Long postId,
		@AuthenticationPrincipal Long memberId) {
		return ApiResponse.success("퀴즈 시도 이력을 조회했습니다.",
			quizService.getMyLatestAttempt(postId, memberId));
	}

	@GetMapping("/posts/{postId}/quiz/attempts")
	@PreAuthorize("hasRole('ADMIN')")
	@ResponseStatus(HttpStatus.OK)
	@Operation(
		summary = "퀴즈 응시 현황 조회 (관리자 전용)",
		description = "해당 게시글 퀴즈에 응시한 회원별 가장 최근 결과를 조회합니다."
	)
	public ApiResponse<Page<QuizAttemptSummaryResponse>> getQuizAttemptSummaries(
		@PathVariable Long postId,
		@PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
		return ApiResponse.success("퀴즈 응시 현황을 조회했습니다.",
			quizService.getQuizAttemptSummaries(postId, pageable));
	}

	@PostMapping("/posts/{postId}/quiz/submit")
	@ResponseStatus(HttpStatus.OK)
	@Operation(
		summary = "퀴즈 제출 및 채점",
		description = "퀴즈 답안을 제출하고 채점 결과를 반환합니다. 모든 문제에 대한 답안을 제출해야 합니다."
	)
	public ApiResponse<QuizResultResponse> submitQuiz(
		@PathVariable Long postId,
		@AuthenticationPrincipal Long memberId,
		@Valid @RequestBody QuizSubmitRequest request) {
		return ApiResponse.success("퀴즈 제출이 완료되었습니다.",
			quizService.submitQuiz(postId, memberId, request));
	}
}
