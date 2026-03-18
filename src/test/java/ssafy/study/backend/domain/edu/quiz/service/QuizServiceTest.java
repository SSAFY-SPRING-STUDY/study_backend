package ssafy.study.backend.domain.edu.quiz.service;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.*;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import tools.jackson.databind.ObjectMapper;

import ssafy.study.backend.domain.edu.curriculum.entity.Curriculum;
import ssafy.study.backend.domain.edu.post.entity.Post;
import ssafy.study.backend.domain.edu.post.repository.PostRepository;
import ssafy.study.backend.domain.edu.quiz.controller.dto.request.QuizSubmitRequest;
import ssafy.study.backend.domain.edu.quiz.controller.dto.response.QuizResponse;
import ssafy.study.backend.domain.edu.quiz.controller.dto.response.QuizResultResponse;
import ssafy.study.backend.domain.edu.quiz.entity.Quiz;
import ssafy.study.backend.domain.edu.quiz.entity.QuizAttempt;
import ssafy.study.backend.domain.edu.quiz.entity.QuizQuestion;
import ssafy.study.backend.domain.edu.quiz.repository.QuizAttemptRepository;
import ssafy.study.backend.domain.edu.quiz.repository.QuizRepository;
import ssafy.study.backend.domain.member.entity.Member;
import ssafy.study.backend.domain.member.repository.MemberRepository;
import ssafy.study.backend.fixture.CurriculumFixture;
import ssafy.study.backend.fixture.MemberFixture;
import ssafy.study.backend.fixture.PostFixture;
import ssafy.study.backend.fixture.QuizFixture;
import ssafy.study.backend.fixture.StudyFixture;
import ssafy.study.backend.global.ai.GroqClient;
import ssafy.study.backend.global.exception.CustomException;
import ssafy.study.backend.global.exception.error.ErrorCode;

@ExtendWith(MockitoExtension.class)
class QuizServiceTest {

	private QuizService quizService;

	@Mock
	private GroqClient groqClient;

	@Mock
	private PostRepository postRepository;

	@Mock
	private MemberRepository memberRepository;

	@Mock
	private QuizRepository quizRepository;

	@Mock
	private QuizAttemptRepository quizAttemptRepository;

	@BeforeEach
	void setUp() {
		quizService = new QuizService(groqClient, new ObjectMapper(), postRepository, memberRepository, quizRepository,
			quizAttemptRepository);
	}

	// ──────────────────────────────────────────────
	// generateQuiz
	// ──────────────────────────────────────────────

	@Test
	@DisplayName("퀴즈 생성 성공")
	void 퀴즈_생성_성공() {
		// given
		Post post = post(1L);
		given(postRepository.findById(1L)).willReturn(Optional.of(post));
		given(quizRepository.existsByPost(post)).willReturn(false);
		given(quizRepository.save(any(Quiz.class))).willAnswer(inv -> {
			Quiz q = inv.getArgument(0);
			ReflectionTestUtils.setField(q, "id", 1L);
			return q;
		});
		mockGroqClient(QuizFixture.validQuizJson());

		// when
		QuizResponse result = quizService.generateQuiz(1L);

		// then
		assertThat(result.postId()).isEqualTo(1L);
		assertThat(result.questions()).hasSize(10);
		assertThat(result.questions().get(0).options()).hasSize(5);
		then(quizRepository).should().save(any(Quiz.class));
	}

	@Test
	@DisplayName("퀴즈 생성 실패 - 존재하지 않는 게시글")
	void 퀴즈_생성_실패_게시글_없음() {
		// given
		given(postRepository.findById(99L)).willReturn(Optional.empty());

		// when & then
		assertThatThrownBy(() -> quizService.generateQuiz(99L))
			.isInstanceOf(CustomException.class)
			.hasFieldOrPropertyWithValue("errorCode", ErrorCode.POST_NOT_FOUND);
	}

	@Test
	@DisplayName("퀴즈 생성 실패 - 이미 퀴즈가 존재하는 게시글")
	void 퀴즈_생성_실패_이미_존재() {
		// given
		Post post = post(1L);
		given(postRepository.findById(1L)).willReturn(Optional.of(post));
		given(quizRepository.existsByPost(post)).willReturn(true);

		// when & then
		assertThatThrownBy(() -> quizService.generateQuiz(1L))
			.isInstanceOf(CustomException.class)
			.hasFieldOrPropertyWithValue("errorCode", ErrorCode.QUIZ_ALREADY_EXISTS);
		then(groqClient).shouldHaveNoInteractions();
	}

