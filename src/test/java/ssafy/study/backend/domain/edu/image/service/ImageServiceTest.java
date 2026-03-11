package ssafy.study.backend.domain.edu.image.service;

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

import jakarta.persistence.EntityManager;
import ssafy.study.backend.domain.edu.image.controller.dto.request.ImageRequest;
import ssafy.study.backend.domain.edu.image.controller.dto.response.ImageResponse;
import ssafy.study.backend.domain.edu.image.entity.Image;
import ssafy.study.backend.domain.edu.image.entity.ImageStatus;
import ssafy.study.backend.domain.edu.image.repository.ImageRepository;
import ssafy.study.backend.domain.edu.post.entity.Post;
import ssafy.study.backend.domain.edu.post.repository.PostRepository;
import ssafy.study.backend.fixture.CurriculumFixture;
import ssafy.study.backend.fixture.ImageFixture;
import ssafy.study.backend.fixture.MemberFixture;
import ssafy.study.backend.fixture.PostFixture;
import ssafy.study.backend.fixture.StudyFixture;
import ssafy.study.backend.global.aws.s3.S3Service;
import ssafy.study.backend.global.exception.CustomException;
import ssafy.study.backend.global.exception.error.ErrorCode;

@ExtendWith(MockitoExtension.class)
class ImageServiceTest {

	@InjectMocks
	private ImageService imageService;

	@Mock
	private S3Service s3Service;

	@Mock
	private EntityManager entityManager;

	@Mock
	private PostRepository postRepository;

	@Mock
	private ImageRepository imageRepository;

	@Test
	@DisplayName("이미지 업로드 presigned URL 발급 성공")
	void 이미지_업로드_URL_발급_성공() {
		// given
		var member = MemberFixture.member(1L);
		var curriculum = CurriculumFixture.curriculum(1L, StudyFixture.study(1L));
		Post post = PostFixture.post(1L, member, curriculum);
		ImageRequest request = new ImageRequest("image/png", 1024L, "test.png");
		String presignedUrl = "https://s3.example.com/presigned-upload-url";

		given(entityManager.getReference(Post.class, 1L)).willReturn(post);
		given(imageRepository.save(any(Image.class))).willAnswer(inv -> inv.getArgument(0));
		given(s3Service.getUploadPresignedUrl(anyString(), eq("image/png"), eq(1024L))).willReturn(presignedUrl);

		// when
		ImageResponse result = imageService.getImageUploadUrl(1L, request);

		// then
		assertThat(result.imageUrl()).isEqualTo(presignedUrl);
		assertThat(result.imageKey()).contains("posts/1/");
		assertThat(result.imageKey()).endsWith("test.png");
	}

	@Test
	@DisplayName("이미지 다운로드 presigned URL 발급 성공")
	void 이미지_다운로드_URL_발급_성공() {
		// given
		var member = MemberFixture.member(1L);
		var curriculum = CurriculumFixture.curriculum(1L, StudyFixture.study(1L));
		Post post = PostFixture.post(1L, member, curriculum);
		Image image = ImageFixture.image(1L, post);
		String presignedUrl = "https://s3.example.com/presigned-download-url";

		given(imageRepository.findById(1L)).willReturn(Optional.of(image));
		given(s3Service.getDownloadPresignedUrl(image.getKey())).willReturn(presignedUrl);

		// when
		ImageResponse result = imageService.getImageDownloadUrl(1L);

		// then
		assertThat(result.imageUrl()).isEqualTo(presignedUrl);
		assertThat(result.imageKey()).isEqualTo(image.getKey());
	}

	@Test
	@DisplayName("이미지 다운로드 URL 발급 실패 - 존재하지 않는 이미지")
	void 이미지_다운로드_URL_발급_실패_존재하지_않는_이미지() {
		// given
		given(imageRepository.findById(999L)).willReturn(Optional.empty());

		// when & then
		assertThatThrownBy(() -> imageService.getImageDownloadUrl(999L))
			.isInstanceOf(CustomException.class)
			.hasFieldOrPropertyWithValue("errorCode", ErrorCode.IMAGE_NOT_FOUND);
	}

	@Test
	@DisplayName("게시글의 이미지 목록 조회 성공")
	void 게시글_이미지_목록_조회_성공() {
		// given
		var member = MemberFixture.member(1L);
		var curriculum = CurriculumFixture.curriculum(1L, StudyFixture.study(1L));
		Post post = PostFixture.post(1L, member, curriculum);
		Image image1 = ImageFixture.image(1L, post);
		Image image2 = ImageFixture.image(2L, post);
		String downloadUrl = "https://s3.example.com/presigned-url";

		given(postRepository.findById(1L)).willReturn(Optional.of(post));
		given(imageRepository.findByPost(post)).willReturn(List.of(image1, image2));
		given(s3Service.getDownloadPresignedUrl(anyString())).willReturn(downloadUrl);

		// when
		List<ImageResponse> result = imageService.getImagesByPost(1L);

		// then
		assertThat(result).hasSize(2);
		assertThat(result.get(0).imageUrl()).isEqualTo(downloadUrl);
	}

	@Test
	@DisplayName("게시글 이미지 목록 조회 실패 - 존재하지 않는 게시글")
	void 게시글_이미지_목록_조회_실패_존재하지_않는_게시글() {
		// given
		given(postRepository.findById(999L)).willReturn(Optional.empty());

		// when & then
		assertThatThrownBy(() -> imageService.getImagesByPost(999L))
			.isInstanceOf(CustomException.class)
			.hasFieldOrPropertyWithValue("errorCode", ErrorCode.POST_NOT_FOUND);
	}

	@Test
	@DisplayName("이미지 업로드 완료 처리 성공")
	void 이미지_업로드_완료_처리_성공() {
		// given
		var member = MemberFixture.member(1L);
		var curriculum = CurriculumFixture.curriculum(1L, StudyFixture.study(1L));
		Post post = PostFixture.post(1L, member, curriculum);
		Image image = ImageFixture.image(1L, post);
		given(imageRepository.findById(1L)).willReturn(Optional.of(image));

		// when
		imageService.completeImageUpload(1L);

		// then
		assertThat(image.getStatus()).isEqualTo(ImageStatus.UPLOADED);
	}

	@Test
	@DisplayName("이미지 업로드 완료 처리 실패 - 존재하지 않는 이미지")
	void 이미지_업로드_완료_처리_실패_존재하지_않는_이미지() {
		// given
		given(imageRepository.findById(999L)).willReturn(Optional.empty());

		// when & then
		assertThatThrownBy(() -> imageService.completeImageUpload(999L))
			.isInstanceOf(CustomException.class)
			.hasFieldOrPropertyWithValue("errorCode", ErrorCode.IMAGE_NOT_FOUND);
	}
}
