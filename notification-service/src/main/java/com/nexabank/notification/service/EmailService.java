package com.nexabank.notification.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromEmail;

    @Async
    public void sendTransactionConfirmation(Map<String, Object> event) {
        String to = resolveEmail(event);
        if (to == null) return;

        String subject = "Transaction Confirmed - " + event.get("reference");
        String html = buildTransactionEmailHtml(event);
        sendEmail(to, subject, html);
    }

    @Async
    public void sendWelcomeEmail(Map<String, Object> event) {
        String to = String.valueOf(event.getOrDefault("email", ""));
        if (to.isBlank()) return;

        String username = String.valueOf(event.getOrDefault("username", "Customer"));
        String accountNumber = String.valueOf(event.getOrDefault("accountNumber", ""));

        String html = """
            <html><body style="font-family:Arial,sans-serif;max-width:600px;margin:auto">
              <div style="background:#1a3c5e;padding:20px;text-align:center">
                <h1 style="color:white;margin:0">Welcome to NexaBank!</h1>
              </div>
              <div style="padding:30px">
                <h2>Hello, %s!</h2>
                <p>Your account has been successfully opened.</p>
                <div style="background:#f0f4f8;padding:15px;border-radius:8px;margin:20px 0">
                  <strong>Account Number:</strong> %s
                </div>
                <p>You can now start banking with NexaBank. Log in to your portal to get started.</p>
                <a href="http://localhost:4200" style="background:#1a3c5e;color:white;padding:12px 24px;text-decoration:none;border-radius:4px;display:inline-block">
                  Go to Banking Portal
                </a>
              </div>
              <div style="background:#f0f0f0;padding:15px;text-align:center;font-size:12px;color:#666">
                NexaBank - Secure. Smart. Simple. | support@nexabank.com
              </div>
            </body></html>
            """.formatted(username, accountNumber);

        sendEmail(to, "Welcome to NexaBank - Your Account is Ready!", html);
    }

    @Async
    public void sendFraudAlert(Map<String, Object> event) {
        String to = resolveEmail(event);
        if (to == null) return;

        String html = """
            <html><body style="font-family:Arial,sans-serif;max-width:600px;margin:auto">
              <div style="background:#dc2626;padding:20px;text-align:center">
                <h1 style="color:white;margin:0">SECURITY ALERT</h1>
              </div>
              <div style="padding:30px">
                <h2 style="color:#dc2626">Suspicious Transaction Detected</h2>
                <p>We have detected a potentially fraudulent transaction on your account.</p>
                <div style="background:#fff5f5;border:1px solid #dc2626;padding:15px;border-radius:8px;margin:20px 0">
                  <p><strong>Transaction ID:</strong> %s</p>
                  <p><strong>Amount:</strong> %s %s</p>
                  <p><strong>Risk Score:</strong> %s</p>
                </div>
                <p>This transaction has been BLOCKED for review. If this was you, please contact us immediately.</p>
                <p><strong>Emergency:</strong> 1-800-NEXABANK</p>
              </div>
            </body></html>
            """.formatted(
            event.getOrDefault("transactionId", "N/A"),
            event.getOrDefault("amount", "N/A"),
            event.getOrDefault("currency", "USD"),
            event.getOrDefault("fraudScore", "High")
        );

        sendEmail(to, "SECURITY ALERT: Suspicious Transaction Blocked", html);
    }

    @Async
    public void sendPasswordReset(Map<String, Object> message) {
        String to = String.valueOf(message.getOrDefault("email", ""));
        String resetToken = String.valueOf(message.getOrDefault("resetToken", ""));

        String html = """
            <html><body>
              <h2>Password Reset Request</h2>
              <p>Click the link below to reset your password (valid for 15 minutes):</p>
              <a href="http://localhost:4200/reset-password?token=%s">Reset Password</a>
            </body></html>
            """.formatted(resetToken);

        sendEmail(to, "NexaBank - Password Reset Request", html);
    }

    @Async
    public void sendAccountLocked(Map<String, Object> message) {
        String to = String.valueOf(message.getOrDefault("email", ""));
        sendEmail(to, "NexaBank - Account Temporarily Locked",
            "<html><body><h2>Your account has been temporarily locked due to multiple failed login attempts. Contact support to unlock.</h2></body></html>");
    }

    @Async
    public void sendLargeTransactionAlert(Map<String, Object> message) {
        String to = String.valueOf(message.getOrDefault("email", ""));
        String amount = String.valueOf(message.getOrDefault("amount", ""));
        sendEmail(to, "NexaBank - Large Transaction Alert",
            "<html><body><h2>A large transaction of $" + amount + " has been processed on your account.</h2></body></html>");
    }

    private String buildTransactionEmailHtml(Map<String, Object> event) {
        return """
            <html><body style="font-family:Arial,sans-serif;max-width:600px;margin:auto">
              <div style="background:#1a3c5e;padding:20px;text-align:center">
                <h1 style="color:white;margin:0">NexaBank</h1>
                <p style="color:#94b8d0;margin:5px 0">Transaction Confirmation</p>
              </div>
              <div style="padding:30px">
                <h2 style="color:#1a3c5e">Your transfer is complete!</h2>
                <div style="background:#f0f4f8;padding:20px;border-radius:8px;margin:20px 0">
                  <table style="width:100%%">
                    <tr><td style="color:#666">Reference</td><td style="text-align:right;font-weight:bold">%s</td></tr>
                    <tr><td style="color:#666">Amount</td><td style="text-align:right;font-weight:bold;color:#16a34a">%s %s</td></tr>
                    <tr><td style="color:#666">From</td><td style="text-align:right">%s</td></tr>
                    <tr><td style="color:#666">To</td><td style="text-align:right">%s</td></tr>
                    <tr><td style="color:#666">Status</td><td style="text-align:right;color:#16a34a">COMPLETED</td></tr>
                  </table>
                </div>
                <a href="http://localhost:4200/transactions" style="background:#1a3c5e;color:white;padding:12px 24px;text-decoration:none;border-radius:4px;display:inline-block">
                  View Transaction
                </a>
              </div>
              <div style="background:#f0f0f0;padding:15px;text-align:center;font-size:12px;color:#666">
                NexaBank - Secure. Smart. Simple.
              </div>
            </body></html>
            """.formatted(
            event.getOrDefault("reference", "N/A"),
            event.getOrDefault("amount", "0"),
            event.getOrDefault("currency", "USD"),
            event.getOrDefault("fromAccount", "N/A"),
            event.getOrDefault("toAccount", "N/A")
        );
    }

    private void sendEmail(String to, String subject, String htmlBody) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setFrom(fromEmail);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(htmlBody, true);
            mailSender.send(message);
            log.info("Email sent to {} | Subject: {}", to, subject);
        } catch (MessagingException e) {
            log.error("Failed to send email to {}: {}", to, e.getMessage());
        }
    }

    private String resolveEmail(Map<String, Object> event) {
        String email = String.valueOf(event.getOrDefault("email", ""));
        if (email.isBlank() || email.equals("null")) {
            log.warn("No email found in event: {}", event.get("eventType"));
            return null;
        }
        return email;
    }
}
