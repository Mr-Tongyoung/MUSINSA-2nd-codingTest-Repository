package com.example.courseRegistrationSystem.controller.docs;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;

@Tag(name = "헬스체크", description = "서버 상태 확인 API")
public interface HealthControllerDocs {

    @Operation(summary = "헬스체크", description = "서버 정상 구동 여부를 확인합니다.")
    @ApiResponse(responseCode = "200", description = "서버 정상")
    ResponseEntity<Void> health();
}
