package com.example.authlab.user.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.authlab.user.entity.OAuthAccount;
import com.example.authlab.user.entity.OAuthProvider;

public interface OAuthAccountRepository extends JpaRepository<OAuthAccount, Long> {

	Optional<OAuthAccount> findByProviderAndProviderUserId(
		OAuthProvider provider,
		String providerUserId
	);

	Optional<OAuthAccount> findByUser_IdAndProvider(
		Long userId,
		OAuthProvider provider
	);

	boolean existsByProviderAndProviderUserId(
		OAuthProvider provider,
		String providerUserId
	);
}
