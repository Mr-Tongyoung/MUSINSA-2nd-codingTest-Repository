package com.example.courseRegistrationSystem.controller.docs;

import com.example.courseRegistrationSystem.dto.CourseResponse;
import com.example.courseRegistrationSystem.dto.ErrorResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;

import java.util.List;

@Tag(name = "강좌", description = "강좌 관련 API")
public interface CourseControllerDocs {

    @Operation(summary = "강좌 목록 조회", description = "전체 강좌 목록을 조회합니다. departmentId를 지정하면 해당 학과의 강좌만 필터링합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "404", description = "존재하지 않는 학과 ID",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    ResponseEntity<List<CourseResponse>> getCourses(
            @Parameter(description = "학과 ID (선택, 미입력 시 전체 조회)") Long departmentId);
}
