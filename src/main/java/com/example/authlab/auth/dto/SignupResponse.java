package com.example.authlab.auth.dto;

public record SignupResponse(
	Long userId,
	String status,
	String message
) {
}
