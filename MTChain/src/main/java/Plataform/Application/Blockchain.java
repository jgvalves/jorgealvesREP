package Plataform.Application;

import org.hyperledger.fabric.protos.peer.Query;
import org.hyperledger.fabric.sdk.*;
import org.hyperledger.fabric.sdk.exception.ProposalException;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;

import java.io.Serializable;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import static java.lang.String.format;
import static org.apache.commons.compress.utils.CharsetNames.UTF_8;

/**
 *
 * THE MOTHERLOAD. This class is responsible for the chaincode related operations
 * Here, chaincode is installed, instatiated, upgraded and used
 *
 * Chaincode operations:
 *
 *      start_op: flag for the start of a new document signature
 *      sign_op: document signature transaction
 *      end_op: flag for the ending of a document signature
 *      query: Receives a document name, returns the signatures associated to it
 *
 * Management operations:
 *
 *      instatiateChaincode: installs initial chaincode version, 1.0. Instatiates it aswell
 *      upgradeChaincode: installs new chaincode version and upgrades the running version
 *      downgradeChaincode: changes the chaincode version to a previously installed version
 *              Without args: changes to the previous version
 *              With args (version number): changes to the version inputed present in the list
 *                  of versions
 *
 *
 *
 */

public class Blockchain implements Serializable {

    //The version of the chaincode functioning. Will always be the one used
    //Upgrade will increment, downgrade will decrement
    private String name;
    private double currentVersion;
    private double upgradingPattern;
    private boolean instantiated;
    private List<Double> chaincodeVersions;
    private Hashtable<String, List<UUID>> IDHashtable;
    private Hashtable<String, Object> ChaincodeVersionHashtable;

    public Blockchain(String chaincodeName){
        this.name = chaincodeName;
        initializeBlockchainClass(chaincodeName);
    }

    public void initializeBlockchainClass(String chaincodeName){

        IDHashtable = new Hashtable<>();
        ChaincodeVersionHashtable = new Hashtable<>();
        currentVersion = 0; ChaincodeVersionHashtable.put("currentVersion", currentVersion);
        upgradingPattern = 0.1; ChaincodeVersionHashtable.put("upgradingPattern", upgradingPattern);
        instantiated = false; ChaincodeVersionHashtable.put("instantiated", instantiated);
        chaincodeVersions = new ArrayList<>(); ChaincodeVersionHashtable.put("chaincodeVersions", chaincodeVersions);
    }


    /**
     *
     * To be used for the installation of chaincode. Instantiates if not instantiated, upgrades if instantiated
     *
     * @param client
     * @param channel
     * @param endorsementPolicies
     * @return
     */
    public String updateChaincode(HFClient client, Channel channel, ChaincodeEndorsementPolicy... endorsementPolicies){

        if(instantiated && currentVersion!=0){
            Logs.write("Upgrading chaincode!");
            return upgradeChaincode(client, channel, endorsementPolicies);
        }



        //if chaincode installed but not instantiated due to error of some kind...
        if(chaincodeVersions.contains(currentVersion)){
            Logs.write("Chaincode version " + currentVersion + " already installed. Moving on to instantiation...");
        }
        else{

            setCurrentVersion(1.0);
            Logs.write("Installing Chaincode 1.0...");

            try {
                install(client, channel, name, endorsementPolicies);
            }
            catch(Exception e){
                Logs.write("Error installing chaincode. " + e.getLocalizedMessage());
                setCurrentVersion(0);
                return "Error installing chaincode: " + e.getLocalizedMessage();
            }

            chaincodeVersions.add(new Double(getCurrentVersion()));
            updateVariables();
            Logs.write("Installed new chaincode version! New version: " + getCurrentVersion());
        }



        Logs.write("Instantiating chaincode...");

        try{
            instantiate(client, channel, name, endorsementPolicies);
        }
        catch(Exception e){
            Logs.write("Error instantiating the chaincode.");
            return "Error instantiating the chaincode: " + e.getLocalizedMessage();
        }

        instantiated = true;
        updateVariables();
        Logs.write("Success deploying new chaincode version! Current Version: " + getCurrentVersion());

        return "Success deploying new chaincode version! Current Version: " + getCurrentVersion();

    }

