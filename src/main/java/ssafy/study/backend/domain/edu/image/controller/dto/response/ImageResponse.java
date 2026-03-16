package ssafy.study.backend.domain.edu.image.controller.dto.response;

public record ImageResponse(
	Long imageId,
	String imageUrl,
	String imageKey
) {
}
