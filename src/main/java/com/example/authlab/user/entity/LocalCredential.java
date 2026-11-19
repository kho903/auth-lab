package com.example.authlab.user.entity;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.Objects;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.MapsId;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "local_credentials")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class LocalCredential {

	@Id
	@Column(name = "user_id")
	private Long userId;

	@MapsId
	@OneToOne(
		fetch = FetchType.LAZY,
		optional = false
	)
	@JoinColumn(
		name = "user_id",
		nullable = false
	)
	private User user;

	@Column(
		name = "password_hash",
		nullable = false,
		length = 255
	)
	private String passwordHash;

	@Column(
		name = "password_changed_at",
		nullable = false
	)
	private LocalDateTime passwordChangedAt;

	public LocalCredential(User user, String passwordHash) {
		this.user = user;
		this.passwordHash = passwordHash;
		this.passwordChangedAt = LocalDateTime.now(
			Clock.systemUTC()
		);
	}

	public void changePasswordHash(String newPasswordHash) {
		this.passwordHash = Objects.requireNonNull(newPasswordHash);
		this.passwordChangedAt = LocalDateTime.now(
			Clock.systemUTC()
		);
	}
}
