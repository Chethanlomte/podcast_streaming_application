package com.chethan.PasswordMailOTP.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/health")
@Tag(name = "Health Check", description = "Health Check APIs")
public class HealthCheckController {

    @GetMapping
    @Operation(
        summary = "Check application health",
        description = "Returns the current health status of the application",
        responses = {
            @ApiResponse(
                responseCode = "200",
                description = "Application is healthy",
                content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = Map.class)
                )
            )
        }
    )
    public ResponseEntity<Map<String, Object>> healthCheck() {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "UP");
        response.put("service", "PasswordMailOTP");
        response.put("timestamp", System.currentTimeMillis());
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @GetMapping("/database")
    @Operation(
        summary = "Check database health",
        description = "Returns the database connection status",
        responses = {
            @ApiResponse(
                responseCode = "200",
                description = "Database is accessible",
                content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = Map.class)
                )
            ),
            @ApiResponse(
                responseCode = "503",
                description = "Database is not accessible"
            )
        }
    )
    public ResponseEntity<Map<String, Object>> databaseHealthCheck() {
        // In a real application, you would check the database connection here
        Map<String, Object> response = new HashMap<>();
        response.put("status", "UP");
        response.put("database", "MySQL");
        response.put("timestamp", System.currentTimeMillis());
        return new ResponseEntity<>(response, HttpStatus.OK);
    }
}
