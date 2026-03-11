package ssafy.study.backend.domain.edu.notice.controller.dto.response;

import java.time.LocalDateTime;

import ssafy.study.backend.domain.edu.notice.entity.Notice;

public record NoticeResponse(
	Long id,
	String title,
	String content,
	LocalDateTime createdAt,
	LocalDateTime updatedAt
) {
	public static NoticeResponse from(Notice notice) {
		return new NoticeResponse(
			notice.getId(),
			notice.getTitle(),
			notice.getContent(),
			notice.getCreatedAt(),
			notice.getUpdatedAt()
		);
	}
}
