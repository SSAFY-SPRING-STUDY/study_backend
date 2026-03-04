package ssafy.study.backend.domain.edu.study.controller;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import ssafy.study.backend.domain.edu.study.controller.dto.request.StudyRequest;
import ssafy.study.backend.domain.edu.study.controller.dto.request.StudyResponse;
import ssafy.study.backend.domain.edu.study.entity.StudyType;
import ssafy.study.backend.domain.edu.study.service.StudyService;
import ssafy.study.backend.global.response.ApiResponse;

@RestController
@RequestMapping("/api/v1/studies")
@RequiredArgsConstructor
@Tag(name="Study", description = "study 관련 API")
public class StudyController {
	private final StudyService studyService;


	@PostMapping
	@Operation(summary = "스터디 생성", description = "새로운 스터디를 생성합니다.")
	@PreAuthorize("hasRole('ADMIN')")
	@ResponseStatus(HttpStatus.CREATED)
	public ApiResponse<StudyResponse> create(@Valid @RequestBody StudyRequest request) {
		StudyResponse response = studyService.createStudy(request);
		return ApiResponse.success("스터디가 성공적으로 생성되었습니다.", response);
	}

	@GetMapping
	@Operation(summary = "타입기반 스터디 조회", description = "특정 타입의 스터디의 정보를 조회합니다.")
	@ResponseStatus(HttpStatus.OK)
	public ApiResponse<Page<StudyResponse>> read(
		@RequestParam StudyType studyType,
		@RequestParam(defaultValue = "0") int page,
		@RequestParam(defaultValue = "10") int size) {
		Page<StudyResponse> response = studyService.getStudiesByType(studyType, PageRequest.of(page, size));
		return ApiResponse.success("스터디가 성공적으로 조회되었습니다.", response);
	}

	@GetMapping("/{studyId}")
	@Operation(summary = "스터디 조회", description = "특정 스터디의 정보를 조회합니다.")
	@ResponseStatus(HttpStatus.OK)
	public ApiResponse<StudyResponse> read(@PathVariable Long studyId) {
		StudyResponse response = studyService.getStudy(studyId);
		return ApiResponse.success("스터디가 성공적으로 조회되었습니다.", response);
	}

	@PutMapping("/{studyId}")
	@Operation(summary = "스터디 수정", description = "특정 스터디를 수정합니다.")
	@PreAuthorize("hasRole('ADMIN')")
	@ResponseStatus(HttpStatus.OK)
	public ApiResponse<StudyResponse> update(@PathVariable Long studyId, @Valid @RequestBody StudyRequest request) {
		StudyResponse response = studyService.updateStudy(studyId, request);
		return ApiResponse.success("스터디가 성공적으로 수정되었습니다.", response);
	}

	@DeleteMapping("/{studyId}")
	@Operation(summary = "스터디 삭제", description = "특정 스터디를 삭제합니다.")
	@PreAuthorize("hasRole('ADMIN')")
	@ResponseStatus(HttpStatus.OK)
	public ApiResponse<Void> delete(@PathVariable Long studyId) {
		studyService.deleteStudy(studyId);
		return ApiResponse.success("스터디가 성공적으로 삭제되었습니다.");
	}
}
