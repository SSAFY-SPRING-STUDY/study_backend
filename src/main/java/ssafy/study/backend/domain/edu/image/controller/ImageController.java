package ssafy.study.backend.domain.edu.image.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import ssafy.study.backend.domain.edu.image.controller.dto.request.ImageRequest;
import ssafy.study.backend.domain.edu.image.controller.dto.response.ImageResponse;
import ssafy.study.backend.domain.edu.image.service.ImageService;
import ssafy.study.backend.global.response.ApiResponse;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
@Tag(name="Image", description = "이미지 관련 API")
public class ImageController {
	private final ImageService imageService;

	@PostMapping("/posts/{postId}/images/presigned-url")
	@Operation(summary = "이미지 업로드 URL 생성", description = "새로운 이미지 URL을 생성합니다.")
	@ResponseStatus(HttpStatus.CREATED)
	public ApiResponse<ImageResponse> getUploadUrl(
		@PathVariable Long postId,
		@Valid @RequestBody ImageRequest request) {

		ImageResponse imageResponse = imageService.getImageUploadUrl(postId, request);
		return ApiResponse.success("이미지 업로드 URL이 발급되었습니다.", imageResponse);
	}

	@PatchMapping("/images/{imageId}/complete")
	@Operation(summary = "이미지 업로드 완료", description = "이미지 업로드를 완료합니다.")
	@ResponseStatus(HttpStatus.OK)
	public ApiResponse<Void> completeUpload(@PathVariable Long imageId) {
		imageService.completeImageUpload(imageId);
		return ApiResponse.success("이미지 업로드가 완료되었습니다.", null);
	}

	@GetMapping("/images/{imageId}")
	@Operation(summary = "특정 이미지 조회", description = "특정 이미지의 정보를 조회합니다.")
	@ResponseStatus(HttpStatus.OK)
	public ApiResponse<ImageResponse> getImage(@PathVariable Long imageId) {
		ImageResponse imageResponse = imageService.getImageDownloadUrl(imageId);
		return ApiResponse.success("이미지가 성공적으로 조회되었습니다.", imageResponse);
	}

	@GetMapping("/posts/{postId}/images")
	@Operation(summary = "게시글 이미지 조회", description = "특정 게시글에 포함된 이미지들의 정보를 조회합니다.")
	@ResponseStatus(HttpStatus.OK)
	public ApiResponse<List<ImageResponse>> getImagesByPost(@PathVariable Long postId) {
		List<ImageResponse> imageResponses = imageService.getImagesByPost(postId);
		return ApiResponse.success("게시글의 이미지들이 성공적으로 조회되었습니다.", imageResponses);
	}
}
