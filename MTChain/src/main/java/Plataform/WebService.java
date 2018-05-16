package Plataform;

import Plataform.Application.Blockchain;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;

@SpringBootApplication
public class WebService extends SpringBootServletInitializer {

    @Override
    protected SpringApplicationBuilder configure(SpringApplicationBuilder application) {

        return application.sources(FireStarter.class);
    }

    public static void main(String[] args) {
        SpringApplication.run(WebService.class, args);
    }
}

@RestController
class ApplicationController{



    @RequestMapping("/")
    public static String chaincodeName(){
        return "Hi";
        /*

        String str = "<html><body>";

        str = FireStarter.init() + "<br><br>";

        str = str + "INSTALLED:<br>";

        for(Blockchain bc: FireStarter.chaincodeInstantiations){
            str = str + bc.getName() + ":" + bc.getCurrentVersion() + " in {";
            for(Double d: bc.getVersions()){
                str = str + d.toString() + ",";
            }
            str = str + "}<br>";
        }

        str = str + "<br>CHAINCODE FILES: <br>" ;

        Path path = Paths.get("/home/jorge/Documents/MTChain/src/main/resources/chaincode");


        File folder = new File("/home/jorge/Documents/MTChain/src/main/resources/chaincode");
        File[] listOfFiles = folder.listFiles();

        for (File file : listOfFiles) {
            if (file.isFile()) {
                str=str + file.getName() + "<br>";
            }
        }

        str = str + "</body></html>";

        return str;*/
    }


    @RequestMapping("/upgrade")
    String hello(@RequestParam(value="chaincode") String chaincodeName) {

        Blockchain blockchain = FireStarter.getChaincode(chaincodeName);
        return FireStarter.updateChaincodeVersion(chaincodeName);


    }

    @RequestMapping("/downgrade")
    String hello(@RequestParam(value="chaincode", defaultValue = "") String chaincodeName,
                 @RequestParam(value="version", defaultValue = "nada") String version)
    {
        if(chaincodeName.equals("")){
            return "Need to provide a chaincode name to downgrade...";
        }
        if(version.equals("nada")) {
            return FireStarter.downgradeChaincodeVersion(chaincodeName);
        }
        else{
            try {
                return FireStarter.downgradeChaincodeVersion(chaincodeName, Double.parseDouble(version));
            }
            catch(Exception e){
                return "Version value must be a number in for x or x.x ...";
            }
        }
    }

    /*
    @RequestMapping("/install")
    public String installBlockchain() {//@RequestParam(value="name", defaultValue="World") String name) {

        try {
            FireStarter.install();
        } catch (Exception a) {
            return a.getMessage();
        }
        return "GOOD";
    }

    @RequestMapping("/instant")
    public String instantBlockchain() {//@RequestParam(value="name", defaultValue="World") String name) {

        try {
            FireStarter.instant();
        } catch (Exception a) {
            return a.getMessage();
        }
        return "GOOD";
    }

    @RequestMapping("/upgrade")
    public void upgradeBlockchain() {//@RequestParam(value="name", defaultValue="World") String name) {

        try {
            FireStarter.upgradeChaincode();
        } catch (Exception a) {
            a.printStackTrace();
        }
    }*/

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

