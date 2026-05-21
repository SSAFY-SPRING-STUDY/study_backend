package ssafy.study.backend.global.mail;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class GmailMailSender implements MailSender {

	private final JavaMailSender javaMailSender;

	@Value("${spring.mail.username}")
	private String from;

	@Override
	@Async(MailAsyncConfig.MAIL_EXECUTOR)
	public void send(String to, String subject, String body) {
		try {
			SimpleMailMessage message = new SimpleMailMessage();
			message.setFrom(from);
			message.setTo(to);
			message.setSubject(subject);
			message.setText(body);

			javaMailSender.send(message);
			log.info("Mail sent to {}", to);
		} catch (Exception e) {
			log.error("Failed to send mail to {}: {}", to, e.getMessage());
		}
	}
}