	@Test
	@DisplayName("퀴즈 생성 실패 - AI 호출 오류")
	void 퀴즈_생성_실패_AI_오류() {
		// given
		Post post = post(1L);
		given(postRepository.findById(1L)).willReturn(Optional.of(post));
		given(quizRepository.existsByPost(post)).willReturn(false);
		given(groqClient.chat(anyString(), anyString())).willThrow(new RuntimeException("Groq API error"));

		// when & then
		assertThatThrownBy(() -> quizService.generateQuiz(1L))
			.isInstanceOf(CustomException.class)
			.hasFieldOrPropertyWithValue("errorCode", ErrorCode.QUIZ_GENERATION_FAILED);
	}

	// ──────────────────────────────────────────────
	// getQuiz
	// ──────────────────────────────────────────────

	@Test
	@DisplayName("퀴즈 조회 성공")
	void 퀴즈_조회_성공() {
		// given
		Post post = post(1L);
		Quiz quiz = QuizFixture.quizWithQuestions(1L, post);
		given(postRepository.findById(1L)).willReturn(Optional.of(post));
		given(quizRepository.findWithQuestionsAndOptionsByPost(post)).willReturn(Optional.of(quiz));

		// when
		QuizResponse result = quizService.getQuiz(1L);

		// then
		assertThat(result.quizId()).isEqualTo(1L);
		assertThat(result.postId()).isEqualTo(1L);
		assertThat(result.questions()).hasSize(10);
	}

	@Test
	@DisplayName("퀴즈 조회 실패 - 존재하지 않는 게시글")
	void 퀴즈_조회_실패_게시글_없음() {
		// given
		given(postRepository.findById(99L)).willReturn(Optional.empty());

		// when & then
		assertThatThrownBy(() -> quizService.getQuiz(99L))
			.isInstanceOf(CustomException.class)
			.hasFieldOrPropertyWithValue("errorCode", ErrorCode.POST_NOT_FOUND);
	}

	@Test
	@DisplayName("퀴즈 조회 실패 - 게시글에 퀴즈 없음")
	void 퀴즈_조회_실패_퀴즈_없음() {
		// given
		Post post = post(1L);
		given(postRepository.findById(1L)).willReturn(Optional.of(post));
		given(quizRepository.findWithQuestionsAndOptionsByPost(post)).willReturn(Optional.empty());

		// when & then
		assertThatThrownBy(() -> quizService.getQuiz(1L))
			.isInstanceOf(CustomException.class)
			.hasFieldOrPropertyWithValue("errorCode", ErrorCode.QUIZ_NOT_FOUND);
	}

	// ──────────────────────────────────────────────
	// getMyLatestAttempt
	// ──────────────────────────────────────────────

	@Test
	@DisplayName("내 최근 시도 결과 조회 성공")
	void 최근_시도_조회_성공() {
		// given
		Post post = post(1L);
		Member member = MemberFixture.member(2L);
		Quiz quiz = QuizFixture.quizWithQuestions(1L, post);
		QuizAttempt attempt = QuizFixture.attemptWithAnswers(1L, quiz, member);

		given(postRepository.findById(1L)).willReturn(Optional.of(post));
		given(quizRepository.findWithQuestionsAndOptionsByPost(post)).willReturn(Optional.of(quiz));
		given(memberRepository.findById(2L)).willReturn(Optional.of(member));
		given(quizAttemptRepository.findTopByQuizAndMemberOrderByCreatedAtDesc(quiz, member))
			.willReturn(Optional.of(attempt));

		// when
		QuizResultResponse result = quizService.getMyLatestAttempt(1L, 2L);

		// then
		assertThat(result.attemptId()).isEqualTo(1L);
		assertThat(result.score()).isEqualTo(10);
		assertThat(result.passed()).isTrue();
		assertThat(result.results()).hasSize(10);
	}

	@Test
	@DisplayName("내 최근 시도 결과 조회 실패 - 시도 이력 없음")
	void 최근_시도_조회_실패_이력_없음() {
		// given
		Post post = post(1L);
		Member member = MemberFixture.member(2L);
		Quiz quiz = QuizFixture.quizWithQuestions(1L, post);

		given(postRepository.findById(1L)).willReturn(Optional.of(post));
		given(quizRepository.findWithQuestionsAndOptionsByPost(post)).willReturn(Optional.of(quiz));
		given(memberRepository.findById(2L)).willReturn(Optional.of(member));
		given(quizAttemptRepository.findTopByQuizAndMemberOrderByCreatedAtDesc(quiz, member))
			.willReturn(Optional.empty());

		// when & then
		assertThatThrownBy(() -> quizService.getMyLatestAttempt(1L, 2L))
			.isInstanceOf(CustomException.class)
			.hasFieldOrPropertyWithValue("errorCode", ErrorCode.QUIZ_ATTEMPT_NOT_FOUND);
	}

