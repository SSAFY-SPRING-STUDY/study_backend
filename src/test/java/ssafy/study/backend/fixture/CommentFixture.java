package ssafy.study.backend.fixture;

import org.springframework.test.util.ReflectionTestUtils;

import ssafy.study.backend.domain.edu.comment.entity.Comment;
import ssafy.study.backend.domain.edu.comment.entity.ReComment;
import ssafy.study.backend.domain.edu.post.entity.Post;
import ssafy.study.backend.domain.member.entity.Member;

public class CommentFixture {

	public static Comment comment(Member author, Post post) {
		return Comment.create("테스트 댓글 내용", author, post);
	}

	public static Comment comment(Long id, Member author, Post post) {
		Comment comment = Comment.create("테스트 댓글 내용", author, post);
		ReflectionTestUtils.setField(comment, "id", id);
		return comment;
	}

	public static ReComment reComment(Member author, Comment comment) {
		return ReComment.create("테스트 대댓글 내용", author, comment);
	}

	public static ReComment reComment(Long id, Member author, Comment comment) {
		ReComment reComment = ReComment.create("테스트 대댓글 내용", author, comment);
		ReflectionTestUtils.setField(reComment, "id", id);
		return reComment;
	}
}
