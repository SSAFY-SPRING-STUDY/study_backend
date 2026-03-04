package ssafy.study.backend.domain.study.post.entity;

import java.time.LocalDateTime;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

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
import ssafy.study.backend.domain.member.entity.Member;
import ssafy.study.backend.domain.study.curriculum.entity.Curriculum;

@Getter
@Entity
@EntityListeners(AuditingEntityListener.class)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Post {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	/** 게시글 제목 */
	@Column(nullable = false)
	private String title;

	/** 게시글 내용 (마크다운 지원) */
	@Lob
	@Column(nullable = false, columnDefinition = "LONGTEXT")
	private String content;

	/** 작성자 */
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "author_id")
	private Member author;

	/** 커리큘럼 내 순서 */
	@Column(nullable = false)
	private int orderInCurriculum;

	/** 속한 커리큘럼 */
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "curriculum_id")
	private Curriculum curriculum;

	/** 생성일시 */
	@CreatedDate
	@Column(nullable = false, updatable = false)
	private LocalDateTime createdAt;

	/** 수정일시 */
	@LastModifiedDate
	@Column(nullable = false)
	private LocalDateTime updatedAt;


	@Builder
	private Post(String title, String content, Member author, int orderInCurriculum, Curriculum curriculum) {
		this.title = title;
		this.content = content;
		this.author = author;
		this.orderInCurriculum = orderInCurriculum;
		this.curriculum = curriculum;
	}

	public void update(String title, String content) {
		this.title = title;
		this.content = content;
	}

	public boolean isAuthor(Long memberId) {
		return this.author.getId().equals(memberId);
	}



}
