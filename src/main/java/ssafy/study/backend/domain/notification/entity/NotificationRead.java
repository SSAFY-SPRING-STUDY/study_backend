package ssafy.study.backend.domain.notification.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(uniqueConstraints = @UniqueConstraint(columnNames = {"notification_id", "member_id"}))
public class NotificationRead {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false)
	private Long notificationId;

	@Column(nullable = false)
	private Long memberId;

	@Builder
	private NotificationRead(Long notificationId, Long memberId) {
		this.notificationId = notificationId;
		this.memberId = memberId;
	}

	public static NotificationRead create(Long notificationId, Long memberId) {
		return NotificationRead.builder()
			.notificationId(notificationId)
			.memberId(memberId)
			.build();
	}
}
