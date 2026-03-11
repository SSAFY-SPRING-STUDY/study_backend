package ssafy.study.backend.domain.edu.curriculum.entity;

import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Version;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import ssafy.study.backend.domain.edu.post.entity.Post;
import ssafy.study.backend.domain.edu.study.entity.Study;


@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Curriculum {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false)
	private String name; // 커리큘럼 이름

	@Column(nullable = false)
	private String description; // 커리큘럼 설명

	@Column(nullable = false)
	private int orderInStudy; // 스터디 내에서의 순서

	@Column(nullable = false)
	private int postsCount; // 이 커리큘럼에 속한 글의 총 개수(반정규화)

	@Version
	private Long version; // 낙관적 락을 위한 버전 필드

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(nullable = false)
	private Study study; // 이 커리큘럼이 속한 스터디

	@OneToMany(mappedBy = "curriculum", cascade = CascadeType.ALL)
	private List<Post> posts; // 이 커리큘럼에 속한 글

	@Builder
	private Curriculum(String name, String description, int orderInStudy, Study study) {
		this.name = name;
		this.description = description;
		this.orderInStudy = orderInStudy;
		this.study = study;
		this.postsCount = 0;
	}

	public void update(String name, String description, Integer order) {
		this.name = name;
		this.description = description;
		this.orderInStudy = order;
	}

	public int incrementPostsCount() {
		this.postsCount++;
		return this.postsCount;
	}
}
