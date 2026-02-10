package com.example.courseRegistrationSystem.controller.docs;

import com.example.courseRegistrationSystem.dto.EnrollmentRequest;
import com.example.courseRegistrationSystem.dto.EnrollmentResponse;
import com.example.courseRegistrationSystem.dto.ErrorResponse;
import com.example.courseRegistrationSystem.dto.TimetableResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;

import java.util.Map;

@Tag(name = "수강신청", description = "수강신청/취소 및 시간표 조회 API")
public interface EnrollmentControllerDocs {

    @Operation(summary = "수강신청", description = "학생이 강좌에 수강신청합니다. 정원 초과, 학점 초과, 시간 충돌, 중복 신청 시 실패합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "수강신청 성공"),
            @ApiResponse(responseCode = "404", description = "학생 또는 강좌를 찾을 수 없음",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "400", description = "요청 데이터 누락 (studentId, courseId 필수)",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "409", description = "정원 초과 / 학점 초과 / 시간 충돌 / 중복 신청",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    ResponseEntity<EnrollmentResponse> enroll(EnrollmentRequest request);

    @Operation(summary = "수강취소", description = "수강신청을 취소합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "수강취소 성공"),
            @ApiResponse(responseCode = "404", description = "수강 기록을 찾을 수 없음",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    ResponseEntity<Map<String, String>> cancel(@Parameter(description = "수강신청 ID") Long enrollmentId);

    @Operation(summary = "내 시간표 조회 (학생 ID)", description = "학생 ID로 이번 학기 시간표를 조회합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "404", description = "학생을 찾을 수 없음",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    ResponseEntity<TimetableResponse> getTimetable(@Parameter(description = "학생 ID") Long studentId);

    @Operation(summary = "내 시간표 조회 (학번)", description = "학번으로 이번 학기 시간표를 조회합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "404", description = "해당 학번의 학생을 찾을 수 없음",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    ResponseEntity<TimetableResponse> getTimetableByStudentNumber(@Parameter(description = "학번 (예: 202300001)") String studentNumber);
}
