package ssafy.study.backend.domain.member.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Member {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Setter
	@Column(nullable = false)
	private String name;

	@Column(nullable = false, unique = true)
	private String email;

	@Setter
	@Column(nullable = false, unique = true)
	private String nickname;

	@Setter
	@Column
	private String password;

	@Column(nullable = false)
	@Enumerated(EnumType.STRING)
	private MemberRole role;

	@Column(nullable = false)
	@Enumerated(EnumType.STRING)
	private MemberLevel level;

	@Column(unique = true)
	private String githubId;

	@Column
	private String githubUsername;

	@Builder
	private Member(String name, String email, String nickname, String password, MemberRole role, MemberLevel level,
		String githubId, String githubUsername) {
		this.name = name;
		this.email = email;
		this.nickname = nickname;
		this.password = password;
		this.role = role;
		this.level = level;
		this.githubId = githubId;
		this.githubUsername = githubUsername;
	}

	public void updateRole(MemberRole role) {
		this.role = role;
	}

	public void updateLevel(MemberLevel level) {
		this.level = level;
	}

	public void connectGithub(String githubId, String githubUsername) {
		this.githubId = githubId;
		this.githubUsername = githubUsername;
	}

	public boolean isGithubLinked() {
		return this.githubId != null;
	}
}
