package ssafy.study.backend.domain.edu.study.entity;

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

@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Study {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false)
	private String name; // 스터디 이름

	@Column(nullable = false)
	private String description; // 스터디 설명

	@Column(nullable = false)
	@Enumerated(EnumType.STRING)
	private DifficultyLevel level; // BASIC, INTERMEDIATE, ADVANCED

	@Column(nullable = false)
	@Enumerated(EnumType.STRING)
	private StudyType type; // ALGORITHM, BACKEND, COMPUTER_SCIENCE

	@Builder
	private Study(String name, String description, DifficultyLevel level, StudyType type) {
		this.name = name;
		this.description = description;
		this.level = level;
		this.type = type;
	}

	public void update(String name, String description, DifficultyLevel level, StudyType type) {
		this.name = name;
		this.description = description;
		this.level = level;
		this.type = type;
	}
}
