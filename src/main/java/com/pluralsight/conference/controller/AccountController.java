package com.pluralsight.conference.controller;

import com.pluralsight.conference.model.Account;
import com.pluralsight.conference.service.AccountService;
import com.pluralsight.conference.util.OnCreateAccountEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.validation.Valid;

@Controller
public class AccountController {

    @Autowired
    private AccountService accountService;

    @Autowired
    private PasswordEncoder encoder;

    @Autowired
    private ApplicationEventPublisher eventPublisher;

    @GetMapping("account")
    //modelAttribute ties to account.jsp's form's account
    public String getRegistration(@ModelAttribute("account") Account account) {
        return "account";
    }

    @PostMapping("account")
    public String addRegistration(@Valid @ModelAttribute ("account")
                                          Account account,
                                  BindingResult result) {

        //STEP 1 => check for errors
        //STEP 2 => should verify that the account and the user don't already exist
        //STEP 3 => should verify valid email address

        //encrypt password
        account.setPassword(encoder.encode(account.getPassword()));

        //create the account
        account = accountService.create(account);

        //fire off an event on creation
        //comes from spring-boot-starter-mail
        //async way of doing
        //Event listener is defined in => AccountListener
        eventPublisher.publishEvent(new OnCreateAccountEvent(account,"conference_war"));

        //spring mvc technique to redirect to a page we want to go to
        return "redirect:account";
    }

    @GetMapping("accountConfirm")
    public String confirmAccount(@RequestParam("token") String token) {
        accountService.confirmAccount(token);

        return "accountConfirmed";
    }

}
