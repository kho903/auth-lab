package com.example.authlab.auth.service;

import com.example.authlab.user.entity.User;
import com.example.authlab.user.entity.UserStatus;
import com.example.authlab.user.repository.UserRepository;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.http.HttpStatus;
import org.springframework.mail.MailException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.SecureRandom;
import java.time.Clock;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.HexFormat;
import java.util.List;
import java.util.Objects;

@Service
public class EmailVerificationService {

	private static final Duration CODE_TTL =
		Duration.ofMinutes(5);

	private static final Duration RESEND_COOLDOWN =
		Duration.ofSeconds(60);

	private static final SecureRandom RANDOM =
		new SecureRandom();

	private static final RedisScript<Long> VERIFY_SCRIPT =
		new DefaultRedisScript<>(
			"""
				local expected = redis.call('GET', KEYS[1])
				
				if not expected then
					return -1
				end
				
				local attempts = tonumber(
					redis.call('GET', KEYS[2]) or '0'
				)
				
				if attempts >= 5 then
					return -2
				end
				
				if expected ~= ARGV[1] then
					local count = redis.call('INCR', KEYS[2])
				
					if count == 1 then
						local ttl = redis.call('PTTL', KEYS[1])
				
						if ttl > 0 then
							redis.call('PEXPIRE', KEYS[2], ttl)
						end
					end
				
					if count >= 5 then
						redis.call('DEL', KEYS[1], KEYS[2])
						return -2
					end
				
					return 0
				end
				
				redis.call('DEL', KEYS[1], KEYS[2])
				return 1
				""",
			Long.class
		);

	private final UserRepository userRepository;
	private final StringRedisTemplate redisTemplate;
	private final VerificationMailSender mailSender;
	private final byte[] hmacKey;

	public EmailVerificationService(
		UserRepository userRepository,
		StringRedisTemplate redisTemplate,
		VerificationMailSender mailSender,
		@Value("${app.email-verification.hmac-secret}")
		String secretHex
	) {
		this.userRepository = userRepository;
		this.redisTemplate = redisTemplate;
		this.mailSender = mailSender;

		this.hmacKey = HexFormat.of().parseHex(secretHex);

		if (hmacKey.length < 32) {
			throw new IllegalArgumentException(
				"EMAIL_CODE_HMAC_KEY는 32바이트 이상이어야 합니다."
			);
		}
	}

	// 회원가입 직후 인증번호 발송
	public void issueForNewUser(Long userId) {

		User user = userRepository.findById(userId)
			.orElseThrow();

		issue(user);
	}

	// 인증번호 재발송
	public void resend(String requestedEmail) {

		String email = SignupService.normalizeEmail(
			requestedEmail
		);

		userRepository.findByEmail(email)
			.filter(user ->
				user.getStatus() == UserStatus.PENDING
			)
			.ifPresent(this::issue);
	}

	private void issue(User user) {

		if (user.getStatus() != UserStatus.PENDING) {
			return;
		}

		Long userId = user.getId();

		String codeKey = codeKey(userId);
		String attemptsKey = attemptsKey(userId);
		String cooldownKey = cooldownKey(userId);

		Boolean acquired = redisTemplate.opsForValue()
			.setIfAbsent(
				cooldownKey,
				"1",
				RESEND_COOLDOWN
			);

		// 재발송 제한 중이면 추가 메일을 보내지 않음
		if (!Boolean.TRUE.equals(acquired)) {
			return;
		}

		String code = String.format(
			"%06d",
			RANDOM.nextInt(1_000_000)
		);

		String codeHash = hash(userId, code);

		redisTemplate.opsForValue().set(
			codeKey,
			codeHash,
			CODE_TTL
		);

		redisTemplate.delete(attemptsKey);

		try {
			mailSender.send(user.getEmail(), code);
		} catch (MailException exception) {

			// 이메일 발송 실패 시 재시도할 수 있도록 정리
			redisTemplate.delete(
				List.of(codeKey, attemptsKey, cooldownKey)
			);

			throw new ResponseStatusException(
				HttpStatus.SERVICE_UNAVAILABLE,
				"이메일 발송에 실패했습니다. 다시 요청해 주세요.",
				exception
			);
		}
	}

	@Transactional
	public void confirm(String requestedEmail, String code) {

		String email = SignupService.normalizeEmail(
			requestedEmail
		);

		User user = userRepository.findByEmail(email)
			.filter(found ->
				found.getStatus() == UserStatus.PENDING
			)
			.orElseThrow(() -> invalidCode());

		Long userId = user.getId();

		Long result = redisTemplate.execute(
			VERIFY_SCRIPT,
			List.of(
				codeKey(userId),
				attemptsKey(userId)
			),
			hash(userId, code)
		);

		if (!Objects.equals(result, 1L)) {
			throw invalidCode();
		}

		user.verifyEmail(
			LocalDateTime.now(Clock.systemUTC())
		);
	}

	private String hash(Long userId, String code) {
		try {
			Mac mac = Mac.getInstance("HmacSHA256");

			mac.init(
				new SecretKeySpec(hmacKey, "HmacSHA256")
			);

			byte[] message = (
				userId + ":" + code
			).getBytes(StandardCharsets.UTF_8);

			return HexFormat.of().formatHex(
				mac.doFinal(message)
			);
		} catch (GeneralSecurityException exception) {
			throw new IllegalStateException(
				"인증번호 해시 생성 실패",
				exception
			);
		}
	}

	private ResponseStatusException invalidCode() {
		return new ResponseStatusException(
			HttpStatus.BAD_REQUEST,
			"인증번호가 잘못되었거나 만료되었습니다."
		);
	}

	private String codeKey(Long userId) {
		return "auth:email:verify:" + userId;
	}

	private String attemptsKey(Long userId) {
		return "auth:email:attempts:" + userId;
	}

	private String cooldownKey(Long userId) {
		return "auth:email:cooldown:" + userId;
	}
}
