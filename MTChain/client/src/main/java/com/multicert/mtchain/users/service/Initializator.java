package com.multicert.mtchain.users.service;

import com.multicert.mtchain.users.cryptography.Certification;
import com.multicert.mtchain.users.utils.FileReader;
import com.multicert.mtchain.users.utils.Logs;
import org.hyperledger.fabric.sdk.Channel;
import org.hyperledger.fabric.sdk.HFClient;
import org.hyperledger.fabric.sdk.NetworkConfig;
import org.hyperledger.fabric.sdk.security.CryptoSuite;
import org.hyperledger.fabric_ca.sdk.HFCAClient;

import java.io.File;
import java.security.cert.X509Certificate;

public class Initializator {

    protected static NetworkConfig networkConfig;
    protected static HFCAClient caClient;
    protected static NetworkConfig.UserInfo adminInfo;
    protected static Channel channelObj;
    protected static HFClient client;
    protected static X509Certificate serverCertificate;
    //protected static List<Blockchain> chaincodeInstantiations;


    protected final static String CHANNEL_NAME = "channel4";



    private Initializator(){

    }

    public static String init(){

        try {
            initializeHyperledgerConnection(CHANNEL_NAME);
        }catch(Exception e){e.printStackTrace();}
        return "Started Sucessfuly!";
    }

    public static NetworkConfig getNetworkConfig(){
        return networkConfig;
    }

    public static Channel initializeHyperledgerConnection(String channelName) throws Exception{


        CryptoSuite cs = CryptoSuite.Factory.getCryptoSuite();
        Logs.write("Starting reading of config file...");
        networkConfig = FileReader.getNetworkConfigFileYAML();//TLSConfigFile);

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

        Certification.setKeyPair(new File("keystore.jks"));

        channelObj = channel;

        return channel;
    }

    /*public static void populateChaincodeInstatiationsList(List<Blockchain> bcList){
        chaincodeInstantiations = bcList;
    }*/
}
