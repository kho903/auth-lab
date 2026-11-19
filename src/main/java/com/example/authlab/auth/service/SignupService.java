package com.example.authlab.auth.service;

import com.example.authlab.auth.dto.SignupRequest;
import com.example.authlab.user.entity.LocalCredential;
import com.example.authlab.user.entity.User;
import com.example.authlab.user.repository.LocalCredentialRepository;
import com.example.authlab.user.repository.UserRepository;

import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.nio.charset.StandardCharsets;
import java.util.Locale;

@Service
public class SignupService {

	private final UserRepository userRepository;
	private final LocalCredentialRepository credentialRepository;
	private final PasswordEncoder passwordEncoder;

	public SignupService(
		UserRepository userRepository,
		LocalCredentialRepository credentialRepository,
		PasswordEncoder passwordEncoder
	) {
		this.userRepository = userRepository;
		this.credentialRepository = credentialRepository;
		this.passwordEncoder = passwordEncoder;
	}

	@Transactional
	public Long register(SignupRequest request) {

		String email = normalizeEmail(request.email());

		if (userRepository.existsByEmail(email)) {
			throw new ResponseStatusException(
				HttpStatus.CONFLICT,
				"이미 등록된 이메일입니다."
			);
		}

		if (request.password()
			.getBytes(StandardCharsets.UTF_8).length > 72) {
			throw new ResponseStatusException(
				HttpStatus.BAD_REQUEST,
				"비밀번호는 UTF-8 기준 72바이트 이하여야 합니다."
			);
		}

		String passwordHash =
			passwordEncoder.encode(request.password());

		User user = User.registerLocal(
			email,
			request.name().strip()
		);

		userRepository.save(user);

		credentialRepository.save(
			new LocalCredential(user, passwordHash)
		);

		return user.getId();
	}

	public static String normalizeEmail(String email) {
		return email.strip().toLowerCase(Locale.ROOT);
	}
}