    /**
     *
     * Abstraction of upgrading the chaincode.
     * Works only if chaincode already instatiated. Workflow: verify if instatiated, install and upgrade
     * TODO: Recover from failed upgrade but successful installation
     *
     * @param client
     * @param channel
     * @param endorsementPolicies
     * @return
     *      0, is ok
     *      -1, ups
     */
    public String upgradeChaincode(HFClient client, Channel channel, ChaincodeEndorsementPolicy... endorsementPolicies){


        //If upgrading from a downgraded version...
        if(currentVersion != chaincodeVersions.get(chaincodeVersions.size()-1)){
            currentVersion = chaincodeVersions.get(chaincodeVersions.size()-1);
        }
        updateChaincodeVersion();
        Logs.write("Installing version " + getCurrentVersion() + "...");

        //If upgrade failed, but install succeded
        if(chaincodeVersions.contains(currentVersion)){
            Logs.write("Chaincode version " + currentVersion + " already installed. Moving on to upgrade...");
        }
        else {

            try {
                install(client, channel, name, endorsementPolicies);
            } catch (Exception e) {
                Logs.write("Error installing chaincode upgrade. Failed to upgrade to version " + getCurrentVersion());
                recoverVersionFromError(false);
                return "Error installing chaincode upgrade. Failed to upgrade to version " + getCurrentVersion();
            }

            chaincodeVersions.add(new Double(getCurrentVersion()));
            updateVariables();
            Logs.write("Installed new chaincode version! New version: " + getCurrentVersion());

        }

        Logs.write("Upgrading version...");

        try{
            upgrade(client, channel, name, endorsementPolicies);
        }
        catch(Exception e){
            Logs.write("Error upgrading chaincode. Failed to upgrade to " + getCurrentVersion());
            recoverVersionFromError(true);
            return "Error upgrading chaincode. Failed to upgrade to " + getCurrentVersion();
        }


        Logs.write("Success deploying new chaincode version! Current Version: " + getCurrentVersion());
        return "Success deploying new chaincode version! Current Version: " + getCurrentVersion();

    }


    /**
     *
     * For management purposes, it is possible to downgrade the chaincode version to the previous deployed version
     *
     * @param client
     * @param channel
     * @param endorsementPolicies
     * @return
     */
    public String downgradeChaincode(HFClient client, Channel channel, ChaincodeEndorsementPolicy... endorsementPolicies){

        if(currentVersion == 0){
            if(!instantiated) {
                Logs.write("Cannot upgrade chaincode if no version is running");
                return "Cannot upgrade chaincode if no version is running";
            }
            resolveChaincodeVersion(client, channel);
        }

        Logs.write("Downgrading version " + getCurrentVersion() + " to " + chaincodeVersions.get(chaincodeVersions.size()-2) + "...");

        updateChaincodeVersion(chaincodeVersions.get(chaincodeVersions.size()-2));
        return "Downgraded to version " + getCurrentVersion();
    }


    /**
     *
     * Overriden the previous downgradeChaincode, with possible version value. If non existant, errors.
     *
     * @param client
     * @param channel
     * @param version
     * @param endorsementPolicies
     * @return
     */
    public String downgradeChaincode(HFClient client, Channel channel, double version, ChaincodeEndorsementPolicy... endorsementPolicies){

        if(currentVersion == 0){
            if(!instantiated) {
                Logs.write("Cannot upgrade chaincode if no version is running");
                return "Cannot upgrade chaincode if no version is running";
            }
            resolveChaincodeVersion(client, channel);
        }

        Logs.write("Downgrading version " + getCurrentVersion() + " to " + version + "...");

        if(chaincodeVersions.contains(version)) {
            updateChaincodeVersion(version);
        }
        else{
            if(instantiated) {
                Logs.write("Version " + version + " is not installed.");
                return "Version " + version + " is not installed.";
            }
            else {
                Logs.write("Chaincode not instantiated.");
                return "Chaincode not instantiated.";
            }
        }
        return "Downgraded successfuly. Current version: " + getCurrentVersion();
    }


