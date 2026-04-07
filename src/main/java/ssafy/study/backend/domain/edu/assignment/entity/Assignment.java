package ssafy.study.backend.domain.edu.assignment.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Lob;
import jakarta.persistence.ManyToOne;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import ssafy.study.backend.domain.edu.study.entity.Study;

@Getter
@Entity
@EntityListeners(AuditingEntityListener.class)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Assignment {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(nullable = false)
	private Study study;

	@Column(nullable = false)
	private String title;

	@Lob
	@Column(nullable = false, columnDefinition = "LONGTEXT")
	private String content;

	@Lob
	@Column(nullable = false, columnDefinition = "LONGTEXT")
	private String prTemplate;

	@Column(nullable = false)
	private int orderInStudy;

	@CreatedDate
	@Column(nullable = false, updatable = false)
	private LocalDateTime createdAt;

	@LastModifiedDate
	@Column(nullable = false)
	private LocalDateTime updatedAt;

	@Builder
	private Assignment(Study study, String title, String content, String prTemplate, int orderInStudy) {
		this.study = study;
		this.title = title;
		this.content = content;
		this.prTemplate = prTemplate;
		this.orderInStudy = orderInStudy;
	}

	public void update(String title, String content, String prTemplate, int orderInStudy) {
		this.title = title;
		this.content = content;
		this.prTemplate = prTemplate;
		this.orderInStudy = orderInStudy;
	}

	public void decrementOrder() {
		this.orderInStudy--;
	}
}
