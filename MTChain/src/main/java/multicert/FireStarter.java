package multicert;

import multicert.Application.Blockchain;
import multicert.Application.FileReader;
import multicert.Application.Logs;
import multicert.Application.Utils;
import org.apache.log4j.Logger;
import org.hyperledger.fabric.sdk.Channel;
import org.hyperledger.fabric.sdk.HFClient;
import org.hyperledger.fabric.sdk.NetworkConfig;
import org.hyperledger.fabric.sdk.security.CryptoSuite;
import org.hyperledger.fabric_ca.sdk.HFCAClient;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.rmi.CORBA.Util;
import java.io.PrintWriter;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Arrays;
import java.util.List;

import static java.lang.System.exit;
import static multicert.FireStarter.DOCUMENT_NAME;


@SpringBootApplication
public class FireStarter extends SpringBootServletInitializer{


    protected static HFClient client;
    protected static HFCAClient caClient;
    protected static NetworkConfig networkConfig;
    protected static NetworkConfig.UserInfo adminInfo;
    protected static Channel channel = null;
    private static Class<FireStarter> applicationClass = FireStarter.class;

    protected final static String CHANNEL_NAME = "channel4";
    protected final static String CHAINCODE_NAME = "cc2";
    protected final static String DOCUMENT_NAME = "documento";

    private final Logger  logger = Logger.getLogger(FireStarter.class);


    @Override
    protected SpringApplicationBuilder configure(SpringApplicationBuilder application) {
        //writeFile("BUILDER!!!");
        return application.sources(FireStarter.class);
    }

    public static void main(String[] args) {



        try {
            initializeHyperledgerConnection(CHANNEL_NAME);
            Blockchain.initializeBlockchainClass();
            ///install();
            //instant();
            //startOpChaincode(DOCUMENT_NAME);
            //Blockchain.sign_op(client, client.getChannel(CHANNEL_NAME), CHAINCODE_NAME, DOCUMENT_NAME, "assinatura".getBytes());
            //Blockchain.sign_op(client, client.getChannel(CHANNEL_NAME), CHAINCODE_NAME, DOCUMENT_NAME, "assinatura2".getBytes());
            //System.out.println("\n\n\nQUERY RESULT :\n\n" + queryChaincode(DOCUMENT_NAME) + "\n\n");
        }catch(Exception e){e.printStackTrace();}
        SpringApplication.run(FireStarter.class, args);
    }


    private static void writeFile(String str){
        try {
            List<String> lines = Arrays.asList(str);
            Path file = Paths.get("/home/jorge/Documents/MTChain/log.txt");
            Files.write(file, lines, Charset.forName("UTF-8"), StandardOpenOption.APPEND);
        }
        catch(Exception e){
            e.printStackTrace();
        }
    }




    public static Channel initializeHyperledgerConnection(String channelName) throws Exception{

        CryptoSuite cs = CryptoSuite.Factory.getCryptoSuite();
        Logs.write("Starting reading of config file...");
        networkConfig = FileReader.getNetworkConfigFileYAML();

        NetworkConfig.CAInfo caInfo = networkConfig.getClientOrganization().getCertificateAuthorities().get(0);

        caClient = HFCAClient.createNewInstance(caInfo);
        caClient.setCryptoSuite(cs);

        adminInfo = networkConfig.getPeerAdmin();

        client = HFClient.createNewInstance();
        client.setCryptoSuite(cs);

        client.setUserContext(adminInfo);
        Channel channel;

        try {
            channel = client.loadChannelFromConfig(channelName, networkConfig);
            channel.initialize();
        }
        catch(Exception e){
            e.printStackTrace();
            Logs.write("Error creating channel. Verify if all machines to connect are running, if all IPs are correct and check TLS!");
            return null;
        }

        Logs.write("Channel started successfully: " + channelName);
        return channel;
    }

    public static void instantiateChaincode(){
        Blockchain.instatiateChaincode(client, client.getChannel(CHANNEL_NAME), CHAINCODE_NAME);
    }

    public static void instant() throws Exception{
        Blockchain.setCurrentVersion(1);
        Blockchain.instantiate(client, client.getChannel(CHANNEL_NAME), CHAINCODE_NAME);
    }

    public static void install() throws Exception{
        Blockchain.setCurrentVersion(1);
        Blockchain.install(client, client.getChannel(CHANNEL_NAME), CHAINCODE_NAME);
    }

    public static void upgradeChaincode(){
        Blockchain.upgradeChaincode(client, client.getChannel(CHANNEL_NAME), CHAINCODE_NAME);
    }

    public static String signOpChaincode(String document) throws Exception{
        try {
            byte[] hash = ("" + document.hashCode()).getBytes();
            Blockchain.sign_op(client, client.getChannel(CHANNEL_NAME), CHAINCODE_NAME,document, hash);
            return "Added \"" + document + "\" with signOp to the blockchain!";
        }catch(Exception e){
            e.printStackTrace();
            throw new Exception();
        }
    }

    public static String endOpChaincode(String document) throws Exception{
        byte[] ass = "assinaturaPlat".getBytes();

        Blockchain.setCurrentVersion(1);
        try {
            Blockchain.end_op(client, client.getChannel(CHANNEL_NAME), CHAINCODE_NAME, document, ass);
            return "Added \"" + document + "\" with endOp to the blockchain!";
        }
        catch(Exception e){
            throw new Exception(e);
        }

    }

    public static String startOpChaincode(String document) throws Exception{
        byte[] ass = "assinaturaPlat".getBytes();

        Blockchain.setCurrentVersion(1);
        try {
            Blockchain.start_op(client, client.getChannel(CHANNEL_NAME), CHAINCODE_NAME, document, ass);
            return "Added \"" + document + "\" with startOp to the blockchain!";
        }
        catch(Exception e){
            throw new Exception(e);
        }

    }

    public static String queryChaincode(String document) throws Exception{
        Blockchain.setCurrentVersion(1);
        return Blockchain.queryDocument(client, client.getChannel(CHANNEL_NAME), CHAINCODE_NAME, document);
    }
}

@RestController
class ApplicationController{

    @RequestMapping("/init")
    String hello() {


        try{
            FireStarter.initializeHyperledgerConnection(FireStarter.CHANNEL_NAME);
        }
        catch (Exception e){return "ERROR!";}
        return "Success Initializing Platform. Connected to " + FireStarter.CHAINCODE_NAME + "!";
    }

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
