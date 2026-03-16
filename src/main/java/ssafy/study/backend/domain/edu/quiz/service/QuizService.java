package ssafy.study.backend.domain.edu.quiz.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import org.springframework.stereotype.Service;

import tools.jackson.databind.ObjectMapper;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ssafy.study.backend.global.ai.GroqClient;
import ssafy.study.backend.domain.edu.post.entity.Post;
import ssafy.study.backend.domain.edu.post.repository.PostRepository;
import ssafy.study.backend.domain.edu.quiz.controller.dto.request.QuizSubmitRequest;
import ssafy.study.backend.domain.edu.quiz.controller.dto.response.QuizAttemptSummaryResponse;
import ssafy.study.backend.domain.edu.quiz.controller.dto.response.QuizResponse;
import ssafy.study.backend.domain.edu.quiz.controller.dto.response.QuizResultResponse;
import ssafy.study.backend.domain.edu.quiz.entity.Quiz;
import ssafy.study.backend.domain.edu.quiz.entity.QuizAttempt;
import ssafy.study.backend.domain.edu.quiz.entity.QuizAttemptAnswer;
import ssafy.study.backend.domain.edu.quiz.entity.QuizOption;
import ssafy.study.backend.domain.edu.quiz.entity.QuizQuestion;
import ssafy.study.backend.domain.edu.quiz.repository.QuizAttemptRepository;
import ssafy.study.backend.domain.edu.quiz.repository.QuizRepository;
import ssafy.study.backend.domain.edu.quiz.service.dto.QuizGenerationResult;
import ssafy.study.backend.domain.member.entity.Member;
import ssafy.study.backend.domain.member.repository.MemberRepository;
import ssafy.study.backend.global.exception.CustomException;
import ssafy.study.backend.global.exception.error.ErrorCode;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class QuizService {

	private static final int PASS_SCORE = 7;
	private static final int MAX_CONTENT_LENGTH = 8000; // 약 2,000 토큰

	private final GroqClient groqClient;
	private final ObjectMapper objectMapper;
	private final PostRepository postRepository;
	private final MemberRepository memberRepository;
	private final QuizRepository quizRepository;
	private final QuizAttemptRepository quizAttemptRepository;

	@Transactional
	public QuizResponse generateQuiz(Long postId) {
		Post post = postRepository.findById(postId)
			.orElseThrow(() -> new CustomException(ErrorCode.POST_NOT_FOUND));

		if (quizRepository.existsByPost(post)) {
			throw new CustomException(ErrorCode.QUIZ_ALREADY_EXISTS);
		}

		QuizGenerationResult result = callGroq(post);

		Quiz quiz = Quiz.builder().post(post).build();
		quizRepository.save(quiz);

		List<QuizGenerationResult.QuestionDto> questionDtos = result.questions();
		for (int qi = 0; qi < questionDtos.size(); qi++) {
			QuizGenerationResult.QuestionDto qDto = questionDtos.get(qi);

			QuizQuestion question = QuizQuestion.builder()
				.quiz(quiz)
				.question(qDto.question())
				.questionOrder(qi + 1)
				.build();
			quiz.addQuestion(question);

			List<QuizGenerationResult.OptionDto> optionDtos = new ArrayList<>(qDto.options());
			Collections.shuffle(optionDtos);
			for (int oi = 0; oi < optionDtos.size(); oi++) {
				QuizGenerationResult.OptionDto oDto = optionDtos.get(oi);
				QuizOption option = QuizOption.builder()
					.quizQuestion(question)
					.content(oDto.content())
					.isCorrect(oDto.isCorrect())
					.optionOrder(oi + 1)
					.build();
				question.addOption(option);
			}
		}

		quizRepository.flush(); // questions/options ID 할당을 위해 flush

		return QuizResponse.from(quiz);
	}

	public QuizResponse getQuiz(Long postId) {
		Post post = postRepository.findById(postId)
			.orElseThrow(() -> new CustomException(ErrorCode.POST_NOT_FOUND));

		Quiz quiz = quizRepository.findWithQuestionsAndOptionsByPost(post)
			.orElseThrow(() -> new CustomException(ErrorCode.QUIZ_NOT_FOUND));

		return QuizResponse.from(quiz);
	}

	public QuizResultResponse getMyLatestAttempt(Long postId, Long memberId) {
		Post post = postRepository.findById(postId)
			.orElseThrow(() -> new CustomException(ErrorCode.POST_NOT_FOUND));

		Quiz quiz = quizRepository.findWithQuestionsAndOptionsByPost(post)
			.orElseThrow(() -> new CustomException(ErrorCode.QUIZ_NOT_FOUND));

		Member member = memberRepository.findById(memberId)
			.orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));

		QuizAttempt attempt = quizAttemptRepository.findTopByQuizAndMemberOrderByCreatedAtDesc(quiz, member)
			.orElseThrow(() -> new CustomException(ErrorCode.QUIZ_ATTEMPT_NOT_FOUND));

		return QuizResultResponse.from(attempt);
	}

	public Page<QuizAttemptSummaryResponse> getQuizAttemptSummaries(Long postId, Pageable pageable) {
		Post post = postRepository.findById(postId)
			.orElseThrow(() -> new CustomException(ErrorCode.POST_NOT_FOUND));

		Quiz quiz = quizRepository.findByPost(post)
			.orElseThrow(() -> new CustomException(ErrorCode.QUIZ_NOT_FOUND));

		return quizAttemptRepository.findLatestAttemptPerMemberByQuiz(quiz, pageable)
			.map(QuizAttemptSummaryResponse::from);
	}

	@Transactional
	public QuizResultResponse submitQuiz(Long postId, Long memberId, QuizSubmitRequest request) {
		Post post = postRepository.findById(postId)
			.orElseThrow(() -> new CustomException(ErrorCode.POST_NOT_FOUND));

		Quiz quiz = quizRepository.findWithQuestionsAndOptionsByPost(post)
			.orElseThrow(() -> new CustomException(ErrorCode.QUIZ_NOT_FOUND));

		Member member = memberRepository.findById(memberId)
			.orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));

		validateSubmission(quiz, request);

		// questionId → QuizQuestion 맵
		Map<Long, QuizQuestion> questionMap = quiz.getQuestions().stream()
			.collect(Collectors.toMap(QuizQuestion::getId, q -> q));

		int score = 0;
		QuizAttempt attempt = QuizAttempt.builder()
			.quiz(quiz)
			.member(member)
			.score(0)
			.passed(false)
			.build();
		quizAttemptRepository.save(attempt);

		for (QuizSubmitRequest.AnswerDto answerDto : request.answers()) {
			QuizQuestion question = questionMap.get(answerDto.questionId());

			// optionId가 해당 문제에 속하는지 검증
			QuizOption selectedOption = question.getOptions().stream()
				.filter(o -> o.getId().equals(answerDto.selectedOptionId()))
				.findFirst()
				.orElseThrow(() -> new CustomException(ErrorCode.QUIZ_INVALID_SUBMISSION));

			boolean correct = selectedOption.isCorrect();
			if (correct) score++;

			QuizAttemptAnswer answer = QuizAttemptAnswer.builder()
				.quizAttempt(attempt)
				.quizQuestion(question)
				.quizOption(selectedOption)
				.isCorrect(correct)
				.build();
			attempt.addAnswer(answer);
		}

		// score 및 passed 업데이트
		updateAttemptResult(attempt, score);

		return QuizResultResponse.from(attempt);
	}

	private void validateSubmission(Quiz quiz, QuizSubmitRequest request) {
		int questionCount = quiz.getQuestions().size();
		int answerCount = request.answers().size();

		if (questionCount != answerCount) {
			throw new CustomException(ErrorCode.QUIZ_INVALID_SUBMISSION);
		}

		Set<Long> submittedQuestionIds = request.answers().stream()
			.map(QuizSubmitRequest.AnswerDto::questionId)
			.collect(Collectors.toSet());

		Set<Long> validQuestionIds = quiz.getQuestions().stream()
			.map(QuizQuestion::getId)
			.collect(Collectors.toSet());

		if (!submittedQuestionIds.equals(validQuestionIds)) {
			throw new CustomException(ErrorCode.QUIZ_INVALID_SUBMISSION);
		}
	}

	private void updateAttemptResult(QuizAttempt attempt, int score) {
		// JPA dirty checking으로 업데이트되지 않으므로 reflection 없이 별도 메서드 활용
		// attempt는 현재 트랜잭션 내에서 managed 상태이므로 setter 대신 엔티티 메서드 사용
		attempt.updateResult(score, score >= PASS_SCORE);
	}

	private static final String SYSTEM_PROMPT = """
		당신은 학습 플랫폼의 퀴즈 생성 전문가입니다.
		주어진 학습 자료를 바탕으로 정확하고 교육적인 퀴즈를 생성합니다.
		반드시 지정된 JSON 형식으로만 응답하세요.
		""";

	private QuizGenerationResult callGroq(Post post) {
		String userPrompt = buildPrompt(post);
		try {
			String rawJson = groqClient.chat(SYSTEM_PROMPT, userPrompt);
			return objectMapper.readValue(rawJson, QuizGenerationResult.class);
		} catch (Exception e) {
			log.error("Groq quiz generation failed for postId={}", post.getId(), e);
			throw new CustomException(ErrorCode.QUIZ_GENERATION_FAILED);
		}
	}

	private String buildPrompt(Post post) {
		String content = post.getContent();
		if (content.length() > MAX_CONTENT_LENGTH) {
			content = content.substring(0, MAX_CONTENT_LENGTH) + "\n...(이하 생략)";
		}
		return """
			다음 학습 게시글을 기반으로 학습 퀴즈를 생성해주세요.

			[게시글 제목]
			%s

			[게시글 내용]
			%s

			[요구사항]
			- 총 10개의 문제를 생성하세요.
			- 각 문제는 정확히 5개의 보기를 가져야 합니다.
			- 각 문제당 isCorrect가 true인 보기는 반드시 1개여야 합니다.
			- 문제는 게시글 내용을 충분히 이해해야 풀 수 있어야 합니다.
			- 문제와 보기는 게시글의 언어(한국어 또는 영어)를 따르세요.
			- 오답 보기도 그럴듯하게 작성하세요.

			[출력 JSON 구조]
			{
			  "questions": [
			    {
			      "question": "문제 텍스트",
			      "options": [
			        { "content": "보기 내용", "isCorrect": false },
			        { "content": "보기 내용", "isCorrect": true },
			        { "content": "보기 내용", "isCorrect": false },
			        { "content": "보기 내용", "isCorrect": false },
			        { "content": "보기 내용", "isCorrect": false }
			      ]
			    }
			  ]
			}
			""".formatted(post.getTitle(), content);
	}
}
