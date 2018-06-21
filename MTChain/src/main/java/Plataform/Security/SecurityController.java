package Plataform.Security;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.ModelAndView;
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
    public ModelAndView loginPage(){
        ModelAndView mav = new ModelAndView("login");

        return mav;
    }
}
