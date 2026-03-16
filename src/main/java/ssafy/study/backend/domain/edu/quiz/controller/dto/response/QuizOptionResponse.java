package ssafy.study.backend.domain.edu.quiz.controller.dto.response;

import ssafy.study.backend.domain.edu.quiz.entity.QuizOption;

public record QuizOptionResponse(
	Long optionId,
	int optionOrder,
	String content
) {
	public static QuizOptionResponse from(QuizOption option) {
		return new QuizOptionResponse(
			option.getId(),
			option.getOptionOrder(),
			option.getContent()
		);
	}
}
