package ssafy.study.backend.fixture;

import org.springframework.test.util.ReflectionTestUtils;

import ssafy.study.backend.domain.edu.image.entity.Image;
import ssafy.study.backend.domain.edu.post.entity.Post;

public class ImageFixture {

	public static Image image(Post post) {
		return Image.create(post, "posts/1/uuid_test.png");
	}

	public static Image image(Long id, Post post) {
		Image image = Image.create(post, "posts/1/uuid_test.png");
		ReflectionTestUtils.setField(image, "id", id);
		return image;
	}
}
