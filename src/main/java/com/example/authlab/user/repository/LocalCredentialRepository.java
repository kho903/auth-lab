package com.example.authlab.user.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.authlab.user.entity.LocalCredential;

public interface LocalCredentialRepository extends JpaRepository<LocalCredential, Long> {
}
