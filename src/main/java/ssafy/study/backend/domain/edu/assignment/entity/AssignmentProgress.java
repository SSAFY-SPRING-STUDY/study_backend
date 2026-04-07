package ssafy.study.backend.domain.edu.assignment.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import ssafy.study.backend.domain.edu.study.entity.StudyMember;

@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class AssignmentProgress {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(nullable = false)
	private StudyMember studyMember;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(nullable = false)
	private Assignment assignment;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private AssignmentProgressStatus status;

	private Long githubIssueNumber;

	@Column(nullable = false)
	private LocalDateTime appliedAt;

	private LocalDateTime completedAt;

	@Builder
	private AssignmentProgress(StudyMember studyMember, Assignment assignment) {
		this.studyMember = studyMember;
		this.assignment = assignment;
		this.status = AssignmentProgressStatus.APPLIED;
		this.appliedAt = LocalDateTime.now();
	}

	public void setIssueNumber(Long githubIssueNumber) {
		this.githubIssueNumber = githubIssueNumber;
	}

	public void complete() {
		this.status = AssignmentProgressStatus.COMPLETED;
		this.completedAt = LocalDateTime.now();
	}
}
