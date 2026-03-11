package ssafy.study.backend.domain.edu.notice.controller.dto.request;

import ssafy.study.backend.domain.edu.notice.entity.Notice;

public record NoticeResponse(
	Long id,
	String title,
	String content) {
	public static NoticeResponse from(Notice notice) {
		return new NoticeResponse(
			notice.getId(),
			notice.getTitle(),
			notice.getContent()
		);
	}
}