    public String queryDocument(HFClient client, Channel channel, String documentName){

        try {
            String str = new String();
            if (IDHashtable.containsKey(documentName)) {
                List<UUID> idList = IDHashtable.get(documentName);

                for (UUID id : idList) {
                    str = str + op_query(client, channel, documentName, id) + "\n";
                }
                return str;
            } else {
                return "NO SUCH DOCUMENT";
            }
        }
        catch(Exception e){
            e.printStackTrace();
            return "ERROR";
        }
    }





    /*****************************************************************
     *
     *                      CLASS VARIABLES AND LOGIC
     *                            MAINTENANCE
     *
     *
     *
     ******************************************************************/

    /**
     *
     * For logging purposes mostly. Returns the next version "name" without commiting it.
     *
     * @return
     */
    //TODO: add version incrementation constant
    private double getIncrementedVersion(){
        return currentVersion + upgradingPattern;
    }

    public String getName() {
        return name;
    }

    /**
     *
     * The pattern in which the chaincode version is upgraded
     *
     * @return
     */
    public double getUpgradingPattern (){
        return upgradingPattern;
    }

    /**
     *
     * The pattern in which the chaincode version is upgraded
     *
     * @return
     */
    public void setUpgradingPattern(double newUpgradingPattern){upgradingPattern = newUpgradingPattern;}

    /**
     * Commit chaincode version
     */
    private void updateChaincodeVersion(double... version){
        if(currentVersion==0) {currentVersion++; updateVariables();return;}
        else if(version.length > 0) {
            currentVersion = version[0];
        }
        else if(version.length == 0) {
            currentVersion = Utils.round(currentVersion + upgradingPattern, 1);
        }
        else {
            Logs.write("Error updating version");
            return;
        }
        updateVariables();
    }

    private void recoverVersionFromError(boolean isInstalled){
        if(isInstalled) {
            currentVersion = currentVersion - upgradingPattern;
        }
        else{
            chaincodeVersions.remove(currentVersion);
            currentVersion = currentVersion - upgradingPattern;

        }
        updateVariables();
    }

    private void updateVariables(){
        ChaincodeVersionHashtable.replace("currentVersion", currentVersion);
        ChaincodeVersionHashtable.replace("upgradingPattern", upgradingPattern);
        ChaincodeVersionHashtable.replace("instantiated", instantiated);
        ChaincodeVersionHashtable.replace("chaincodeVersions", chaincodeVersions);

        /*try {
            Utils.serialize(ChaincodeVersionHashtable, "ChaincodeVersionHashtable:" + name);
        }
        catch (IOException e){
            e.printStackTrace();
        }*/
    }

    /**
     *
     * Simple getter for the current chaincode version
     *
     * @return
     */
    public double getCurrentVersion(){return currentVersion;}

    public List<Double> getVersions(){return chaincodeVersions;}

    public void setCurrentVersion(double newVersion){currentVersion = newVersion;}

    /**
     *
     * * This function has the purpose of correcting possible errors that force the failure of the upgrade or instantiate
     * functions before the correct upgrade of the variable "currentVersion"
     *
     * Contacts the hyperledger system in order to retrieve the latest version in use
     *
     *
     * @param client
     * @param channel
     * TODO
     */
    private static void resolveChaincodeVersion(HFClient client, Channel channel){}



