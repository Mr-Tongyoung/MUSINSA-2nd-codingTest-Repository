package com.example.courseRegistrationSystem.controller;

import com.example.courseRegistrationSystem.dto.CourseResponse;
import com.example.courseRegistrationSystem.service.CourseService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "강좌", description = "강좌 관련 API")
@RestController
@RequestMapping("/courses")
@RequiredArgsConstructor
public class CourseController {

    private final CourseService courseService;

    @Operation(summary = "강좌 목록 조회", description = "전체 강좌 목록을 조회합니다. departmentId를 지정하면 해당 학과의 강좌만 필터링합니다.")
    @ApiResponse(responseCode = "200", description = "조회 성공")
    @GetMapping
    public ResponseEntity<List<CourseResponse>> getCourses(
            @Parameter(description = "학과 ID (선택, 미입력 시 전체 조회)")
            @RequestParam(required = false) Long departmentId) {
        if (departmentId != null) {
            return ResponseEntity.ok(courseService.findByDepartmentId(departmentId));
        }
        return ResponseEntity.ok(courseService.findAll());
    }
}
