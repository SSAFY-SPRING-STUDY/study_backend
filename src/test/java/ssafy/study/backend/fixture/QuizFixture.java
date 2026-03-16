package ssafy.study.backend.fixture;

import org.springframework.test.util.ReflectionTestUtils;

import ssafy.study.backend.domain.edu.post.entity.Post;
import ssafy.study.backend.domain.edu.quiz.entity.Quiz;
import ssafy.study.backend.domain.edu.quiz.entity.QuizAttempt;
import ssafy.study.backend.domain.edu.quiz.entity.QuizAttemptAnswer;
import ssafy.study.backend.domain.edu.quiz.entity.QuizOption;
import ssafy.study.backend.domain.edu.quiz.entity.QuizQuestion;
import ssafy.study.backend.domain.member.entity.Member;

public class QuizFixture {

	/** 문제/보기 없는 빈 Quiz */
	public static Quiz quiz(Long id, Post post) {
		Quiz quiz = Quiz.builder().post(post).build();
		ReflectionTestUtils.setField(quiz, "id", id);
		return quiz;
	}

	/** 10문제 × 5지선다 완성된 Quiz. 각 문제의 첫 번째 보기가 정답 */
	public static Quiz quizWithQuestions(Long id, Post post) {
		Quiz quiz = quiz(id, post);
		for (int qi = 1; qi <= 10; qi++) {
			QuizQuestion question = quizQuestion((long) qi, quiz, qi);
			quiz.addQuestion(question);
			for (int oi = 1; oi <= 5; oi++) {
				QuizOption option = quizOption((long) ((qi - 1) * 5 + oi), question, oi, oi == 1);
				question.addOption(option);
			}
		}
		return quiz;
	}

	public static QuizQuestion quizQuestion(Long id, Quiz quiz, int order) {
		QuizQuestion question = QuizQuestion.builder()
			.quiz(quiz)
			.question("테스트 질문 " + order)
			.questionOrder(order)
			.build();
		ReflectionTestUtils.setField(question, "id", id);
		return question;
	}

	public static QuizOption quizOption(Long id, QuizQuestion question, int order, boolean isCorrect) {
		QuizOption option = QuizOption.builder()
			.quizQuestion(question)
			.content("보기 " + order)
			.isCorrect(isCorrect)
			.optionOrder(order)
			.build();
		ReflectionTestUtils.setField(option, "id", id);
		return option;
	}

	public static QuizAttempt quizAttempt(Long id, Quiz quiz, Member member, int score) {
		QuizAttempt attempt = QuizAttempt.builder()
			.quiz(quiz)
			.member(member)
			.score(score)
			.passed(score >= 7)
			.build();
		ReflectionTestUtils.setField(attempt, "id", id);
		return attempt;
	}

	public static QuizAttemptAnswer quizAttemptAnswer(Long id, QuizAttempt attempt,
		QuizQuestion question, QuizOption option, boolean isCorrect) {
		QuizAttemptAnswer answer = QuizAttemptAnswer.builder()
			.quizAttempt(attempt)
			.quizQuestion(question)
			.quizOption(option)
			.isCorrect(isCorrect)
			.build();
		ReflectionTestUtils.setField(answer, "id", id);
		return answer;
	}

	/** 10문제 모두 정답인 QuizAttempt (합격) */
	public static QuizAttempt attemptWithAnswers(Long id, Quiz quiz, Member member) {
		QuizAttempt attempt = quizAttempt(id, quiz, member, 10);
		for (QuizQuestion question : quiz.getQuestions()) {
			QuizOption correctOption = question.getOptions().stream()
				.filter(QuizOption::isCorrect)
				.findFirst()
				.orElseThrow();
			QuizAttemptAnswer answer = quizAttemptAnswer(
				(long) question.getQuestionOrder(), attempt, question, correctOption, true
			);
			attempt.addAnswer(answer);
		}
		return attempt;
	}

	/** Gemini 응답으로 올 수 있는 유효한 JSON (10문제 × 5지선다, 각 첫 번째 보기가 정답) */
	public static String validQuizJson() {
		StringBuilder sb = new StringBuilder("{\"questions\":[");
		for (int i = 1; i <= 10; i++) {
			sb.append("{\"question\":\"테스트 질문 ").append(i).append("\",\"options\":[");
			for (int j = 1; j <= 5; j++) {
				sb.append("{\"content\":\"보기 ").append(j)
					.append("\",\"isCorrect\":").append(j == 1 ? "true" : "false").append("}");
				if (j < 5) sb.append(",");
			}
			sb.append("]}");
			if (i < 10) sb.append(",");
		}
		sb.append("]}");
		return sb.toString();
	}
}
