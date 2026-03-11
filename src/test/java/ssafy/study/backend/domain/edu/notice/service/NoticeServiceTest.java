package ssafy.study.backend.domain.edu.notice.service;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.*;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import jakarta.persistence.EntityManager;
import ssafy.study.backend.domain.edu.notice.controller.dto.request.NoticeRequest;
import ssafy.study.backend.domain.edu.notice.controller.dto.response.NoticeResponse;
import ssafy.study.backend.domain.edu.notice.entity.Notice;
import ssafy.study.backend.domain.edu.notice.repository.NoticeRepository;
import ssafy.study.backend.domain.member.entity.Member;
import ssafy.study.backend.domain.notification.entity.NotificationType;
import ssafy.study.backend.domain.notification.service.NotificationService;
import ssafy.study.backend.fixture.MemberFixture;
import ssafy.study.backend.fixture.NoticeFixture;
import ssafy.study.backend.global.exception.CustomException;
import ssafy.study.backend.global.exception.error.ErrorCode;

@ExtendWith(MockitoExtension.class)
class NoticeServiceTest {

	@InjectMocks
	private NoticeService noticeService;

	@Mock
	private EntityManager entityManager;

	@Mock
	private NoticeRepository noticeRepository;

	@Mock
	private NotificationService notificationService;

	@Test
	@DisplayName("공지사항 단건 조회 성공")
	void 공지사항_단건_조회_성공() {
		// given
		Member author = MemberFixture.member(1L);
		Notice notice = NoticeFixture.notice(1L, author);
		given(noticeRepository.findById(1L)).willReturn(Optional.of(notice));

		// when
		NoticeResponse result = noticeService.getNotice(1L);

		// then
		assertThat(result.id()).isEqualTo(1L);
		assertThat(result.title()).isEqualTo(notice.getTitle());
		assertThat(result.content()).isEqualTo(notice.getContent());
	}

	@Test
	@DisplayName("공지사항 단건 조회 실패 - 존재하지 않는 공지사항")
	void 공지사항_단건_조회_실패_존재하지_않는_공지사항() {
		// given
		given(noticeRepository.findById(999L)).willReturn(Optional.empty());

		// when & then
		assertThatThrownBy(() -> noticeService.getNotice(999L))
			.isInstanceOf(CustomException.class)
			.hasFieldOrPropertyWithValue("errorCode", ErrorCode.NOTICE_NOT_FOUND);
	}

	@Test
	@DisplayName("공지사항 생성 성공 - 전체 알림 발송")
	void 공지사항_생성_성공() {
		// given
		Member author = MemberFixture.member(1L);
		NoticeRequest request = new NoticeRequest("공지 제목", "공지 내용");
		given(entityManager.getReference(Member.class, 1L)).willReturn(author);
		willDoNothing().given(notificationService).createNotification(NotificationType.NOTICE_CREATED.getMessage());

		// when
		assertThatNoException().isThrownBy(() -> noticeService.createNotice(request, 1L));

		// then
		then(noticeRepository).should().save(any(Notice.class));
		then(notificationService).should().createNotification(NotificationType.NOTICE_CREATED.getMessage());
	}

	@Test
	@DisplayName("공지사항 수정 성공")
	void 공지사항_수정_성공() {
		// given
		Member author = MemberFixture.member(1L);
		Notice notice = NoticeFixture.notice(1L, author);
		NoticeRequest request = new NoticeRequest("수정된 제목", "수정된 내용");
		given(noticeRepository.findById(1L)).willReturn(Optional.of(notice));

		// when
		NoticeResponse result = noticeService.updateNotice(1L, 1L, request);

		// then
		assertThat(result.title()).isEqualTo("수정된 제목");
		assertThat(result.content()).isEqualTo("수정된 내용");
	}

	@Test
	@DisplayName("공지사항 수정 실패 - 존재하지 않는 공지사항")
	void 공지사항_수정_실패_존재하지_않는_공지사항() {
		// given
		NoticeRequest request = new NoticeRequest("수정된 제목", "수정된 내용");
		given(noticeRepository.findById(999L)).willReturn(Optional.empty());

		// when & then
		assertThatThrownBy(() -> noticeService.updateNotice(999L, 1L, request))
			.isInstanceOf(CustomException.class)
			.hasFieldOrPropertyWithValue("errorCode", ErrorCode.NOTICE_NOT_FOUND);
	}

	@Test
	@DisplayName("공지사항 삭제 성공")
	void 공지사항_삭제_성공() {
		// given
		Member author = MemberFixture.member(1L);
		Notice notice = NoticeFixture.notice(1L, author);
		given(noticeRepository.findById(1L)).willReturn(Optional.of(notice));
		willDoNothing().given(noticeRepository).delete(notice);

		// when & then
		assertThatNoException().isThrownBy(() -> noticeService.deleteNotice(1L, 1L));
		then(noticeRepository).should().delete(notice);
	}

	@Test
	@DisplayName("공지사항 삭제 실패 - 존재하지 않는 공지사항")
	void 공지사항_삭제_실패_존재하지_않는_공지사항() {
		// given
		given(noticeRepository.findById(999L)).willReturn(Optional.empty());

		// when & then
		assertThatThrownBy(() -> noticeService.deleteNotice(999L, 1L))
			.isInstanceOf(CustomException.class)
			.hasFieldOrPropertyWithValue("errorCode", ErrorCode.NOTICE_NOT_FOUND);
	}

	@Test
	@DisplayName("공지사항 목록 조회 성공")
	void 공지사항_목록_조회_성공() {
		// given
		Member author = MemberFixture.member(1L);
		Notice notice1 = NoticeFixture.notice(1L, author);
		Notice notice2 = NoticeFixture.notice(2L, author);
		PageRequest pageable = PageRequest.of(0, 10);
		Page<Notice> noticePage = new PageImpl<>(List.of(notice1, notice2), pageable, 2);
		given(noticeRepository.findAll(pageable)).willReturn(noticePage);

		// when
		Page<NoticeResponse> result = noticeService.getNotices(pageable);

		// then
		assertThat(result.getTotalElements()).isEqualTo(2);
		assertThat(result.getContent()).hasSize(2);
	}
}
