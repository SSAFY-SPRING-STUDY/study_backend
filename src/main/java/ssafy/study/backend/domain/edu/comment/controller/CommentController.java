package ssafy.study.backend.domain.edu.comment;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;
import ssafy.study.backend.domain.edu.comment.service.CommentService;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/comments")
public class CommentController {
	private final CommentService commentService;


}
