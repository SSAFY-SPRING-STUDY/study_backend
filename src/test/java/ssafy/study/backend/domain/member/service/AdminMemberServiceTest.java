package ssafy.study.backend.domain.member.service;

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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.util.ReflectionTestUtils;

import ssafy.study.backend.domain.member.controller.dto.request.AdminMemberUpdateRequest;
import ssafy.study.backend.domain.member.controller.dto.response.MemberInfo;
import ssafy.study.backend.domain.member.entity.Member;
import ssafy.study.backend.domain.member.entity.MemberLevel;
import ssafy.study.backend.domain.member.entity.MemberRole;
import ssafy.study.backend.domain.member.repository.MemberRepository;
import ssafy.study.backend.fixture.MemberFixture;
import ssafy.study.backend.global.exception.CustomException;
import ssafy.study.backend.global.exception.error.ErrorCode;

@ExtendWith(MockitoExtension.class)
class AdminMemberServiceTest {

	@InjectMocks
	private AdminMemberService adminMemberService;

	@Mock
	private MemberRepository memberRepository;

	// ──────────────────────────────────────────────
	// getMembers
	// ──────────────────────────────────────────────

	@Test
	@DisplayName("회원 목록 조회 성공 - 키워드 없음 (전체 조회)")
	void 회원_목록_조회_성공_전체() {
		// given
		Pageable pageable = PageRequest.of(0, 20);
		Member member = MemberFixture.member(1L);
		Page<Member> page = new PageImpl<>(List.of(member), pageable, 1);
		given(memberRepository.searchByKeyword(null, pageable)).willReturn(page);

		// when
		Page<MemberInfo> result = adminMemberService.getMembers(null, pageable);

		// then
		assertThat(result.getTotalElements()).isEqualTo(1);
		assertThat(result.getContent().get(0).id()).isEqualTo(1L);
	}

	@Test
	@DisplayName("회원 목록 조회 성공 - 빈 키워드는 전체 조회로 처리")
	void 회원_목록_조회_성공_빈_키워드() {
		// given
		Pageable pageable = PageRequest.of(0, 20);
		Page<Member> page = new PageImpl<>(List.of(), pageable, 0);
		given(memberRepository.searchByKeyword(null, pageable)).willReturn(page);

		// when
		Page<MemberInfo> result = adminMemberService.getMembers("   ", pageable);

		// then
		assertThat(result.getTotalElements()).isEqualTo(0);
		then(memberRepository).should().searchByKeyword(null, pageable);
	}

	@Test
	@DisplayName("회원 목록 조회 성공 - 키워드로 검색")
	void 회원_목록_조회_성공_키워드() {
		// given
		Pageable pageable = PageRequest.of(0, 20);
		Member member = MemberFixture.member(1L);
		Page<Member> page = new PageImpl<>(List.of(member), pageable, 1);
		given(memberRepository.searchByKeyword("test", pageable)).willReturn(page);

		// when
		Page<MemberInfo> result = adminMemberService.getMembers("test", pageable);

		// then
		assertThat(result.getTotalElements()).isEqualTo(1);
		assertThat(result.getContent().get(0).email()).isEqualTo(member.getEmail());
	}

	// ──────────────────────────────────────────────
	// getMember
	// ──────────────────────────────────────────────

	@Test
	@DisplayName("회원 상세 조회 성공")
	void 회원_상세_조회_성공() {
		// given
		Member member = MemberFixture.member(1L);
		given(memberRepository.findById(1L)).willReturn(Optional.of(member));

		// when
		MemberInfo result = adminMemberService.getMember(1L);

		// then
		assertThat(result.id()).isEqualTo(1L);
		assertThat(result.email()).isEqualTo(member.getEmail());
		assertThat(result.role()).isEqualTo(MemberRole.ROLE_USER);
		assertThat(result.level()).isEqualTo(MemberLevel.BASIC);
	}

	@Test
	@DisplayName("회원 상세 조회 실패 - 존재하지 않는 회원")
	void 회원_상세_조회_실패_없는_회원() {
		// given
		given(memberRepository.findById(99L)).willReturn(Optional.empty());

		// when & then
		assertThatThrownBy(() -> adminMemberService.getMember(99L))
			.isInstanceOf(CustomException.class)
			.hasFieldOrPropertyWithValue("errorCode", ErrorCode.MEMBER_NOT_FOUND);
	}

	// ──────────────────────────────────────────────
	// updateMember
	// ──────────────────────────────────────────────

	@Test
	@DisplayName("회원 정보 수정 성공 - 이름 변경")
	void 회원_수정_성공_이름() {
		// given
		Member member = MemberFixture.member(1L);
		given(memberRepository.findById(1L)).willReturn(Optional.of(member));
		AdminMemberUpdateRequest request = new AdminMemberUpdateRequest("새이름", null, null, null);

		// when
		MemberInfo result = adminMemberService.updateMember(1L, request);

		// then
		assertThat(result.name()).isEqualTo("새이름");
	}

