package ssafy.study.backend.domain.study.curriculum.entity;

import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Version;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import ssafy.study.backend.domain.study.post.entity.Post;

@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Curriculum {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false)
	@Enumerated(EnumType.STRING)
	private DifficultyLevel level; // BASIC, INTERMEDIATE, ADVANCED

	@Column(nullable = false)
	private String name; // 커리큘럼 이름

	@Column(nullable = false)
	private String description; // 커리큘럼 설명

	@Column(nullable = false, unique = true)
	private int orderInStudy; // 스터디 내에서의 순서

	@Column(nullable = false)
	private int postsCount; // 이 커리큘럼에 속한 글의 총 개수(반정규화)

	@Version
	private Long version; // 낙관적 락을 위한 버전 필드

	@OneToMany(mappedBy = "curriculum", cascade = CascadeType.ALL)
	private List<Post> posts; // 이 커리큘럼에 속한 글

	@Builder
	private Curriculum(DifficultyLevel level, String name, String description, int orderInStudy) {
		this.level = level;
		this.name = name;
		this.description = description;
		this.orderInStudy = orderInStudy;
		this.postsCount = 0;
	}

	public void update(String name, String description, Integer order) {
		this.name = name;
		this.description = description;
		this.orderInStudy = order;
	}
}
