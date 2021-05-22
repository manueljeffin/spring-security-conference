package com.pluralsight.conference.util;

import com.pluralsight.conference.model.Account;
import com.pluralsight.conference.service.AccountService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class AccountListener implements ApplicationListener<OnCreateAccountEvent> {

    private static final String SERVER_URL = "http://localhost:8080/";

    @Autowired
    private JavaMailSender mailSender;

    @Autowired
    private AccountService accountService;

    @Override
    public void onApplicationEvent(OnCreateAccountEvent event) {
        this.confirmCreateAccount(event);
    }

    private void confirmCreateAccount(OnCreateAccountEvent event) {
        //get the account
        Account account = event.getAccount();

        //create verification token
        String token = UUID.randomUUID().toString();
        accountService.createVerificationToken(account, token);

        //build the contents of the email. Mainly the url which user has to click to confirm
        String recipientAddress = account.getEmail();
        String subject = "Account Confirmation";
        String confirmationUrl = event.getAppUrl() + "/accountConfirm?token=" + token;
        String message = "Please confirm:";

        //send email
        SimpleMailMessage email = new SimpleMailMessage();
        email.setTo(recipientAddress);
        email.setSubject(subject);
        email.setText(message + "\r\n" + SERVER_URL + confirmationUrl);
        mailSender.send(email);

    }
}