    /*****************************************************************
     *
     *                      CHAINCODE OPERATIONS
     *
     *
     *
     ******************************************************************/
    public int start_op(HFClient client, Channel channel, String documentName, byte[] signature) throws Exception{


        UUID id = Utils.generateID(IDHashtable);
        Logs.write("Starting signature cycle for document " + documentName + "...");

        Collection<ProposalResponse> successful = new LinkedList<>();
        Collection<ProposalResponse> failed = new LinkedList<>();
        TransactionProposalRequest transactionRequest = client.newTransactionProposalRequest();



        ChaincodeID ccId = ChaincodeID.newBuilder().setName(name).setVersion(""+currentVersion).build();
        transactionRequest.setChaincodeID(ccId);
        transactionRequest.setFcn("op_start");
        transactionRequest.setArgs(id.toString(), documentName);
        transactionRequest.setArgs(signature);

        Logs.write("Getting chaincode " + ccId.getName() + ":" + ccId.getVersion());

        Logs.write("Sending transaction proposal to all peers...");
        Logs.write(documentName + ": " + new String(signature, StandardCharsets.UTF_8));

        Collection<ProposalResponse> res = channel.sendTransactionProposal(transactionRequest);

        // display response
        for (ProposalResponse response : res) {
            if (response.getStatus() == ChaincodeResponse.Status.SUCCESS) {
                Logs.write("Successful transaction proposal response Txid: %s from peer %s", response.getTransactionID(), response.getPeer().getName());
                successful.add(response);
            } else {
                failed.add(response);
            }
        }

        Logs.write(format("Received %d transaction proposal responses. Successful+verified: %d . Failed: %d", res.size(), successful.size(), failed.size()));
        if (failed.size() > 0) {
            ProposalResponse firstTransactionProposalResponse = failed.iterator().next();

            throw new ProposalException(format("Not enough endorsers for newEmp:%d endorser error:%s. Was verified:%b",
                    firstTransactionProposalResponse.getStatus().getStatus(), firstTransactionProposalResponse.getMessage(), firstTransactionProposalResponse.isVerified()));
        }
        Logs.write("Successfully received transaction proposal responses from peers!");

        Logs.write("Sending transaction to orderer!");

        CompletableFuture<BlockEvent.TransactionEvent> transactionEvents =  channel.sendTransaction(successful);

        List<UUID> idList = new ArrayList();
        idList.add(id);
        IDHashtable.put(documentName, idList);
        //Utils.serialize(IDHashtable, "IDHashtable:" + name);
        return 0;

    }

    public int sign_op(HFClient client, Channel channel, String documentName, byte[] hash) throws Exception{

        UUID id = Utils.generateID(IDHashtable);
        Logs.write("Signing document " + documentName + "...");

        Collection<ProposalResponse> successful = new LinkedList<>();
        Collection<ProposalResponse> failed = new LinkedList<>();
        TransactionProposalRequest transactionRequest = client.newTransactionProposalRequest();


        ChaincodeID ccId = ChaincodeID.newBuilder().setName(name).setVersion(""+currentVersion).build();
        transactionRequest.setChaincodeID(ccId);
        transactionRequest.setFcn("op_sign");
        transactionRequest.setArgs(id.toString(), documentName);
        transactionRequest.setArgs(hash);

        Logs.write("Getting chaincode " + ccId.getName() + ":" + ccId.getVersion());
        Logs.write("Sending transaction proposal to all peers...");
        Logs.write(documentName + ": " + new String(hash, StandardCharsets.UTF_8));

        Collection<ProposalResponse> res = channel.sendTransactionProposal(transactionRequest);

        // display response
        for (ProposalResponse response : res) {
            if (response.getStatus() == ChaincodeResponse.Status.SUCCESS) {
                Logs.write("Successful transaction proposal response Txid: %s from peer %s", response.getTransactionID(), response.getPeer().getName());
                successful.add(response);
            } else {
                failed.add(response);
            }
        }

        Logs.write(format("Received %d transaction proposal responses. Successful+verified: %d . Failed: %d", res.size(), successful.size(), failed.size()));
        if (failed.size() > 0) {
            ProposalResponse firstTransactionProposalResponse = failed.iterator().next();

            throw new ProposalException(format("Not enough endorsers for newEmp:%d endorser error:%s. Was verified:%b",
                    firstTransactionProposalResponse.getStatus().getStatus(), firstTransactionProposalResponse.getMessage(), firstTransactionProposalResponse.isVerified()));
        }
        Logs.write("Successfully received transaction proposal responses from peers!");

        Logs.write("Sending transaction to orderer!");

        CompletableFuture<BlockEvent.TransactionEvent> transactionEvents =  channel.sendTransaction(successful);

        List<UUID> idList = IDHashtable.get(documentName);
        idList.add(id);
        //Utils.serialize(IDHashtable, "IDHashtable:" + name);
        return 0;

    }

