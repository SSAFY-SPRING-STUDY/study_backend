package ssafy.study.backend.domain.member.service;

import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ssafy.study.backend.domain.member.entity.Member;
import ssafy.study.backend.domain.member.repository.MemberGithubConnectStateRepository;
import ssafy.study.backend.domain.member.repository.MemberRepository;
import ssafy.study.backend.global.exception.CustomException;
import ssafy.study.backend.global.exception.error.ErrorCode;
import ssafy.study.backend.global.github.GitHubOAuthClient;
import ssafy.study.backend.global.github.GitHubProperties;
import ssafy.study.backend.global.github.dto.GitHubTokenResponse;
import ssafy.study.backend.global.github.dto.GitHubUserResponse;

/**
 * 로그인된 회원의 계정에 GitHub 정보를 사후 연결하는 흐름.
 * <p>
 * - start: state 발급 + Redis 저장 → GitHub OAuth 페이지 URL 반환
 * - callback: state 검증으로 어떤 회원에 묶을지 복원 → GitHub 정보 수신 → 충돌 검증 → 연결
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MemberGithubConnectService {

	private final GitHubProperties gitHubProperties;
	private final GitHubOAuthClient gitHubOAuthClient;
	private final MemberRepository memberRepository;
	private final MemberGithubConnectStateRepository stateRepository;

	/** GitHub OAuth 페이지로 보낼 URL 생성. state 는 Redis 에 5분 TTL 로 저장. */
	public String startConnect(Long memberId) {
		String state = UUID.randomUUID().toString();
		stateRepository.save(state, memberId);

		return "https://github.com/login/oauth/authorize"
			+ "?client_id=" + gitHubProperties.clientId()
			+ "&redirect_uri=" + gitHubProperties.connectRedirectUri()
			+ "&scope=read:user,user:email"
			+ "&state=" + state;
	}

	/** GitHub 콜백 처리. state 로 회원을 복원하고 GitHub 정보를 연결한다. */
	@Transactional
	public void handleConnectCallback(String code, String state) {
		Long memberId = resolveMemberIdFromState(state);

		GitHubUserResponse userInfo = fetchGitHubUser(code);

		Member member = memberRepository.findById(memberId)
			.orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));

		ensureGitHubNotLinkedToOtherMember(userInfo.id(), memberId);

		member.connectGithub(userInfo.id(), userInfo.login());
	}

	// ===== private =====

	private Long resolveMemberIdFromState(String state) {
		if (state == null || state.isBlank()) {
			throw new CustomException(ErrorCode.GITHUB_CONNECT_STATE_INVALID);
		}
		Long memberId = stateRepository.findMemberIdAndDelete(state);
		if (memberId == null) {
			throw new CustomException(ErrorCode.GITHUB_CONNECT_STATE_INVALID);
		}
		return memberId;
	}

	private GitHubUserResponse fetchGitHubUser(String code) {
		try {
			GitHubTokenResponse token = gitHubOAuthClient.exchangeConnectCode(code);
			return gitHubOAuthClient.getUser(token.accessToken());
		} catch (Exception e) {
			log.warn("GitHub connect 흐름에서 사용자 정보 조회 실패", e);
			throw new CustomException(ErrorCode.GITHUB_OAUTH_FAILED);
		}
	}

	private void ensureGitHubNotLinkedToOtherMember(String githubId, Long currentMemberId) {
		Optional<Member> existing = memberRepository.findByGithubId(githubId);
		if (existing.isPresent() && !existing.get().getId().equals(currentMemberId)) {
			throw new CustomException(ErrorCode.GITHUB_ALREADY_LINKED_TO_OTHER_MEMBER);
		}
	}
}
