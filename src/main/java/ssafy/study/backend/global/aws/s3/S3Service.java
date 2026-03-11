package ssafy.study.backend.global.aws.s3;

import java.time.Duration;

import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Exception;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import ssafy.study.backend.global.aws.config.AwsProperties;
import ssafy.study.backend.global.exception.CustomException;
import ssafy.study.backend.global.exception.error.ErrorCode;

@Slf4j
@Service
@RequiredArgsConstructor
public class S3Service {
	private final S3Client s3Client;
	private final S3Presigner s3Presigner;
	private final AwsProperties awsProperties;

	public String getDownloadPresignedUrl(String key) {
		return s3Presigner.presignGetObject(b -> b
			.signatureDuration(Duration.ofMinutes(10))
			.getObjectRequest(g -> g.bucket(awsProperties.bucket()).key(key))
		).url().toString();
	}

	public String getUploadPresignedUrl(String key, String contentType, Long contentLength) {

		return s3Presigner.presignPutObject(b -> b
			.signatureDuration(Duration.ofMinutes(5))
			.putObjectRequest(p -> p
				.bucket(awsProperties.bucket())
				.key(key)
				.contentType(contentType)
				.contentLength(contentLength)
			)
		).url().toString();
	}

	public void deleteObject(String key) {
		DeleteObjectRequest deleteObjectRequest = DeleteObjectRequest.builder()
			.bucket(awsProperties.bucket())
			.key(key)
			.build();

		try {
			s3Client.deleteObject(deleteObjectRequest);
		} catch(S3Exception e) {
			log.error("AWS ERROR: s3 Obejct 삭제 에러가 발생하였습니다. AWS S3 Exception: {}", e.awsErrorDetails().errorMessage());
			throw new CustomException(ErrorCode.INTERNAL_SERVER_ERROR);

		} catch (Exception e) {
			log.error("AWS ERROR: s3 Obejct 삭제 에러가 발생하였습니다.");
			throw new CustomException(ErrorCode.INTERNAL_SERVER_ERROR);
		}
	}


}
