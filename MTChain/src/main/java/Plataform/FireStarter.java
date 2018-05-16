package Plataform;

import Plataform.Application.Blockchain;
import Plataform.Application.FileReader;
import Plataform.Application.Logs;
import Plataform.Application.Utils;
import javafx.application.Application;
import org.apache.log4j.Logger;
import org.hyperledger.fabric.sdk.Channel;
import org.hyperledger.fabric.sdk.HFClient;
import org.hyperledger.fabric.sdk.NetworkConfig;
import org.hyperledger.fabric.sdk.security.CryptoSuite;
import org.hyperledger.fabric_ca.sdk.HFCAClient;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

import static java.lang.System.exit;


public class FireStarter{


    protected static HFClient client;
    protected static HFCAClient caClient;
    protected static NetworkConfig networkConfig;
    protected static NetworkConfig.UserInfo adminInfo;
    protected static Channel channel = null;
    protected static List<Blockchain> chaincodeInstantiations;
    protected static boolean started = false;

    protected final static String CHANNEL_NAME = "channel4";
    protected final static String CHAINCODE_NAME = "cc2";
    protected final static String DOCUMENT_NAME = "documento";

    private final Logger  logger = Logger.getLogger(FireStarter.class);

    public static String init(){
        if(started) return "Already Started!";
        chaincodeInstantiations = getChaincodeList();
        try {
            initializeHyperledgerConnection(CHANNEL_NAME);
        }catch(Exception e){e.printStackTrace();}
        started = true;
        return "Started Sucessfuly!";
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

    public static Blockchain getChaincode(String chaincodeName){
        if(chaincodeInstantiations.size() > 0){
            for(Blockchain bc: chaincodeInstantiations){
                if(bc.getName().equals(chaincodeName)){
                    return bc;
                }
            }
            Blockchain bc = new Blockchain(chaincodeName);
            Logs.write("Creating new instance of chaincode named: " + chaincodeName);
            chaincodeInstantiations.add(bc);
            return bc;

        }
        else{
            Logs.write("Creating new instance of chaincode named: " + chaincodeName);
            Blockchain bc = new Blockchain(chaincodeName);
            chaincodeInstantiations.add(bc);
            return bc;
        }
    }

    private static List<Blockchain> getChaincodeList(){
        try{
            List<Blockchain> l = (List<Blockchain>) Utils.tryDeserialize("BlockchainObjectsList");
            Logs.write("Found BlockchainObjectsList file, getting blockchain objects...");
            return l;
        }
        catch(Exception e){
            Logs.write("Didn't find BlockchainObjectsList file. Initializing new List...");
            return new ArrayList<>();
        }
    }



    /*********************************************************
     *
     *
     *              SET OF REST CALLS TO THE
     *                      PLATFORM
     *
     *
     *
     *********************************************************/

    /**************************************
     *
     *  MANAGER CALLS
     *
     **************************************/

    public static String updateChaincodeVersion(String chaincodeName){

        Blockchain blockchain = getChaincode(chaincodeName);
        String str = blockchain.updateChaincode(client, client.getChannel(CHANNEL_NAME));
        try {
            Utils.serialize(chaincodeInstantiations, "BlockchainObjectsList");
        }catch(Exception e){e.printStackTrace();}
        return str;
    }


    public static String downgradeChaincodeVersion(String chaincodeName, double... version){
        Blockchain blockchain = getChaincode(chaincodeName);
        String str;
        if(version.length>0){
            str = blockchain.downgradeChaincode(client, client.getChannel(CHANNEL_NAME), version[0]);
        }
        else{
            str = blockchain.downgradeChaincode(client, client.getChannel(CHANNEL_NAME));
        }
        try {
            Utils.serialize(chaincodeInstantiations, "BlockchainObjectsList");
        }catch(Exception e){e.printStackTrace();}
        return str;
    }


    /***************************************
     *
     *  APPLICATION CALLS
     *
     ***************************************/

    public static String signOpChaincode(String document) throws Exception{
        Blockchain blockchain = getChaincode(CHAINCODE_NAME);
        try {

            byte[] hash = ("" + document.hashCode()).getBytes();
            blockchain.sign_op(client, client.getChannel(CHANNEL_NAME), document, hash);
            return "Added \"" + document + "\" with signOp to the blockchain!";
        }catch(Exception e){
            e.printStackTrace();
            throw new Exception();
        }
    }

    public static String endOpChaincode(String document) throws Exception{
        byte[] ass = "assinaturaPlat".getBytes();

        Blockchain blockchain = getChaincode(CHAINCODE_NAME);
        try {
            blockchain.end_op(client, client.getChannel(CHANNEL_NAME), document, ass);
            return "Added \"" + document + "\" with endOp to the blockchain!";
        }
        catch(Exception e){
            throw new Exception(e);
        }

    }

    public static String startOpChaincode(String document) throws Exception{
        byte[] ass = "assinaturaPlat".getBytes();

        Blockchain blockchain = getChaincode(CHAINCODE_NAME);
        try {
            blockchain.start_op(client, client.getChannel(CHANNEL_NAME), document, ass);
            return "Added \"" + document + "\" with startOp to the blockchain!";
        }
        catch(Exception e){
            throw new Exception(e);
        }

    }

    public static String queryChaincode(String document) throws Exception{
        Blockchain blockchain = getChaincode(CHAINCODE_NAME);
        return blockchain.queryDocument(client, client.getChannel(CHANNEL_NAME), document);
    }
}


