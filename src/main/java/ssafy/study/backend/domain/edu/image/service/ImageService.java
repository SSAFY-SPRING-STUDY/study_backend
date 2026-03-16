package ssafy.study.backend.domain.edu.image.service;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import ssafy.study.backend.domain.edu.image.controller.dto.request.ImageRequest;
import ssafy.study.backend.domain.edu.image.controller.dto.response.ImageResponse;
import ssafy.study.backend.domain.edu.image.entity.Image;
import ssafy.study.backend.domain.edu.image.repository.ImageRepository;
import ssafy.study.backend.domain.edu.post.entity.Post;
import ssafy.study.backend.domain.edu.post.repository.PostRepository;
import ssafy.study.backend.global.aws.s3.S3Service;
import ssafy.study.backend.global.exception.CustomException;
import ssafy.study.backend.global.exception.error.ErrorCode;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ImageService {

	private final S3Service s3Service;
	private final EntityManager entityManager;
	private final PostRepository postRepository;
	private final ImageRepository imageRepository;

	@Transactional
	public ImageResponse getImageUploadUrl(ImageRequest request) {
		String key = generateKey(request.fileName());
		Image image = Image.createPending(key);
		imageRepository.save(image);
		String preSignedUrl = s3Service.getUploadPresignedUrl(key, request.contentType(), request.contentLength());
		return new ImageResponse(image.getId(), preSignedUrl, key);
	}

	@Transactional
	public void attachImagesToPost(Long postId, List<Long> imageIds) {
		if (imageIds == null || imageIds.isEmpty()) return;
		Post post = entityManager.getReference(Post.class, postId);
		List<Image> images = imageRepository.findByIdsAndPostIsNull(imageIds);
		images.forEach(image -> image.attachTo(post));
	}

	/** GET /images/{imageId} 리다이렉트용 presigned URL 반환 (유효시간 60분) */
	public String getImageRedirectUrl(Long imageId) {
		Image image = imageRepository.findById(imageId)
			.orElseThrow(() -> new CustomException(ErrorCode.IMAGE_NOT_FOUND));
		return s3Service.getDownloadPresignedUrl(image.getKey());
	}

	public List<ImageResponse> getImagesByPost(Long postId) {
		Post post = postRepository.findById(postId)
			.orElseThrow(() -> new CustomException(ErrorCode.POST_NOT_FOUND));

		return imageRepository.findByPost(post).stream()
			.map(image -> new ImageResponse(image.getId(), "/api/v1/images/" + image.getId(), image.getKey()))
			.toList();
	}

	@Transactional
	public void completeImageUpload(Long imageId) {
		Image image = imageRepository.findById(imageId)
			.orElseThrow(() -> new CustomException(ErrorCode.IMAGE_NOT_FOUND));
		image.markAsUploaded();
	}

	private String generateKey(String fileName) {
		return String.format("images/%s_%s", UUID.randomUUID(), fileName);
	}
}
