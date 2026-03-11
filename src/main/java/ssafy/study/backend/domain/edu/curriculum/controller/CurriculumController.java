package ssafy.study.backend.domain.edu.curriculum.controller;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import ssafy.study.backend.domain.edu.curriculum.controller.dto.CurriculumRequest;
import ssafy.study.backend.domain.edu.curriculum.controller.dto.CurriculumResponse;
import ssafy.study.backend.domain.edu.curriculum.service.CurriculumService;
import ssafy.study.backend.global.response.ApiResponse;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
@Tag(name="Curriculum", description = "커리큘럼 관련 API")
public class CurriculumController {
	private final CurriculumService curriculumService;

	@GetMapping("/studies/{studyId}/curriculums")
	@Operation(summary = "스터디별 커리큘럼 목록 조회", description = "특정 스터디의 커리큘럼 목록을 페이지 단위로 조회합니다.")
	@ResponseStatus(HttpStatus.OK)
	public ApiResponse<Page<CurriculumResponse>> listByStudy(
		@PathVariable Long studyId,
		@PageableDefault(size = 10, sort = "orderInStudy") Pageable pageable) {
		Page<CurriculumResponse> result = curriculumService.getCurriculumsByStudy(studyId, pageable);
		return ApiResponse.success("커리큘럼 목록이 성공적으로 조회되었습니다.", result);
	}

	@PostMapping("/studies/{studyId}/curriculums")
	@Operation(summary = "커리큘럼 생성", description = "새로운 커리큘럼을 생성합니다.")
	@PreAuthorize("hasRole('ADMIN')")
	@ResponseStatus(HttpStatus.CREATED)
	public ApiResponse<CurriculumResponse> create(@PathVariable Long studyId, @Valid @RequestBody CurriculumRequest request) {
		CurriculumResponse curriculumResponse = curriculumService.createCurriculums(studyId, request);
		return ApiResponse.success("커리큘럼이 성공적으로 생성되었습니다.", curriculumResponse);
	}

	@GetMapping("/curriculums/{curriculumId}")
	@Operation(summary = "커리큘럼 조회", description = "특정 커리큘럼의 정보를 조회합니다.")
	@ResponseStatus(HttpStatus.OK)
	public ApiResponse<CurriculumResponse> read(@PathVariable Long curriculumId) {
		CurriculumResponse curriculumResponse = curriculumService.getCurriculums(curriculumId);
		return ApiResponse.success("커리큘럼이 성공적으로 조회되었습니다.", curriculumResponse);
	}

	@PutMapping("/curriculums/{curriculumId}")
	@Operation(summary = "커리큘럼 수정", description = "특정 커리큘럼을 수정합니다.")
	@PreAuthorize("hasRole('ADMIN')")
	@ResponseStatus(HttpStatus.OK)
	public ApiResponse<CurriculumResponse> update(@PathVariable Long curriculumId, @Valid @RequestBody CurriculumRequest request) {
		CurriculumResponse curriculumResponse = curriculumService.updateCurriculums(curriculumId, request);
		return ApiResponse.success("커리큘럼이 성공적으로 수정되었습니다.", curriculumResponse);
	}

	@DeleteMapping("/curriculums/{curriculumId}")
	@Operation(summary = "커리큘럼 삭제", description = "특정 커리큘럼을 삭제합니다.")
	@PreAuthorize("hasRole('ADMIN')")
	@ResponseStatus(HttpStatus.OK)
	public ApiResponse<Void> delete(@PathVariable Long curriculumId) {
		curriculumService.deleteCurriculums(curriculumId);
		return ApiResponse.success("커리큘럼이 성공적으로 삭제되었습니다.");
	}

}
