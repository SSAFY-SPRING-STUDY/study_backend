package ssafy.study.backend.domain.member.service;

import java.util.UUID;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import ssafy.study.backend.domain.member.controller.dto.request.ConfirmProfileImageRequest;
import ssafy.study.backend.domain.member.controller.dto.request.MemberUpdateRequest;
import ssafy.study.backend.domain.member.controller.dto.request.PasswordUpdateRequest;
import ssafy.study.backend.domain.member.controller.dto.request.ProfileImagePresignedUrlRequest;
import ssafy.study.backend.domain.member.controller.dto.request.SignupRequest;
import ssafy.study.backend.domain.member.controller.dto.request.UpdateDescriptionRequest;
import ssafy.study.backend.domain.member.controller.dto.response.MemberDetailInfo;
import ssafy.study.backend.domain.member.controller.dto.response.MemberInfo;
import ssafy.study.backend.domain.member.controller.dto.response.ProfileImagePresignedUrlResponse;
import ssafy.study.backend.domain.member.entity.Member;
import ssafy.study.backend.domain.member.entity.MemberLevel;
import ssafy.study.backend.domain.member.entity.MemberProfile;
import ssafy.study.backend.domain.member.entity.MemberRole;
import ssafy.study.backend.domain.member.repository.MemberProfileRepository;
import ssafy.study.backend.domain.member.repository.MemberRepository;
import ssafy.study.backend.global.aws.s3.S3Service;
import ssafy.study.backend.global.exception.CustomException;
import ssafy.study.backend.global.exception.error.ErrorCode;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MemberService {

	private final MemberRepository memberRepository;
	private final MemberProfileRepository memberProfileRepository;
	private final PasswordEncoder passwordEncoder;
	private final S3Service s3Service;

	@Transactional
	public void signup(SignupRequest request) {
		if (memberRepository.existsByEmail(request.email())) {
			throw new CustomException(ErrorCode.EMAIL_DUPLICATE);
		}
		if (memberRepository.existsByNickname(request.nickname())) {
			throw new CustomException(ErrorCode.USERNAME_DUPLICATE);
		}

		Member member = Member.builder()
			.email(request.email())
			.password(passwordEncoder.encode(request.password()))
			.name(request.name())
			.nickname(request.nickname())
			.role(MemberRole.ROLE_USER)
			.level(MemberLevel.BASIC)
			.build();

		memberRepository.save(member);
	}

	public MemberInfo getInfo(Long memberId) {
		Member member = memberRepository.findById(memberId)
			.orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));

		return MemberInfo.fromEntity(member);
	}

	public MemberDetailInfo getDetailInfo(Long memberId) {
		Member member = memberRepository.findById(memberId)
			.orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));
		MemberProfile profile = memberProfileRepository.findByMemberId(memberId)
			.orElseGet(() -> MemberProfile.createDefault(member));

		String profileImageUrl = resolveProfileImageUrl(profile);
		return MemberDetailInfo.fromEntity(member, profile, profileImageUrl);
	}

	@Transactional
	public MemberInfo updateInfo(Long requesterId, MemberUpdateRequest request) {
		Member member = memberRepository.findById(requesterId)
			.orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));

		if (request.nickname() != null && !request.nickname().equals(member.getNickname())) {
			if (memberRepository.existsByNickname(request.nickname())) {
				throw new CustomException(ErrorCode.USERNAME_DUPLICATE);
			}
			member.setNickname(request.nickname());
		}

		if (request.name() != null) {
			member.setName(request.name());
		}

		return MemberInfo.fromEntity(member);
	}

	@Transactional
	public void updatePassword(Long requesterId, PasswordUpdateRequest request) {
		Member member = memberRepository.findById(requesterId)
			.orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));

		if (!passwordEncoder.matches(request.currentPassword(), member.getPassword())) {
			throw new CustomException(ErrorCode.INVALID_PASSWORD);
		}

		if (passwordEncoder.matches(request.newPassword(), member.getPassword())) {
			throw new CustomException(ErrorCode.SAME_AS_OLD_PASSWORD);
		}

		member.setPassword(passwordEncoder.encode(request.newPassword()));
	}

	@Transactional
	public MemberDetailInfo updateDescription(Long memberId, UpdateDescriptionRequest request) {
		Member member = memberRepository.findById(memberId)
			.orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));
		MemberProfile profile = memberProfileRepository.findByMemberId(memberId)
			.orElseGet(() -> memberProfileRepository.save(MemberProfile.createDefault(member)));

		profile.updateDescription(request.description());

		String profileImageUrl = resolveProfileImageUrl(profile);
		return MemberDetailInfo.fromEntity(member, profile, profileImageUrl);
	}

	public ProfileImagePresignedUrlResponse getProfileImagePresignedUrl(Long memberId,
		ProfileImagePresignedUrlRequest request) {
		String key = "profile-images/" + memberId + "/" + UUID.randomUUID();
		String presignedUrl = s3Service.getUploadPresignedUrl(key, request.contentType(), request.contentLength());
		return new ProfileImagePresignedUrlResponse(presignedUrl, key);
	}

	@Transactional
	public MemberDetailInfo confirmProfileImage(Long memberId, ConfirmProfileImageRequest request) {
		Member member = memberRepository.findById(memberId)
			.orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));
		MemberProfile profile = memberProfileRepository.findByMemberId(memberId)
			.orElseGet(() -> memberProfileRepository.save(MemberProfile.createDefault(member)));

		if (profile.getProfileImageKey() != null) {
			s3Service.deleteObject(profile.getProfileImageKey());
		}
		profile.updateProfileImageKey(request.imageKey());

		String profileImageUrl = s3Service.getDownloadPresignedUrl(profile.getProfileImageKey());
		return MemberDetailInfo.fromEntity(member, profile, profileImageUrl);
	}

	@Transactional
	public MemberDetailInfo deleteProfileImage(Long memberId) {
		Member member = memberRepository.findById(memberId)
			.orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));
		MemberProfile profile = memberProfileRepository.findByMemberId(memberId)
			.orElseGet(() -> MemberProfile.createDefault(member));

		if (profile.getProfileImageKey() != null) {
			s3Service.deleteObject(profile.getProfileImageKey());
			profile.deleteProfileImage();
		}

		return MemberDetailInfo.fromEntity(member, profile, null);
	}

	// ===== private =====

	private String resolveProfileImageUrl(MemberProfile profile) {
		if (profile.getProfileImageKey() == null) {
			return null;
		}
		return s3Service.getDownloadPresignedUrl(profile.getProfileImageKey());
	}
}
