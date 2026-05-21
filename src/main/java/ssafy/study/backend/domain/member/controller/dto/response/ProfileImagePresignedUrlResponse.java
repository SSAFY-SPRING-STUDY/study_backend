package ssafy.study.backend.domain.member.controller.dto.response;

public record ProfileImagePresignedUrlResponse(
	String presignedUrl,
	String imageKey
) {
}
