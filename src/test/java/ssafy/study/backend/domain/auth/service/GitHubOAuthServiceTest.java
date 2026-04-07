package ssafy.study.backend.domain.auth.service;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;

import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import ssafy.study.backend.domain.auth.repository.RefreshTokenRepository;
import ssafy.study.backend.domain.member.entity.Member;
import ssafy.study.backend.domain.member.repository.MemberRepository;
import ssafy.study.backend.global.exception.CustomException;
import ssafy.study.backend.global.exception.error.ErrorCode;
import ssafy.study.backend.global.github.GitHubOAuthClient;
import ssafy.study.backend.global.github.GitHubProperties;
import ssafy.study.backend.global.github.dto.GitHubTokenResponse;
import ssafy.study.backend.global.github.dto.GitHubUserResponse;
import ssafy.study.backend.global.security.jwt.JwtProvider;

@ExtendWith(MockitoExtension.class)
class GitHubOAuthServiceTest {

	@InjectMocks
	private GitHubOAuthService gitHubOAuthService;

	@Mock
	private GitHubOAuthClient gitHubOAuthClient;

	@Mock
	private GitHubProperties gitHubProperties;

	@Mock
	private MemberRepository memberRepository;

	@Mock
	private RefreshTokenRepository refreshTokenRepository;

	@Mock
	private JwtProvider jwtProvider;

	// ===== ISSUE-6: 닉네임 충돌 시 재시도 =====

	@Test
	@DisplayName("login과 login_githubId 모두 충돌 시 랜덤 suffix 닉네임으로 회원 생성")
	void 닉네임_이중_충돌_시_랜덤_suffix_생성() {
		// given
		GitHubUserResponse userInfo = new GitHubUserResponse("gh123", "cooluser", null, "Cool User");
		GitHubTokenResponse tokenResponse = new GitHubTokenResponse("access_token", "bearer");

		given(gitHubOAuthClient.exchangeLoginCode("code")).willReturn(tokenResponse);
		given(gitHubOAuthClient.getUser("access_token")).willReturn(userInfo);
		given(memberRepository.findByGithubId("gh123")).willReturn(Optional.empty());
		// email이 null이므로 createGitHubMember 직접 호출됨

		// "cooluser"와 "cooluser_gh123" 모두 충돌
		given(memberRepository.existsByNickname("cooluser")).willReturn(true);
		given(memberRepository.existsByNickname("cooluser_gh123")).willReturn(true);
		// suffix 붙인 닉네임은 사용 가능
		given(memberRepository.existsByNickname(argThat(n ->
			n != null && n.startsWith("cooluser_gh123_") && n.length() > "cooluser_gh123_".length()
		))).willReturn(false);

		given(memberRepository.save(any(Member.class))).willAnswer(inv -> inv.getArgument(0));
		given(jwtProvider.generateAccessToken(any(), any())).willReturn("access-token");
		given(jwtProvider.generateRefreshToken(any(), any())).willReturn("refresh-token");

		// when & then: 예외 없이 처리 완료
		assertThatNoException().isThrownBy(() -> gitHubOAuthService.handleLoginCallback("code"));

		// 저장된 멤버의 닉네임이 "cooluser_gh123_" 으로 시작하는지 확인
		then(memberRepository).should().save(argThat(m ->
			m.getNickname().startsWith("cooluser_gh123_")
		));
	}

	@Test
	@DisplayName("닉네임 충돌 3회 모두 실패 시 USERNAME_DUPLICATE 에러")
	void 닉네임_충돌_3회_실패_USERNAME_DUPLICATE_에러() {
		// given
		GitHubUserResponse userInfo = new GitHubUserResponse("gh456", "collider", null, "Collider");
		GitHubTokenResponse tokenResponse = new GitHubTokenResponse("access_token", "bearer");

		given(gitHubOAuthClient.exchangeLoginCode("code")).willReturn(tokenResponse);
		given(gitHubOAuthClient.getUser("access_token")).willReturn(userInfo);
		given(memberRepository.findByGithubId("gh456")).willReturn(Optional.empty());

		// 모든 닉네임 후보가 충돌
		given(memberRepository.existsByNickname(anyString())).willReturn(true);

		// when & then
		assertThatThrownBy(() -> gitHubOAuthService.handleLoginCallback("code"))
			.isInstanceOf(CustomException.class)
			.hasFieldOrPropertyWithValue("errorCode", ErrorCode.USERNAME_DUPLICATE);
	}

	@Test
	@DisplayName("닉네임 충돌 없을 때 login 그대로 사용")
	void 닉네임_충돌_없을때_login_그대로_사용() {
		// given
		GitHubUserResponse userInfo = new GitHubUserResponse("gh789", "uniqueuser", null, "Unique User");
		GitHubTokenResponse tokenResponse = new GitHubTokenResponse("access_token", "bearer");

		given(gitHubOAuthClient.exchangeLoginCode("code")).willReturn(tokenResponse);
		given(gitHubOAuthClient.getUser("access_token")).willReturn(userInfo);
		given(memberRepository.findByGithubId("gh789")).willReturn(Optional.empty());
		given(memberRepository.existsByNickname("uniqueuser")).willReturn(false);

		given(memberRepository.save(any(Member.class))).willAnswer(inv -> inv.getArgument(0));
		given(jwtProvider.generateAccessToken(any(), any())).willReturn("access-token");
		given(jwtProvider.generateRefreshToken(any(), any())).willReturn("refresh-token");

		// when & then
		assertThatNoException().isThrownBy(() -> gitHubOAuthService.handleLoginCallback("code"));

		then(memberRepository).should().save(argThat(m ->
			m.getNickname().equals("uniqueuser")
		));
	}
}
