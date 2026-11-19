package com.example.authlab.auth.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;

@Component
public class VerificationMailSender {

	private final JavaMailSender mailSender;
	private final String from;

	public VerificationMailSender(
		JavaMailSender mailSender,
		@Value("${app.mail.from}") String from
	) {
		this.mailSender = mailSender;
		this.from = from;
	}

	public void send(String email, String code) {

		SimpleMailMessage message = new SimpleMailMessage();

		message.setFrom(from);
		message.setTo(email);
		message.setSubject("[Auth Lab] 이메일 인증번호");

		message.setText(
			"""
				Auth Lab 이메일 인증번호입니다.
				
				인증번호: %s
				
				유효시간: 5분
				
				요청하지 않은 경우 이 메일을 무시해 주세요.
				""".formatted(code)
		);

		mailSender.send(message);
	}
}