    public int end_op(HFClient client, Channel channel, String documentName, byte[] signature) throws Exception{

        UUID id = Utils.generateID(IDHashtable);
        Logs.write("Signing document " + documentName + "...");

        Collection<ProposalResponse> successful = new LinkedList<>();
        Collection<ProposalResponse> failed = new LinkedList<>();
        TransactionProposalRequest transactionRequest = client.newTransactionProposalRequest();


        ChaincodeID ccId = ChaincodeID.newBuilder().setName(name).setVersion(""+currentVersion).build();
        transactionRequest.setChaincodeID(ccId);
        transactionRequest.setFcn("op_end");
        transactionRequest.setArgs(id.toString(), documentName);
        transactionRequest.setArgs(signature);

        Logs.write("Getting chaincode " + ccId.getName() + ":" + ccId.getVersion());
        Logs.write("Sending transaction proposal to all peers...");
        Logs.write(documentName + ": " + new String(signature, StandardCharsets.UTF_8));

        Collection<ProposalResponse> res = channel.sendTransactionProposal(transactionRequest);

        // display response
        for (ProposalResponse response : res) {
            if (response.getStatus() == ChaincodeResponse.Status.SUCCESS) {
                Logs.write("Successful transaction proposal response Txid: %s from peer %s", response.getTransactionID(), response.getPeer().getName());
                successful.add(response);
            } else {
                failed.add(response);
            }
        }

        Logs.write(format("Received %d transaction proposal responses. Successful+verified: %d . Failed: %d", res.size(), successful.size(), failed.size()));
        if (failed.size() > 0) {
            ProposalResponse firstTransactionProposalResponse = failed.iterator().next();

            throw new ProposalException(format("Not enough endorsers for newEmp:%d endorser error:%s. Was verified:%b",
                    firstTransactionProposalResponse.getStatus().getStatus(), firstTransactionProposalResponse.getMessage(), firstTransactionProposalResponse.isVerified()));
        }
        Logs.write("Successfully received transaction proposal responses from peers!");

        Logs.write("Sending transaction to orderer!");

        CompletableFuture<BlockEvent.TransactionEvent> transactionEvents =  channel.sendTransaction(successful);

        List<UUID> idList = IDHashtable.get(documentName);
        idList.add(id);
        //Utils.serialize(IDHashtable, "IDHashtable:" + name);
        return 0;
    }

    private String op_query(HFClient client, Channel channel, String documentName, UUID id) throws Exception{

        Logs.write("Query blockchain for document " + documentName);

        QueryByChaincodeRequest qpr = client.newQueryProposalRequest();
        ChaincodeID ccId = ChaincodeID.newBuilder().setName(name).setVersion(""+currentVersion).build();
        qpr.setChaincodeID(ccId);
        qpr.setFcn("op_query");
        qpr.setArgs(id.toString());


        Logs.write("Getting chaincode " + ccId.getName() + ":" + ccId.getVersion());
        Logs.write("Querying the peers...");

        Collection<ProposalResponse> res = channel.queryByChaincode(qpr);

        String payload = "nothing";
        for (ProposalResponse pres : res) {

            try {
                if (!pres.getProposalResponse().getResponse().getPayload().toString().equals("")) {
                    payload = pres.getProposalResponse().getResponse().getPayload().toStringUtf8();
                    Logs.write("Query worked.");
                } else {
                    Logs.write("Query didn't return anything.");
                }
            }
            catch(Exception e){
                Logs.write("Response from query returned null.");
                return "-1";
            }

        }

        return payload;

    }




    /*****************************************************************
     *
     *                      CHAINCODE MAINTENANCE
     *
     *
     *
     ******************************************************************/

