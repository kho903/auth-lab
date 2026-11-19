package com.example.authlab.user.entity;

import java.time.LocalDateTime;
import java.util.Objects;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "users")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class User {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(
		name = "email",
		nullable = false,
		length = 254
	)
	private String email;

	@Column(
		name = "name",
		nullable = false,
		length = 100
	)
	private String name;

	@Enumerated(EnumType.STRING)
	@JdbcTypeCode(SqlTypes.VARCHAR)
	@Column(
		name = "Status",
		nullable = false,
		length = 20
	)
	private UserStatus status;

	@Column(name = "email_verified_at")
	private LocalDateTime emailVerifiedAt;

	@Column(
		name = "created_at",
		insertable = false,
		updatable = false
	)
	private LocalDateTime createdAt;

	@Column(
		name = "updated_at",
		insertable = false,
		updatable = false
	)
	private LocalDateTime updatedAt;

	public User(
		String email,
		String name,
		UserStatus status,
		LocalDateTime emailVerifiedAt
	) {
		this.email = email;
		this.name = name;
		this.status = status;
		this.emailVerifiedAt = emailVerifiedAt;
	}

	// 일반 회원가입: 이메일 인증 전
	public static User registerLocal(String email, String name) {
		return new User(
			email,
			name,
			UserStatus.PENDING,
			null
		);
	}

	// Google 회원가입: 신뢰할 수 있는 이메일 인증 확인 후
	public static User registerGoogle(
		String email,
		String name,
		LocalDateTime verifiedAt
	) {
		return new User(
			email,
			name,
			UserStatus.ACTIVE,
			Objects.requireNonNull(verifiedAt)
		);
	}

	public void verifyEmail(LocalDateTime verifiedAt) {
		if (this.status != UserStatus.PENDING) {
			throw new IllegalStateException(
				"이메일 인증 대기 상태가 아닙니다."
			);
		}

		this.emailVerifiedAt = Objects.requireNonNull(verifiedAt);
		this.status = UserStatus.ACTIVE;
	}
}
