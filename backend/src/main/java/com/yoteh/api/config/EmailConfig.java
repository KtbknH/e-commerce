package com.yoteh.api.config;

import java.util.Properties;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;

@Configuration
public class EmailConfig {

    @Value("${yoteh.mail.host:smtp.gmail.com}")
    private String host;

    @Value("${yoteh.mail.port:587}")
    private int port;

    @Value("${yoteh.mail.username:}")
    private String username;

    @Value("${yoteh.mail.password:}")
    private String password;

    @Value("${yoteh.mail.protocol:smtp}")
    private String protocol;

    @Value("${yoteh.mail.smtp.auth:true}")
    private boolean smtpAuth;

    @Value("${yoteh.mail.smtp.starttls:true}")
    private boolean starttls;

    @Value("${yoteh.mail.debug:false}")
    private boolean debug;

    @Bean
    public JavaMailSender javaMailSender() {
        JavaMailSenderImpl mailSender = new JavaMailSenderImpl();
        mailSender.setHost(host);
        mailSender.setPort(port);
        mailSender.setUsername(username);
        mailSender.setPassword(password);
        mailSender.setProtocol(protocol);

        Properties props = mailSender.getJavaMailProperties();
        props.put("mail.smtp.auth", String.valueOf(smtpAuth));
        props.put("mail.smtp.starttls.enable", String.valueOf(starttls));
        props.put("mail.smtp.connectiontimeout", "5000");
        props.put("mail.smtp.timeout", "5000");
        props.put("mail.smtp.writetimeout", "5000");
        props.put("mail.debug", String.valueOf(debug));

        return mailSender;
    }
}