	@Test
	@DisplayName("내 최근 시도 결과 조회 실패 - 퀴즈 없음")
	void 최근_시도_조회_실패_퀴즈_없음() {
		// given
		Post post = post(1L);
		given(postRepository.findById(1L)).willReturn(Optional.of(post));
		given(quizRepository.findWithQuestionsAndOptionsByPost(post)).willReturn(Optional.empty());

		// when & then
		assertThatThrownBy(() -> quizService.getMyLatestAttempt(1L, 2L))
			.isInstanceOf(CustomException.class)
			.hasFieldOrPropertyWithValue("errorCode", ErrorCode.QUIZ_NOT_FOUND);
	}

	// ──────────────────────────────────────────────
	// submitQuiz
	// ──────────────────────────────────────────────

	@Test
	@DisplayName("퀴즈 제출 성공 - 합격 (10/10)")
	void 퀴즈_제출_성공_합격() {
		// given
		Post post = post(1L);
		Member member = MemberFixture.member(2L);
		Quiz quiz = QuizFixture.quizWithQuestions(1L, post);

		given(postRepository.findById(1L)).willReturn(Optional.of(post));
		given(quizRepository.findWithQuestionsAndOptionsByPost(post)).willReturn(Optional.of(quiz));
		given(memberRepository.findById(2L)).willReturn(Optional.of(member));
		given(quizAttemptRepository.save(any(QuizAttempt.class))).willAnswer(inv -> {
			QuizAttempt a = inv.getArgument(0);
			ReflectionTestUtils.setField(a, "id", 1L);
			return a;
		});

		QuizSubmitRequest request = allCorrectAnswers(quiz);

		// when
		QuizResultResponse result = quizService.submitQuiz(1L, 2L, request);

		// then
		assertThat(result.score()).isEqualTo(10);
		assertThat(result.totalQuestions()).isEqualTo(10);
		assertThat(result.passed()).isTrue();
		assertThat(result.results()).allMatch(r -> r.correct());
	}

	@Test
	@DisplayName("퀴즈 제출 성공 - 불합격 (6/10)")
	void 퀴즈_제출_성공_불합격() {
		// given
		Post post = post(1L);
		Member member = MemberFixture.member(2L);
		Quiz quiz = QuizFixture.quizWithQuestions(1L, post);

		given(postRepository.findById(1L)).willReturn(Optional.of(post));
		given(quizRepository.findWithQuestionsAndOptionsByPost(post)).willReturn(Optional.of(quiz));
		given(memberRepository.findById(2L)).willReturn(Optional.of(member));
		given(quizAttemptRepository.save(any(QuizAttempt.class))).willAnswer(inv -> {
			QuizAttempt a = inv.getArgument(0);
			ReflectionTestUtils.setField(a, "id", 1L);
			return a;
		});

		// 6문제 정답(첫 번째 옵션), 4문제 오답(두 번째 옵션)
		List<QuizSubmitRequest.AnswerDto> answers = quiz.getQuestions().stream()
			.map(q -> {
				boolean pickCorrect = q.getQuestionOrder() <= 6;
				Long optionId = pickCorrect
					? q.getOptions().get(0).getId()   // 정답 (첫 번째)
					: q.getOptions().get(1).getId();   // 오답 (두 번째)
				return new QuizSubmitRequest.AnswerDto(q.getId(), optionId);
			})
			.toList();

		// when
		QuizResultResponse result = quizService.submitQuiz(1L, 2L, new QuizSubmitRequest(answers));

		// then
		assertThat(result.score()).isEqualTo(6);
		assertThat(result.passed()).isFalse();
	}

	@Test
	@DisplayName("퀴즈 제출 실패 - 존재하지 않는 게시글")
	void 퀴즈_제출_실패_게시글_없음() {
		// given
		given(postRepository.findById(99L)).willReturn(Optional.empty());
		QuizSubmitRequest request = new QuizSubmitRequest(List.of());

		// when & then
		assertThatThrownBy(() -> quizService.submitQuiz(99L, 2L, request))
			.isInstanceOf(CustomException.class)
			.hasFieldOrPropertyWithValue("errorCode", ErrorCode.POST_NOT_FOUND);
	}

