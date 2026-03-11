package ssafy.study.backend.domain.edu.notice.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import ssafy.study.backend.domain.edu.notice.controller.dto.request.NoticeRequest;
import ssafy.study.backend.domain.edu.notice.controller.dto.response.NoticeResponse;
import ssafy.study.backend.domain.edu.notice.entity.Notice;
import ssafy.study.backend.domain.edu.notice.repository.NoticeRepository;
import ssafy.study.backend.domain.member.entity.Member;
import ssafy.study.backend.domain.notification.entity.NotificationType;
import ssafy.study.backend.domain.notification.service.NotificationService;
import ssafy.study.backend.global.exception.CustomException;
import ssafy.study.backend.global.exception.error.ErrorCode;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class NoticeService {
	private final EntityManager entityManager;
	private final NoticeRepository noticeRepository;
	private final NotificationService notificationService;

	public NoticeResponse getNotice(Long noticeId) {
		Notice notice = noticeRepository.findById(noticeId)
			.orElseThrow(() -> new CustomException(ErrorCode.NOTICE_NOT_FOUND));
		return NoticeResponse.from(notice);
	}

	@Transactional
	public void createNotice(NoticeRequest request, Long authorId) {
		Member author = entityManager.getReference(Member.class, authorId);

		Notice notice = Notice.builder()
			.title(request.title())
			.content(request.content())
			.author(author)
			.build();

		noticeRepository.save(notice);
		notificationService.createNotification(NotificationType.NOTICE_CREATED.getMessage());
	}

	@Transactional
	public NoticeResponse updateNotice(Long noticeId, Long authorId, NoticeRequest request) {
		Notice notice = noticeRepository.findById(noticeId)
			.orElseThrow(() -> new CustomException(ErrorCode.NOTICE_NOT_FOUND));

		notice.update(request.title(), request.content());
		return NoticeResponse.from(notice);
	}

	@Transactional
	public void deleteNotice(Long noticeId, Long authorId) {
		Notice notice = noticeRepository.findById(noticeId)
			.orElseThrow(() -> new CustomException(ErrorCode.NOTICE_NOT_FOUND));

		noticeRepository.delete(notice);
	}

	public Page<NoticeResponse> getNotices(PageRequest pageable) {
		return noticeRepository.findAll(pageable)
			.map(NoticeResponse::from);
	}
}
