package Plataform.Application;

import Plataform.FireStarter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.RedirectView;

import javax.websocket.server.PathParam;
import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Hashtable;

@RestController
@RequestMapping("/admin")
public class ApplicationController extends FireStarter{


    @GetMapping("/")
    public RedirectView firstRedirectPage(){
        //CHECK COOKIES FOR ALREADY PERFORMED LOGIN
        //CHECK USER TYPE AND REDIRECT TO PAGE, IF LOGGED
        //ModelAndView mav = new ModelAndView("first");

        return new RedirectView("/login");

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
            return new RedirectView("/admin/index");
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


    @RequestMapping("/query")
    public String queryBlockchain(@RequestParam(value="document", defaultValue="null") String document) {

        try {
            return FireStarter.queryChaincode(document);
        } catch (Exception a) {
            a.printStackTrace();
        }
        return "ERROR";
    }

    @RequestMapping("/start")
    public String startBlockchain(@RequestParam(value="document") String document) {

        if(document.equals(null)){
            return "Please, provide a document!";
        }

        try {
            return FireStarter.startOpChaincode(document);
        } catch (Exception a) {
            a.printStackTrace();
        }
        return "ERROR";
    }

    @RequestMapping("/sign")
    public String signBlockchain(@RequestParam(value="document") String document) {

        if(document.equals(null)){
            return "Please, provide a document!";
        }

        try {
            return FireStarter.signOpChaincode(document);
        } catch (Exception a) {
            a.printStackTrace();
        }
        return "ERROR";
    }

    @RequestMapping("/end")
    public String endBlockchain(@RequestParam(value="document") String document) {

        if(document.equals(null)){
            return "Please, provide a document!";
        }

        try {
            return FireStarter.endOpChaincode(document);
        } catch (Exception a) {
            a.printStackTrace();
        }
        return "ERROR";
    }
}