	@Test
	@DisplayName("퀴즈 제출 실패 - 퀴즈 없음")
	void 퀴즈_제출_실패_퀴즈_없음() {
		// given
		Post post = post(1L);
		given(postRepository.findById(1L)).willReturn(Optional.of(post));
		given(quizRepository.findWithQuestionsAndOptionsByPost(post)).willReturn(Optional.empty());

		// when & then
		assertThatThrownBy(() -> quizService.submitQuiz(1L, 2L, new QuizSubmitRequest(List.of())))
			.isInstanceOf(CustomException.class)
			.hasFieldOrPropertyWithValue("errorCode", ErrorCode.QUIZ_NOT_FOUND);
	}

	@Test
	@DisplayName("퀴즈 제출 실패 - 답안 수 부족 (일부만 제출)")
	void 퀴즈_제출_실패_답안_수_불일치() {
		// given
		Post post = post(1L);
		Member member = MemberFixture.member(2L);
		Quiz quiz = QuizFixture.quizWithQuestions(1L, post);

		given(postRepository.findById(1L)).willReturn(Optional.of(post));
		given(quizRepository.findWithQuestionsAndOptionsByPost(post)).willReturn(Optional.of(quiz));
		given(memberRepository.findById(2L)).willReturn(Optional.of(member));

		// 10문제 중 1개만 제출
		QuizQuestion firstQuestion = quiz.getQuestions().get(0);
		QuizSubmitRequest request = new QuizSubmitRequest(List.of(
			new QuizSubmitRequest.AnswerDto(firstQuestion.getId(), firstQuestion.getOptions().get(0).getId())
		));

		// when & then
		assertThatThrownBy(() -> quizService.submitQuiz(1L, 2L, request))
			.isInstanceOf(CustomException.class)
			.hasFieldOrPropertyWithValue("errorCode", ErrorCode.QUIZ_INVALID_SUBMISSION);
	}

	@Test
	@DisplayName("퀴즈 제출 실패 - 해당 문제에 속하지 않는 보기 선택")
	void 퀴즈_제출_실패_잘못된_옵션() {
		// given
		Post post = post(1L);
		Member member = MemberFixture.member(2L);
		Quiz quiz = QuizFixture.quizWithQuestions(1L, post);

		given(postRepository.findById(1L)).willReturn(Optional.of(post));
		given(quizRepository.findWithQuestionsAndOptionsByPost(post)).willReturn(Optional.of(quiz));
		given(memberRepository.findById(2L)).willReturn(Optional.of(member));

		// 1번 문제에 2번 문제의 보기를 제출
		List<QuizSubmitRequest.AnswerDto> answers = quiz.getQuestions().stream()
			.map(q -> {
				// 모든 문제에 1번 문제의 첫 번째 옵션 ID를 제출 → 2~10번 문제는 잘못된 optionId
				Long wrongOptionId = quiz.getQuestions().get(0).getOptions().get(0).getId();
				return new QuizSubmitRequest.AnswerDto(q.getId(), wrongOptionId);
			})
			.toList();

		// when & then
		assertThatThrownBy(() -> quizService.submitQuiz(1L, 2L, new QuizSubmitRequest(answers)))
			.isInstanceOf(CustomException.class)
			.hasFieldOrPropertyWithValue("errorCode", ErrorCode.QUIZ_INVALID_SUBMISSION);
	}

	// ──────────────────────────────────────────────
	// helpers
	// ──────────────────────────────────────────────

	private Post post(Long id) {
		Curriculum curriculum = CurriculumFixture.curriculum(1L, StudyFixture.study(1L));
		return PostFixture.post(id, MemberFixture.member(1L), curriculum);
	}

	private void mockGroqClient(String responseJson) {
		given(groqClient.chat(anyString(), anyString())).willReturn(responseJson);
	}

	private QuizSubmitRequest allCorrectAnswers(Quiz quiz) {
		List<QuizSubmitRequest.AnswerDto> answers = quiz.getQuestions().stream()
			.map(q -> new QuizSubmitRequest.AnswerDto(
				q.getId(),
				q.getOptions().get(0).getId() // 첫 번째 보기가 정답
			))
			.toList();
		return new QuizSubmitRequest(answers);
	}
}
