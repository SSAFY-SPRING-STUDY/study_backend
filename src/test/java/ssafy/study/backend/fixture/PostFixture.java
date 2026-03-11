package ssafy.study.backend.fixture;

import org.springframework.test.util.ReflectionTestUtils;

import ssafy.study.backend.domain.edu.curriculum.entity.Curriculum;
import ssafy.study.backend.domain.edu.post.entity.Post;
import ssafy.study.backend.domain.member.entity.Member;

public class PostFixture {

	public static Post post(Member author, Curriculum curriculum) {
		return postBuilder(author, curriculum).build();
	}

	public static Post post(Long id, Member author, Curriculum curriculum) {
		Post post = postBuilder(author, curriculum).build();
		ReflectionTestUtils.setField(post, "id", id);
		return post;
	}

	public static Post.PostBuilder postBuilder(Member author, Curriculum curriculum) {
		return Post.builder()
			.title("테스트 게시글")
			.content("테스트 내용")
			.author(author)
			.curriculum(curriculum)
			.orderInCurriculum(1);
	}
}