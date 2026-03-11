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
	public ImageResponse getImageUploadUrl(Long postId, ImageRequest request) {
		// S3에 업로드할 파일의 메타데이터 생성
		String key = generateKey(postId, request.fileName());

		Post post = entityManager.getReference(Post.class, postId);

		Image image = Image.create(post, key);

		imageRepository.save(image);

		// S3에서 presigned URL 생성
		String preSignedUrl = s3Service.getUploadPresignedUrl(key, request.contentType(), request.contentLength());

		return new ImageResponse(preSignedUrl, key);
	}
	public ImageResponse getImageDownloadUrl(Long imageId) {
		Image image = imageRepository.findById(imageId)
			.orElseThrow(() -> new CustomException(ErrorCode.IMAGE_NOT_FOUND));

		String preSignedUrl = s3Service.getDownloadPresignedUrl(image.getKey());

		return new ImageResponse(preSignedUrl, image.getKey());
	}

	public List<ImageResponse> getImagesByPost(Long postId) {
		Post post = postRepository.findById(postId)
			.orElseThrow(() -> new CustomException(ErrorCode.POST_NOT_FOUND));

		List<Image> images = imageRepository.findByPost(post);

		return images.stream()
			.map(image -> {
				String preSignedUrl = s3Service.getDownloadPresignedUrl(image.getKey());
				return new ImageResponse(preSignedUrl, image.getKey());
			})
			.toList();
	}

	@Transactional
	public void completeImageUpload(Long imageId) {
		Image image = imageRepository.findById(imageId)
			.orElseThrow(() -> new CustomException(ErrorCode.IMAGE_NOT_FOUND));

		image.markAsUploaded();
	}


	private String generateKey(Long postId, String fileName) {
		return String.format("posts/%d/%s_%s",
			postId,
			UUID.randomUUID(),
			fileName);
	}

}
