package ssafy.study.backend.fixture;

import org.springframework.test.util.ReflectionTestUtils;

import ssafy.study.backend.domain.edu.notice.entity.Notice;
import ssafy.study.backend.domain.member.entity.Member;

public class NoticeFixture {

	public static Notice notice(Member author) {
		return noticeBuilder(author).build();
	}

	public static Notice notice(Long id, Member author) {
		Notice notice = noticeBuilder(author).build();
		ReflectionTestUtils.setField(notice, "id", id);
		return notice;
	}

	public static Notice.NoticeBuilder noticeBuilder(Member author) {
		return Notice.builder()
			.title("테스트 공지사항")
			.content("테스트 공지 내용")
			.author(author);
	}
}
