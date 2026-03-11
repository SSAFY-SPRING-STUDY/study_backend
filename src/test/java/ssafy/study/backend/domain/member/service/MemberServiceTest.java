package ssafy.study.backend.domain.member.service;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.*;

import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import ssafy.study.backend.domain.member.controller.dto.request.MemberUpdateRequest;
import ssafy.study.backend.domain.member.controller.dto.request.PasswordUpdateRequest;
import ssafy.study.backend.domain.member.controller.dto.request.SignupRequest;
import ssafy.study.backend.domain.member.controller.dto.response.MemberInfo;
import ssafy.study.backend.domain.member.entity.Member;
import ssafy.study.backend.domain.member.repository.MemberRepository;
import ssafy.study.backend.fixture.MemberFixture;
import ssafy.study.backend.global.exception.CustomException;
import ssafy.study.backend.global.exception.error.ErrorCode;

@ExtendWith(MockitoExtension.class)
class MemberServiceTest {

	@InjectMocks
	private MemberService memberService;

	@Mock
	private MemberRepository memberRepository;

	@Mock
	private PasswordEncoder passwordEncoder;

	@Test
	@DisplayName("회원가입 성공")
	void 회원가입_성공() {
		// given
		SignupRequest request = new SignupRequest("new@example.com", "password123", "새유저", "newuser");
		given(memberRepository.existsByEmail(request.email())).willReturn(false);
		given(memberRepository.existsByNickname(request.nickname())).willReturn(false);
		given(passwordEncoder.encode(request.password())).willReturn("encoded_password");

		// when & then
		assertThatNoException().isThrownBy(() -> memberService.signup(request));
		then(memberRepository).should().save(any(Member.class));
	}

	@Test
	@DisplayName("회원가입 실패 - 이메일 중복")
	void 회원가입_실패_이메일_중복() {
		// given
		SignupRequest request = new SignupRequest("dup@example.com", "password123", "새유저", "newuser");
		given(memberRepository.existsByEmail(request.email())).willReturn(true);

		// when & then
		assertThatThrownBy(() -> memberService.signup(request))
			.isInstanceOf(CustomException.class)
			.hasFieldOrPropertyWithValue("errorCode", ErrorCode.EMAIL_DUPLICATE);
	}

	@Test
	@DisplayName("회원가입 실패 - 닉네임 중복")
	void 회원가입_실패_닉네임_중복() {
		// given
		SignupRequest request = new SignupRequest("new@example.com", "password123", "새유저", "dupnick");
		given(memberRepository.existsByEmail(request.email())).willReturn(false);
		given(memberRepository.existsByNickname(request.nickname())).willReturn(true);

		// when & then
		assertThatThrownBy(() -> memberService.signup(request))
			.isInstanceOf(CustomException.class)
			.hasFieldOrPropertyWithValue("errorCode", ErrorCode.USERNAME_DUPLICATE);
	}

	@Test
	@DisplayName("회원 정보 조회 성공")
	void 회원_정보_조회_성공() {
		// given
		Member member = MemberFixture.member(1L);
		given(memberRepository.findById(1L)).willReturn(Optional.of(member));

		// when
		MemberInfo result = memberService.getInfo(1L);

		// then
		assertThat(result.id()).isEqualTo(1L);
		assertThat(result.email()).isEqualTo(member.getEmail());
		assertThat(result.nickName()).isEqualTo(member.getNickname());
	}

	@Test
	@DisplayName("회원 정보 조회 실패 - 존재하지 않는 회원")
	void 회원_정보_조회_실패_존재하지_않는_회원() {
		// given
		given(memberRepository.findById(999L)).willReturn(Optional.empty());

		// when & then
		assertThatThrownBy(() -> memberService.getInfo(999L))
			.isInstanceOf(CustomException.class)
			.hasFieldOrPropertyWithValue("errorCode", ErrorCode.MEMBER_NOT_FOUND);
	}

