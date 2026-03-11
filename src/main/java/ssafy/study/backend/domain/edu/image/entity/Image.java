package ssafy.study.backend.domain.edu.image.entity;

import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
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
import ssafy.study.backend.domain.edu.post.entity.Post;

@Getter
@Entity
@EntityListeners(AuditingEntityListener.class)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Image {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(nullable = false)
	private Post post;

	@Column(nullable = false)
	private String key;

	@Column(nullable = false)
	private ImageStatus status; // PENDING, UPLOADED, DELETED

	public void markAsUploaded() {
		this.status = ImageStatus.UPLOADED;
	}

	public void markAsDeleted() {
		this.status = ImageStatus.DELETED;
	}

	@Builder(access = AccessLevel.PRIVATE)
	private Image(Post post, String key, ImageStatus status) {
		this.post = post;
		this.key = key;
		this.status = status;
	}

	public static Image create(Post post, String key) {
		return Image.builder()
			.post(post)
			.key(key)
			.status(ImageStatus.PENDING)
			.build();
	}
}
