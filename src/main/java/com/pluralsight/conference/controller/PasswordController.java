package com.pluralsight.conference.controller;

import com.pluralsight.conference.model.Password;
import com.pluralsight.conference.model.ResetToken;
import com.pluralsight.conference.repository.PasswordRepository;
import com.pluralsight.conference.service.PasswordService;
import com.pluralsight.conference.util.OnPasswordResetEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import javax.validation.Valid;
import java.util.Date;

@Controller
public class PasswordController {

    @Autowired
    private ApplicationEventPublisher eventPublisher;

    @Autowired
    private PasswordRepository passwordRepository;

    @Autowired
    private PasswordService passwordService;

    @Autowired
    private PasswordEncoder encoder;

    @GetMapping("password")
    public String getPasswordReset(@ModelAttribute("password") Password password) {
        return "password";
    }

    //NOTE: We will have only mail and username as input into the Password POJO
    @PostMapping("password")
    public String sendEmailToReset(@Valid @ModelAttribute("password")
                                           Password password,
                                   BindingResult result) {
        //check for errors
        //should verify valid email address
        //verify email from database
        //fire off an event to reset email


        //NOTE: We will have only mail and username at this point into Password POJO
        eventPublisher.publishEvent(new OnPasswordResetEvent(password, "conference_war"));
        return "redirect:password?sent=true";
    }

    @GetMapping("passwordReset")
    public ModelAndView getNewPassword(@RequestParam("token") String token) {
        //verify token
        Password password = new Password();
        password.setToken(token);

        return new ModelAndView("resetPassword", "password", password);
    }

    //here we'll only have password and matchingPassword in Password POJO
    @PostMapping("passwordReset")
    public String saveNewPassword(@RequestParam("token") String token,
                                  @ModelAttribute("password") Password password) {
        //should match the password
        //TECH_DEBT: To check if both passwords are matching :)

        //verify token
        ResetToken resetToken = passwordRepository.findByToken(token);
        if (resetToken.getExpiryDate().after(new Date())) {
            password.setPassword(encoder.encode(password.getPassword()));
            passwordService.update(password, resetToken.getUsername());
            return "redirect:passwordReset?reset=true&token=0";
        } else {
            return "tokenExpired";
        }

        //TECH DEBT => To delete token in reset_tokens table
    }
}
