package com.example.authlab.auth.controller;

import com.example.authlab.auth.dto.*;
import com.example.authlab.auth.service.EmailVerificationService;
import com.example.authlab.auth.service.SignupService;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

	private final SignupService signupService;
	private final EmailVerificationService verificationService;

	public AuthController(
		SignupService signupService,
		EmailVerificationService verificationService
	) {
		this.signupService = signupService;
		this.verificationService = verificationService;
	}

	@PostMapping("/signup")
	public ResponseEntity<SignupResponse> signup(
		@Valid @RequestBody SignupRequest request
	) {
		Long userId = signupService.register(request);

		// 회원 DB 트랜잭션 완료 후 메일 발송
		verificationService.issueForNewUser(userId);

		return ResponseEntity
			.status(HttpStatus.CREATED)
			.body(
				new SignupResponse(
					userId,
					"PENDING",
					"인증번호를 이메일로 발송했습니다."
				)
			);
	}

	@PostMapping("/email-verifications")
	public ResponseEntity<Void> resend(
		@Valid @RequestBody
		EmailVerificationRequest request
	) {
		verificationService.resend(request.email());

		return ResponseEntity.accepted().build();
	}

	@PostMapping("/email-verifications/confirm")
	public ResponseEntity<Void> confirm(
		@Valid @RequestBody
		EmailVerificationConfirmRequest request
	) {
		verificationService.confirm(
			request.email(),
			request.code()
		);

		return ResponseEntity.noContent().build();
	}
}