    private void install(HFClient client, Channel channel, String chaincodeName, ChaincodeEndorsementPolicy... endorsementPolicies) throws Exception{
        Collection<ProposalResponse> responses;
        Collection<ProposalResponse> successful = new LinkedList<>();
        Collection<ProposalResponse> failed = new LinkedList<>();

        //ChaincodeID chaincodeID = ChaincodeID.newBuilder().setPath("main/resources/chaincode/").setName(chaincodeName).setVersion(""+currentVersion).build();

        ChaincodeID.Builder chaincodeIDBuilder = ChaincodeID.newBuilder().setName(chaincodeName);
        chaincodeIDBuilder.setPath("chaincode/");
        ChaincodeID chaincodeID = chaincodeIDBuilder.build();

        InstallProposalRequest installProposalRequest = client.newInstallProposalRequest();
        installProposalRequest.setChaincodeID(chaincodeID);
        installProposalRequest.setChaincodeSourceLocation(Paths.get("").toFile());
        installProposalRequest.setChaincodeVersion(""+currentVersion);
        installProposalRequest.setChaincodeLanguage(TransactionRequest.Type.GO_LANG);
        installProposalRequest.setProposalWaitTime(100000);

        Logs.write( "CHAINCODE: " + installProposalRequest.getChaincodeSourceLocation().getAbsoluteFile());

        if(endorsementPolicies.length > 0){
            installProposalRequest.setChaincodeEndorsementPolicy(endorsementPolicies[0]);
        }

        int numInstallProposal = 0;
        //    Set<String> orgs = orgPeers.keySet();
        //   for (SampleOrg org : testSampleOrgs) {

        Collection<Peer> peers = channel.getPeers();
        numInstallProposal = numInstallProposal + peers.size();
        responses = client.sendInstallProposal(installProposalRequest, peers);

        for (ProposalResponse response : responses) {
            if (response.getStatus() == ProposalResponse.Status.SUCCESS) {
                Logs.write(format("Successful install proposal response Txid: %s from peer %s", response.getTransactionID(), response.getPeer().getName()));
                successful.add(response);
            } else {
                failed.add(response);
            }
        }

        //   }
        Logs.write(format("Received %d install proposal responses. Successful+verified: %d . Failed: %d", numInstallProposal, successful.size(), failed.size()));

        if (failed.size() > 0) {
            ProposalResponse first = failed.iterator().next();
            Logs.write("Not enough endorsers for install :" + successful.size() + ".  " + first.getMessage());
        }

    }

    private void upgrade(HFClient client, Channel channel, String chaincodeName, ChaincodeEndorsementPolicy... endorsementPolicies) throws Exception{

        Collection<ProposalResponse> responses;
        Collection<ProposalResponse> successful = new LinkedList<>();
        Collection<ProposalResponse> failed = new LinkedList<>();

        ChaincodeID.Builder chaincodeIDBuilder = ChaincodeID.newBuilder().setName(chaincodeName).setVersion(""+currentVersion);
        chaincodeIDBuilder.setPath("chaincode/");
        ChaincodeID chaincodeID = chaincodeIDBuilder.build();




        UpgradeProposalRequest upgrade = client.newUpgradeProposalRequest();
        upgrade.setChaincodeID(chaincodeID);
        upgrade.setChaincodeLanguage(TransactionRequest.Type.GO_LANG);
        upgrade.setArgs("init");
        Map<String, byte[]> tmap = new HashMap<>();
        tmap.put("test", "data".getBytes());
        upgrade.setTransientMap(tmap);
        upgrade.setProposalWaitTime(100000);

        if(endorsementPolicies.length > 0){
            upgrade.setChaincodeEndorsementPolicy(endorsementPolicies[0]);
        }

        int numUpgradeProposal = 0;
        Collection<Peer> peers = channel.getPeers();
        numUpgradeProposal = numUpgradeProposal + peers.size();
        //upgrade.setProposalWaitTime(100000);
        responses = channel.sendUpgradeProposal(upgrade, peers);


        for (ProposalResponse response : responses) {
            if (response.getStatus() == ProposalResponse.Status.SUCCESS) {
                Logs.write(format("Successful upgrade proposal response Txid: %s from peer %s", response.getTransactionID(), response.getPeer().getName()));
                successful.add(response);
            } else {
                failed.add(response);
            }
        }

        Logs.write(format("Received %d upgrade proposal responses. Successful+verified: %d . Failed: %d", numUpgradeProposal, successful.size(), failed.size()));

        if (failed.size() > 0) {
            ProposalResponse first = failed.iterator().next();
            Logs.write("Not enough endorsers for upgrade :" + successful.size() + ".  " + first.getMessage());
        }

        Logs.write("Sending Upgrade Transaction to orderer...");
        CompletableFuture<BlockEvent.TransactionEvent> future = channel.sendTransaction(successful, channel.getOrderers());

        Logs.write("calling get...");
        BlockEvent.TransactionEvent event = future.get(100, TimeUnit.SECONDS);
        Logs.write("get done...");


        Logs.write(format("Finished instantiate transaction with transaction id %s", event.getTransactionID()));
    }

