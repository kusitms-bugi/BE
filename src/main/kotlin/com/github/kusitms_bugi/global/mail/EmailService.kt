package com.github.kusitms_bugi.global.mail

import com.github.kusitms_bugi.global.properties.MailProperties
import org.springframework.mail.javamail.JavaMailSender
import org.springframework.mail.javamail.MimeMessageHelper
import org.springframework.stereotype.Service
import org.thymeleaf.TemplateEngine
import org.thymeleaf.context.Context

@Service
class EmailService(
    private val mailSender: JavaMailSender,
    private val mailProperties: MailProperties,
    private val templateEngine: TemplateEngine
) {

    fun sendVerificationEmail(email: String, token: String, callbackUrl: String) {
        val verificationUrl = "$callbackUrl?token=$token"
        val subject = "이메일 인증을 완료해주세요"

        val context = Context()
        context.setVariable("verificationUrl", verificationUrl)
        val content = templateEngine.process("email-verification", context)

        sendHtmlEmail(email, subject, content)
    }

    fun sendDownloadEmail(email: String, downloadUrl: String) {
        val subject = "다운로드 링크가 도착했어요!"

        val context = Context()
        context.setVariable("downloadUrl", downloadUrl)
        val content = templateEngine.process("download-link", context)

        sendHtmlEmail(email, subject, content)
    }

    private fun sendHtmlEmail(to: String, subject: String, content: String) {
        val message = mailSender.createMimeMessage()
        val helper = MimeMessageHelper(message, true, "UTF-8")

        helper.setFrom(mailProperties.from)
        helper.setTo(to)
        helper.setSubject(subject)
        helper.setText(content, true)

        mailSender.send(message)
    }

}
