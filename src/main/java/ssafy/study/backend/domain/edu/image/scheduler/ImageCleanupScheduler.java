package ssafy.study.backend.domain.edu.image.scheduler;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ssafy.study.backend.domain.edu.image.entity.Image;
import ssafy.study.backend.domain.edu.image.repository.ImageRepository;
import ssafy.study.backend.global.aws.s3.S3Service;

@Slf4j
@Component
@RequiredArgsConstructor
public class ImageCleanupScheduler {

	private final ImageRepository imageRepository;
	private final S3Service s3Service;

	/** 매일 새벽 3시: post가 없고 24시간 이상 지난 이미지를 S3·DB에서 삭제 */
	@Scheduled(cron = "0 0 3 * * *")
	@Transactional
	public void cleanupOrphanImages() {
		LocalDateTime cutoff = LocalDateTime.now().minusHours(24);
		List<Image> orphans = imageRepository.findByPostIsNullAndCreatedAtBefore(cutoff);

		if (orphans.isEmpty()) return;

		log.info("고아 이미지 정리 시작: {}건", orphans.size());
		orphans.forEach(image -> {
			try {
				s3Service.deleteObject(image.getKey());
			} catch (Exception e) {
				log.warn("S3 삭제 실패 (imageId={}): {}", image.getId(), e.getMessage());
			}
		});
		imageRepository.deleteAll(orphans);
		log.info("고아 이미지 정리 완료: {}건 삭제", orphans.size());
	}
}
