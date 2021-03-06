package com.multicert.mtchain.backoffice.Security;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.view.RedirectView;

@RestController
public class SecurityController {

    @GetMapping("/")
    public RedirectView firstRedirectPage(){
        //CHECK COOKIES FOR ALREADY PERFORMED LOGIN
        //CHECK USER TYPE AND REDIRECT TO PAGE, IF LOGGED
        //ModelAndView mav = new ModelAndView("first");

        return new RedirectView("login");

    }

    @GetMapping("/login")
    public RedirectView loginPage(){
        return new RedirectView("/mtchain/admin/index");
    }
}
