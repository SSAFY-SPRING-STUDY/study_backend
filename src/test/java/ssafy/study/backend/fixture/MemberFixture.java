package ssafy.study.backend.fixture;

import org.springframework.test.util.ReflectionTestUtils;

import ssafy.study.backend.domain.member.entity.Member;
import ssafy.study.backend.domain.member.entity.MemberLevel;
import ssafy.study.backend.domain.member.entity.MemberRole;

public class MemberFixture {

	public static Member member() {
		return memberBuilder().build();
	}

	public static Member member(Long id) {
		Member member = memberBuilder().build();
		ReflectionTestUtils.setField(member, "id", id);
		return member;
	}

	public static Member.MemberBuilder memberBuilder() {
		return Member.builder()
			.email("test@example.com")
			.password("encoded_password")
			.name("테스트유저")
			.nickname("testuser")
			.role(MemberRole.ROLE_USER)
			.level(MemberLevel.BASIC);
	}

	/** GitHub 전용 계정 (password = null) */
	public static Member githubOnlyMember(Long id) {
		Member member = Member.builder()
			.email("github@github.noemail")
			.password(null)
			.name("GitHub유저")
			.nickname("ghuser")
			.role(MemberRole.ROLE_USER)
			.level(MemberLevel.BASIC)
			.githubId("gh_id_123")
			.githubUsername("ghuser")
			.build();
		ReflectionTestUtils.setField(member, "id", id);
		return member;
	}

	/** GitHub 연동된 일반 계정 (password 있음) */
	public static Member githubLinkedMember(Long id) {
		Member member = memberBuilder()
			.githubId("gh_id_123")
			.githubUsername("ghuser")
			.build();
		ReflectionTestUtils.setField(member, "id", id);
		return member;
	}
}
