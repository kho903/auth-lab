
package com.example.authlab.user;

import com.example.authlab.user.entity.*;
import com.example.authlab.user.repository.*;

import jakarta.persistence.EntityManager;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class AuthEntityMappingTest {

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private LocalCredentialRepository localCredentialRepository;

	@Autowired
	private OAuthAccountRepository oauthAccountRepository;

	@Autowired
	private EntityManager entityManager;

	@Test
	void shouldSaveAndFindAuthenticationEntities() {

		// 1. 일반 회원 생성
		String email = "test-" + UUID.randomUUID()
			+ "@example.com";

		User user = User.registerLocal(email, "테스트 사용자");

		user = userRepository.saveAndFlush(user);

		Long userId = user.getId();

		assertNotNull(userId);
		assertEquals(UserStatus.PENDING, user.getStatus());

		// 2. 이메일 인증 완료
		user.verifyEmail(
			LocalDateTime.now(Clock.systemUTC())
		);

		// 3. 일반 로그인 정보 저장
		LocalCredential credential = new LocalCredential(
			user,
			"test-only-password-hash"
		);

		localCredentialRepository.saveAndFlush(credential);

		// 4. 다른 이메일의 Google 계정 연동
		String googleSub = "google-" + UUID.randomUUID();

		OAuthAccount oauthAccount = new OAuthAccount(
			user,
			OAuthProvider.GOOGLE,
			googleSub,
			"other@example.com"
		);

		oauthAccountRepository.saveAndFlush(oauthAccount);

		// 5. DB에 변경 내용 반영 후
		//    영속성 컨텍스트 비우기
		entityManager.flush();
		entityManager.clear();

		// 6. 실제 DB에서 회원 다시 조회
		User foundUser = userRepository.findByEmail(email)
			.orElseThrow();

		assertEquals(userId, foundUser.getId());
		assertEquals(UserStatus.ACTIVE, foundUser.getStatus());
		assertNotNull(foundUser.getEmailVerifiedAt());

		// 7. 공유 PK 확인
		LocalCredential foundCredential =
			localCredentialRepository.findById(userId)
				.orElseThrow();

		assertEquals(
			userId,
			foundCredential.getUserId()
		);

		// 8. Google 계정 조회
		OAuthAccount foundOAuth =
			oauthAccountRepository
				.findByProviderAndProviderUserId(
					OAuthProvider.GOOGLE,
					googleSub
				)
				.orElseThrow();

		assertEquals(
			userId,
			foundOAuth.getUser().getId()
		);

		assertEquals(
			"other@example.com",
			foundOAuth.getProviderEmail()
		);
	}
}