	@Test
	@DisplayName("회원 정보 수정 성공 - 이름과 닉네임 모두 변경")
	void 회원_정보_수정_성공() {
		// given
		Member member = MemberFixture.member(1L);
		MemberUpdateRequest request = new MemberUpdateRequest("새이름", "newnickname");
		given(memberRepository.findById(1L)).willReturn(Optional.of(member));
		given(memberRepository.existsByNickname("newnickname")).willReturn(false);

		// when
		MemberInfo result = memberService.updateInfo(1L, request);

		// then
		assertThat(result.name()).isEqualTo("새이름");
		assertThat(result.nickName()).isEqualTo("newnickname");
	}

	@Test
	@DisplayName("회원 정보 수정 성공 - 닉네임 동일하면 중복 체크 스킵")
	void 회원_정보_수정_성공_닉네임_동일() {
		// given
		Member member = MemberFixture.member(1L);
		String sameNickname = member.getNickname();
		MemberUpdateRequest request = new MemberUpdateRequest("새이름", sameNickname);
		given(memberRepository.findById(1L)).willReturn(Optional.of(member));

		// when
		MemberInfo result = memberService.updateInfo(1L, request);

		// then
		assertThat(result.name()).isEqualTo("새이름");
		then(memberRepository).should(never()).existsByNickname(any());
	}

	@Test
	@DisplayName("회원 정보 수정 실패 - 닉네임 중복")
	void 회원_정보_수정_실패_닉네임_중복() {
		// given
		Member member = MemberFixture.member(1L);
		MemberUpdateRequest request = new MemberUpdateRequest(null, "dupnick");
		given(memberRepository.findById(1L)).willReturn(Optional.of(member));
		given(memberRepository.existsByNickname("dupnick")).willReturn(true);

		// when & then
		assertThatThrownBy(() -> memberService.updateInfo(1L, request))
			.isInstanceOf(CustomException.class)
			.hasFieldOrPropertyWithValue("errorCode", ErrorCode.USERNAME_DUPLICATE);
	}

	@Test
	@DisplayName("비밀번호 변경 성공")
	void 비밀번호_변경_성공() {
		// given
		Member member = MemberFixture.member(1L);
		PasswordUpdateRequest request = new PasswordUpdateRequest("currentPassword", "newPassword123");
		given(memberRepository.findById(1L)).willReturn(Optional.of(member));
		given(passwordEncoder.matches("currentPassword", member.getPassword())).willReturn(true);
		given(passwordEncoder.matches("newPassword123", member.getPassword())).willReturn(false);
		given(passwordEncoder.encode("newPassword123")).willReturn("encoded_new_password");

		// when & then
		assertThatNoException().isThrownBy(() -> memberService.updatePassword(1L, request));
	}

	@Test
	@DisplayName("비밀번호 변경 실패 - 현재 비밀번호 불일치")
	void 비밀번호_변경_실패_현재_비밀번호_불일치() {
		// given
		Member member = MemberFixture.member(1L);
		PasswordUpdateRequest request = new PasswordUpdateRequest("wrongPassword", "newPassword123");
		given(memberRepository.findById(1L)).willReturn(Optional.of(member));
		given(passwordEncoder.matches("wrongPassword", member.getPassword())).willReturn(false);

		// when & then
		assertThatThrownBy(() -> memberService.updatePassword(1L, request))
			.isInstanceOf(CustomException.class)
			.hasFieldOrPropertyWithValue("errorCode", ErrorCode.INVALID_PASSWORD);
	}

	@Test
	@DisplayName("비밀번호 변경 실패 - 새 비밀번호가 기존과 동일")
	void 비밀번호_변경_실패_새_비밀번호_동일() {
		// given
		Member member = MemberFixture.member(1L);
		PasswordUpdateRequest request = new PasswordUpdateRequest("currentPassword", "currentPassword");
		given(memberRepository.findById(1L)).willReturn(Optional.of(member));
		given(passwordEncoder.matches("currentPassword", member.getPassword())).willReturn(true);
		given(passwordEncoder.matches("currentPassword", member.getPassword())).willReturn(true);

		// when & then
		assertThatThrownBy(() -> memberService.updatePassword(1L, request))
			.isInstanceOf(CustomException.class)
			.hasFieldOrPropertyWithValue("errorCode", ErrorCode.SAME_AS_OLD_PASSWORD);
	}
}
