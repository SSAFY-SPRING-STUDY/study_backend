package ssafy.study.backend.fixture;

import org.springframework.test.util.ReflectionTestUtils;

import ssafy.study.backend.domain.edu.image.entity.Image;
import ssafy.study.backend.domain.edu.post.entity.Post;

public class ImageFixture {

	public static Image image(Post post) {
		Image image = Image.createPending("images/uuid_test.png");
		image.attachTo(post);
		return image;
	}

	public static Image image(Long id, Post post) {
		Image image = Image.createPending("images/uuid_test.png");
		image.attachTo(post);
		ReflectionTestUtils.setField(image, "id", id);
		return image;
	}
}
