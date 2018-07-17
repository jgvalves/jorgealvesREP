package com.multicert.mtchain.backoffice;

import com.multicert.mtchain.backoffice.Application.FileReader;
import com.multicert.mtchain.backoffice.Application.Logs;
import com.multicert.mtchain.core.Blockchain;
import org.apache.log4j.Logger;
import org.hyperledger.fabric.sdk.Channel;
import org.hyperledger.fabric.sdk.HFClient;
import org.hyperledger.fabric.sdk.NetworkConfig;
import org.hyperledger.fabric.sdk.security.CryptoSuite;
import org.hyperledger.fabric_ca.sdk.HFCAClient;

import java.util.ArrayList;
import java.util.List;


public class FireStarter {


    protected static HFClient client;
    protected static HFCAClient caClient;
    protected static NetworkConfig networkConfig;
    protected static NetworkConfig.UserInfo adminInfo;
    protected static Channel channel = null;
    protected static List<Blockchain> chaincodeInstantiations;
    private static List<BlockchainNodes> blockchainNodesInfo;

    protected final static String CHANNEL_NAME = "channel4";
    protected final static String CHAINCODE_NAME = "cc2";
    protected final static String DOCUMENT_NAME = "documento";

    private final Logger logger = Logger.getLogger(FireStarter.class);

    public static String init(){

        try {
            initializeHyperledgerConnection(CHANNEL_NAME);
            //populateChaincodeInstatiationsList(new FireStarterConfigurator().getChaincodes());
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

        populateBlockchainNodeList(channel);

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

    private static void populateBlockchainNodeList(Channel channel){

        blockchainNodesInfo = new ArrayList<>();

        for(NetworkConfig.OrgInfo org: networkConfig.getOrganizationInfos()){
            for(String peer: networkConfig.getOrganizationInfo(org.getName()).getPeerNames()){
                blockchainNodesInfo.add(new BlockchainNodes(peer, org.getName(), "Endorsing", channel.getName()));
            }

            for(String orderer: networkConfig.getOrdererNames()){
                blockchainNodesInfo.add(new BlockchainNodes(orderer, org.getName(), "Orderer", channel.getName()));
            }
        }
    }

    public static void populateChaincodeInstatiationsList(List<Blockchain> bcList){
        chaincodeInstantiations = bcList;
    }

    public static List<BlockchainNodes> getBlockchainNodeList(){
        return blockchainNodesInfo;
    }

    public static List<Blockchain> getChaincodeList(){
        return chaincodeInstantiations;
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

    public static String updateChaincodeVersion(Blockchain blockchain){

        String str = "ERROR";
        try {
        str = blockchain.updateChaincode(client, client.getChannel(CHANNEL_NAME));
        }catch(Exception e){e.printStackTrace();}
        return str;
    }


    public static String downgradeChaincodeVersion(Blockchain blockchain, double... version){

        String str;
        if(version.length>0){
            str = blockchain.downgradeChaincode(client, client.getChannel(CHANNEL_NAME), version[0]);
        }
        else{
            str = blockchain.downgradeChaincode(client, client.getChannel(CHANNEL_NAME));
        }
        return str;
    }

    public static String queryChaincode(String document) throws Exception{
        Blockchain blockchain = getChaincode(CHAINCODE_NAME);
        return blockchain.queryDocument(client, client.getChannel(CHANNEL_NAME), document);
    }
}

class BlockchainNodes {

    public enum State {
        UP,
        DOWN
    }

    private String name;
    private String org;
    private String type;
    private String channel;

    public BlockchainNodes(String name, String org, String type, String channel){
        this.name = name;
        this.org = org;
        this.type = type;
        this.channel = channel;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getOrg() {
        return org;
    }

    public void setOrg(String org) {
        this.org = org;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getChannel() {
        return channel;
    }

    public void setChannel(String channel) {
        this.channel = channel;
    }
}