    private void instantiate(HFClient client, Channel channel, String chaincodeName, ChaincodeEndorsementPolicy... endorsementPolicies) throws Exception{


        ChaincodeID chaincodeID = ChaincodeID.newBuilder().setPath("chaincode/").setName(chaincodeName).setVersion(""+currentVersion).build();
        Collection<ProposalResponse> responses;
        Collection<ProposalResponse> successful = new LinkedList<>();
        Collection<ProposalResponse> failed = new LinkedList<>();

        InstantiateProposalRequest instantiate = client.newInstantiationProposalRequest();
        instantiate.setFcn("init");
        instantiate.setArgs("Init");
        instantiate.setChaincodeID(chaincodeID);

        if(endorsementPolicies.length > 0){
            instantiate.setChaincodeEndorsementPolicy(endorsementPolicies[0]);
        }

        Map<String, byte[]> tm = new HashMap<>();
        tm.put("HyperLedgerFabric", "InstantiateProposalRequest:JavaSDK".getBytes(UTF_8));
        tm.put("method", "InstantiateProposalRequest".getBytes(UTF_8));
        instantiate.setTransientMap(tm);

        instantiate.setProposalWaitTime(100000);

        Logs.write("Sending instantiateProposalRequest to all peers...");

        responses = channel.sendInstantiationProposal(instantiate);

        for (ProposalResponse response : responses) {
            if (response.isVerified() && response.getStatus() == ProposalResponse.Status.SUCCESS) {
                successful.add(response);
                Logs.write(format("Succesful instantiate proposal response Txid: %s from peer %s", response.getTransactionID(), response.getPeer().getName()));
            } else {
                failed.add(response);
            }
        }
        Logs.write(format("Received %d instantiate proposal responses. Successful+verified: %d . Failed: %d", responses.size(), successful.size(), failed.size()));
        if (failed.size() > 0) {
            ProposalResponse first = failed.iterator().next();
            Logs.write("Not enough endorsers for instantiate :" + successful.size() + "endorser failed with " + first.getMessage() + ". Was verified:" + first.isVerified());
        }

        ///////////////
        /// Send instantiate transaction to orderer
        Logs.write("Sending instantiateTransaction to orderer...");
        CompletableFuture<BlockEvent.TransactionEvent> future = channel.sendTransaction(successful, channel.getOrderers());

        Logs.write("calling get...");
        BlockEvent.TransactionEvent event = future.get(100, TimeUnit.SECONDS);
        Logs.write("get done...");


        Logs.write(format("Finished instantiate transaction with transaction id %s", event.getTransactionID()));


    }

    public void requestInfo(HFClient client, Channel channel, String chaincode) throws Exception{

        for(Peer s: channel.getPeers()) {
            List<Query.ChaincodeInfo> chainInfo = client.queryInstalledChaincodes(s);

            for(Query.ChaincodeInfo cI: chainInfo) {
                if(cI.getName().equals(chaincode)) {
                    Logs.write(cI.getName() + "{ " + cI.getVersion() + " }");
                    return;
                }
            }

        }

    }



}