	@Test
	@DisplayName("회원 정보 수정 성공 - 닉네임 변경")
	void 회원_수정_성공_닉네임() {
		// given
		Member member = MemberFixture.member(1L);
		given(memberRepository.findById(1L)).willReturn(Optional.of(member));
		given(memberRepository.existsByNickname("newnick")).willReturn(false);
		AdminMemberUpdateRequest request = new AdminMemberUpdateRequest(null, "newnick", null, null);

		// when
		MemberInfo result = adminMemberService.updateMember(1L, request);

		// then
		assertThat(result.nickName()).isEqualTo("newnick");
	}

	@Test
	@DisplayName("회원 정보 수정 성공 - 역할 변경")
	void 회원_수정_성공_역할() {
		// given
		Member member = MemberFixture.member(1L);
		given(memberRepository.findById(1L)).willReturn(Optional.of(member));
		AdminMemberUpdateRequest request = new AdminMemberUpdateRequest(null, null, MemberRole.ROLE_ADMIN, null);

		// when
		MemberInfo result = adminMemberService.updateMember(1L, request);

		// then
		assertThat(result.role()).isEqualTo(MemberRole.ROLE_ADMIN);
	}

	@Test
	@DisplayName("회원 정보 수정 성공 - 레벨 변경")
	void 회원_수정_성공_레벨() {
		// given
		Member member = MemberFixture.member(1L);
		given(memberRepository.findById(1L)).willReturn(Optional.of(member));
		AdminMemberUpdateRequest request = new AdminMemberUpdateRequest(null, null, null, MemberLevel.ADVANCED);

		// when
		MemberInfo result = adminMemberService.updateMember(1L, request);

		// then
		assertThat(result.level()).isEqualTo(MemberLevel.ADVANCED);
	}

	@Test
	@DisplayName("회원 정보 수정 성공 - 모든 필드 변경")
	void 회원_수정_성공_전체() {
		// given
		Member member = MemberFixture.member(1L);
		given(memberRepository.findById(1L)).willReturn(Optional.of(member));
		given(memberRepository.existsByNickname("newnick")).willReturn(false);
		AdminMemberUpdateRequest request = new AdminMemberUpdateRequest("새이름", "newnick", MemberRole.ROLE_ADMIN, MemberLevel.INTERMEDIATE);

		// when
		MemberInfo result = adminMemberService.updateMember(1L, request);

		// then
		assertThat(result.name()).isEqualTo("새이름");
		assertThat(result.nickName()).isEqualTo("newnick");
		assertThat(result.role()).isEqualTo(MemberRole.ROLE_ADMIN);
		assertThat(result.level()).isEqualTo(MemberLevel.INTERMEDIATE);
	}

	@Test
	@DisplayName("회원 정보 수정 성공 - 동일 닉네임이면 중복 체크 스킵")
	void 회원_수정_성공_동일_닉네임() {
		// given
		Member member = MemberFixture.member(1L);
		String sameNickname = member.getNickname();
		given(memberRepository.findById(1L)).willReturn(Optional.of(member));
		AdminMemberUpdateRequest request = new AdminMemberUpdateRequest(null, sameNickname, null, null);

		// when
		adminMemberService.updateMember(1L, request);

		// then
		then(memberRepository).should(never()).existsByNickname(any());
	}

	@Test
	@DisplayName("회원 정보 수정 실패 - 존재하지 않는 회원")
	void 회원_수정_실패_없는_회원() {
		// given
		given(memberRepository.findById(99L)).willReturn(Optional.empty());
		AdminMemberUpdateRequest request = new AdminMemberUpdateRequest("이름", null, null, null);

		// when & then
		assertThatThrownBy(() -> adminMemberService.updateMember(99L, request))
			.isInstanceOf(CustomException.class)
			.hasFieldOrPropertyWithValue("errorCode", ErrorCode.MEMBER_NOT_FOUND);
	}

	@Test
	@DisplayName("회원 정보 수정 실패 - 닉네임 중복")
	void 회원_수정_실패_닉네임_중복() {
		// given
		Member member = MemberFixture.member(1L);
		given(memberRepository.findById(1L)).willReturn(Optional.of(member));
		given(memberRepository.existsByNickname("dupnick")).willReturn(true);
		AdminMemberUpdateRequest request = new AdminMemberUpdateRequest(null, "dupnick", null, null);

		// when & then
		assertThatThrownBy(() -> adminMemberService.updateMember(1L, request))
			.isInstanceOf(CustomException.class)
			.hasFieldOrPropertyWithValue("errorCode", ErrorCode.USERNAME_DUPLICATE);
	}

	@Test
	@DisplayName("회원 정보 수정 성공 - null 필드는 기존값 유지")
	void 회원_수정_성공_null_필드_유지() {
		// given
		Member member = MemberFixture.member(1L);
		String originalName = member.getName();
		String originalNickname = member.getNickname();
		given(memberRepository.findById(1L)).willReturn(Optional.of(member));
		AdminMemberUpdateRequest request = new AdminMemberUpdateRequest(null, null, null, null);

		// when
		MemberInfo result = adminMemberService.updateMember(1L, request);

		// then
		assertThat(result.name()).isEqualTo(originalName);
		assertThat(result.nickName()).isEqualTo(originalNickname);
		assertThat(result.role()).isEqualTo(MemberRole.ROLE_USER);
		assertThat(result.level()).isEqualTo(MemberLevel.BASIC);
	}
}
