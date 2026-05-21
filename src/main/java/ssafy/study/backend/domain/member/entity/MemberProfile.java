package ssafy.study.backend.domain.member.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "member_profile")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MemberProfile {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "member_id", nullable = false, unique = true)
	private Member member;

	@Column(length = 500)
	private String description;

	@Column
	private String profileImageKey;

	public static MemberProfile createDefault(Member member) {
		MemberProfile profile = new MemberProfile();
		profile.member = member;
		return profile;
	}

	public void updateDescription(String description) {
		this.description = description;
	}

	public void updateProfileImageKey(String key) {
		this.profileImageKey = key;
	}

	public void deleteProfileImage() {
		this.profileImageKey = null;
	}
}
