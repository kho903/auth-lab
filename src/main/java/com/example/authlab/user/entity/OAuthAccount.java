package com.example.authlab.user.entity;

import java.time.LocalDateTime;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "oauth_accounts")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class OAuthAccount {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(
		fetch = FetchType.LAZY,
		optional = false
	)
	@JoinColumn(
		name = "user_id",
		nullable = false
	)
	private User user;

	@Enumerated(EnumType.STRING)
	@JdbcTypeCode(SqlTypes.VARCHAR)
	@Column(
		name = "provider",
		nullable = false,
		length = 20
	)
	private OAuthProvider provider;

	@Column(
		name = "provider_user_id",
		nullable = false,
		length = 255
	)
	private String providerUserId;

	@Column(
		name = "provider_email",
		length = 254
	)
	private String providerEmail;

	@Column(
		name = "linked_at",
		insertable = false,
		updatable = false
	)
	private LocalDateTime linkedAt;

	public OAuthAccount(User user, OAuthProvider provider, String providerUserId, String providerEmail) {
		this.user = user;
		this.provider = provider;
		this.providerUserId = providerUserId;
		this.providerEmail = providerEmail;
	}
}
