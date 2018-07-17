package com.multicert.mtchain.backoffice.Application;

import com.multicert.mtchain.backoffice.FireStarter;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.RedirectView;

@RestController
@RequestMapping("/admin")
public class ApplicationController extends FireStarter{


    @GetMapping("/")
    public RedirectView firstRedirectPage(){
        //CHECK COOKIES FOR ALREADY PERFORMED LOGIN
        //CHECK USER TYPE AND REDIRECT TO PAGE, IF LOGGED
        //ModelAndView mav = new ModelAndView("first");

        return new RedirectView("/mtchain/login");

    }

    @GetMapping("/index")
    public ModelAndView indexPage(){

            ModelAndView mav = new ModelAndView("index");

            mav.addObject("BlockchainNodes", FireStarter.getBlockchainNodeList());
            return mav;

    }

    @GetMapping("/refresh")
    public RedirectView refreshNetworkLink(){
        try {
            FireStarter.initializeHyperledgerConnection("channel4");
            return new RedirectView("/mtchain/admin/index");
        }
        catch(Exception e){
            e.printStackTrace();
            return new RedirectView("/error");
        }
    }

    @GetMapping ("/chaincode")
    public ModelAndView chaincodePage(){
        ModelAndView mav = new ModelAndView("chaincode");

        //Double x = FireStarter.getChaincode("cc2").getCurrentVersion();
        mav.addObject("nextVersion", "1.1");
        mav.addObject("version", "1.0");

        //List<Users> users = (List<Users>) usersService.findAll();

        //mav.addObject("users", users);

        return mav;
    }





}

