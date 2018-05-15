package multicert.Application;

import org.hyperledger.fabric.protos.peer.Query;
import org.hyperledger.fabric.sdk.*;
import org.hyperledger.fabric.sdk.exception.ProposalException;

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

public class Blockchain {

    //The version of the chaincode functioning. Will always be the one used
    //Upgrade will increment, downgrade will decrement
    private String name;
    private double currentVersion = 0;
    private double upgradingPattern = 0.1;
    private boolean instatiated = false;
    private List<Double> chaincodeVersions = new ArrayList<>();
    private Hashtable<String, List<UUID>> IDHashtable = new Hashtable<>();
    private Hashtable<String, Double> ChaincodeVersionHashtable = new Hashtable<>();

    public Blockchain(String chaincodeName){
        this.name = chaincodeName;
    }

    public void initializeBlockchainClass(){
        try {
            IDHashtable = (Hashtable<String, List<UUID>>) Utils.tryDeserialize("IDHashtable");
        }
        catch(Exception e) {
            IDHashtable = new Hashtable<>();
        }

        try{
            ChaincodeVersionHashtable = (Hashtable<String, Double>) Utils.tryDeserialize("ChaincodeVersionHashtable");
        }
        catch(Exception e){
            ChaincodeVersionHashtable = new Hashtable<>();
        }
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
    public int upgradeChaincode(HFClient client, Channel channel, String chaincodeName, ChaincodeEndorsementPolicy... endorsementPolicies){
        if(currentVersion == 0){
            if(!instatiated) {
                Logs.write("Cannot upgrade chaincode if no version is running");
                return -1;
            }
            resolveChaincodeVersion(client, channel, chaincodeName);
        }

        Logs.write("Installing version " + getCurrentVersion() + "...");


        //If upgrading from a downgraded version...
        if(currentVersion != chaincodeVersions.get(chaincodeVersions.size()-1)){
            currentVersion = chaincodeVersions.get(chaincodeVersions.size()-1);
        }
        updateChaincodeVersion();

        //If upgrade failed, but install succeded
        if(chaincodeVersions.contains(currentVersion)){
            Logs.write("Chaincode version " + currentVersion + " already installed. Moving on to upgrade...");
        }
        else {

            chaincodeVersions.add(new Double(getCurrentVersion()));

            try {
                install(client, channel, chaincodeName, endorsementPolicies);
            } catch (Exception e) {
                Logs.write("Error installing chaincode upgrade. Failed to upgrade to version " + getCurrentVersion());
                recoverVersionFromError(false);
                return -1;
            }

            Logs.write("Installed new chaincode version! New version: " + getCurrentVersion());

        }

        Logs.write("Upgrading version...");

        try{
            upgrade(client, channel, chaincodeName, endorsementPolicies);
        }
        catch(Exception e){
            Logs.write("Error upgrading chaincode. Failed to upgrade to " + getCurrentVersion());
            recoverVersionFromError(true);
            return -1;
        }


        Logs.write("Success deploying new chaincode version! Current Version: " + getCurrentVersion());
        return 0;

    }


    /**
     *
     * To be used for the first installation of chaincode, and only then. Started the chaincode cycle
     *
     * @param client
     * @param channel
     * @param endorsementPolicies
     * @return
     */
    public static int instatiateChaincode(HFClient client, Channel channel, String chaincodeName, ChaincodeEndorsementPolicy... endorsementPolicies){

        if(instatiated || currentVersion != 0 || !chaincodeVersions.isEmpty()){
            Logs.write("Chaincode already instantiated, proceeding to upgrade!");
            return upgradeChaincode(client, channel, chaincodeName, endorsementPolicies);
        }
        updateChaincodeVersion();

        //if chaincode installed but not instantiated due to error of some king...
        if(chaincodeVersions.contains(currentVersion)){
            Logs.write("Chaincode version " + currentVersion + " already installed. Moving on to instantiation...");
        }
        else{
            chaincodeVersions.add(new Double(getCurrentVersion()));

            Logs.write("Installing Chaincode...");
            try {
                install(client, channel, chaincodeName, endorsementPolicies);
            }
            catch(Exception e){
                Logs.write("Error installing chaincode. " + e.getLocalizedMessage());
                recoverVersionFromError(false);
                return -1;
            }

            Logs.write("Installed new chaincode version! New version: " + getCurrentVersion());
        }



        Logs.write("Instantiating chaincode...");

        try{
            instantiate(client, channel, chaincodeName, endorsementPolicies);
        }
        catch(Exception e){
            Logs.write("Error instantiating the chaincode.");
            recoverVersionFromError(true);
            return -1;
        }

        Logs.write("Success deploying new chaincode version! Current Version: " + getCurrentVersion());

        return 0;

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
    public static int downgradeChaincode(HFClient client, Channel channel, String chaincodeName, ChaincodeEndorsementPolicy... endorsementPolicies){

        if(currentVersion == 0){
            if(!instatiated) {
                Logs.write("Cannot upgrade chaincode if no version is running");
                return -1;
            }
            resolveChaincodeVersion(client, channel, chaincodeName);
        }

        Logs.write("Downgrading version " + getCurrentVersion() + " to " + chaincodeVersions.get(chaincodeVersions.size()-1) + "...");

        updateChaincodeVersion(chaincodeVersions.get(chaincodeVersions.size()-1));
        return 0;
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
    public static int downgradeChaincode(HFClient client, Channel channel, double version, String chaincodeName, ChaincodeEndorsementPolicy... endorsementPolicies){

        if(currentVersion == 0){
            if(!instatiated) {
                Logs.write("Cannot upgrade chaincode if no version is running");
                return -1;
            }
            resolveChaincodeVersion(client, channel, chaincodeName);
        }

        Logs.write("Downgrading version " + getCurrentVersion() + " to " + version + "...");

        if(chaincodeVersions.contains(version)) {
            updateChaincodeVersion(version);
        }
        else{
            if(instatiated) Logs.write("Version " + version + " is not installed.");
            else Logs.write("Chaincode not instantiated.");
            return -1;
        }
        return 0;
    }



    /**
     *
     * Simple getter for the current chaincode version
     *
     * @return
     */
    public static double getCurrentVersion(){return currentVersion;}

    public static void setCurrentVersion(double newVersion){currentVersion = newVersion;}

    public static String queryDocument(HFClient client, Channel channel, String chaincodeName, String documentName){

        try {
            String str = new String();
            if (IDHashtable.containsKey(documentName)) {
                List<UUID> idList = IDHashtable.get(documentName);

                for (UUID id : idList) {
                    str = str + op_query(client, channel, chaincodeName, documentName, id) + "\n";
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


    /**
     *
     * For logging purposes mostly. Returns the next version "name" without commiting it.
     *
     * @return
     */
    //TODO: add version incrementation constant
    private static double getIncrementedVersion(){
        return currentVersion + upgradingPattern;
    }


    /**
     *
     * The pattern in which the chaincode version is upgraded
     *
     * @return
     */
    public static double getUpgradingPattern (){
        return upgradingPattern;
    }


    /**
     *
     * The pattern in which the chaincode version is upgraded
     *
     * @return
     */
    public static void setUpgradingPattern(double newUpgradingPattern){upgradingPattern = newUpgradingPattern;}


    /**
     * Commit chaincode version
     */
    private static void updateChaincodeVersion(double... version){
        if(currentVersion==0) {currentVersion++; instatiated = true; return;}

        if(version.length > 0) {
            currentVersion = version[0];
        }
        else if(version.length == 0) {
            currentVersion = currentVersion + upgradingPattern;
        }
        else {
            Logs.write("Error updating version");
        }
    }

    private static void recoverVersionFromError(boolean isInstalled){
        if(isInstalled) {
            currentVersion = currentVersion - upgradingPattern;
        }
        else{
            chaincodeVersions.remove(currentVersion);
            currentVersion = currentVersion - upgradingPattern;
        }
    }

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
    private static void resolveChaincodeVersion(HFClient client, Channel channel, String chaincodeName){}


    public static int start_op(HFClient client, Channel channel, String chaincodeName, String documentName, byte[] signature) throws Exception{


        UUID id = Utils.generateID(IDHashtable);
        Logs.write("Starting signature cycle for document " + documentName + "...");

        Collection<ProposalResponse> successful = new LinkedList<>();
        Collection<ProposalResponse> failed = new LinkedList<>();
        TransactionProposalRequest transactionRequest = client.newTransactionProposalRequest();


        ChaincodeID ccId = ChaincodeID.newBuilder().setName(chaincodeName).setVersion(""+currentVersion).build();
        transactionRequest.setChaincodeID(ccId);
        transactionRequest.setFcn("op_start");
        transactionRequest.setArgs(id.toString(), documentName);
        transactionRequest.setArgs(signature);

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
        Utils.serialize(IDHashtable, "IDHashtable");
        return 0;

    }

    public static int sign_op(HFClient client, Channel channel, String chaincodeName, String documentName, byte[] hash) throws Exception{

        UUID id = Utils.generateID(IDHashtable);
        Logs.write("Signing document " + documentName + "...");

        Collection<ProposalResponse> successful = new LinkedList<>();
        Collection<ProposalResponse> failed = new LinkedList<>();
        TransactionProposalRequest transactionRequest = client.newTransactionProposalRequest();


        ChaincodeID ccId = ChaincodeID.newBuilder().setName(chaincodeName).setVersion(""+currentVersion).build();
        transactionRequest.setChaincodeID(ccId);
        transactionRequest.setFcn("op_sign");
        transactionRequest.setArgs(id.toString(), documentName);
        transactionRequest.setArgs(hash);

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
        Utils.serialize(IDHashtable, "IDHashtable");
        return 0;

    }

    public static int end_op(HFClient client, Channel channel, String chaincodeName, String documentName, byte[] signature) throws Exception{

        UUID id = Utils.generateID(IDHashtable);
        Logs.write("Signing document " + documentName + "...");

        Collection<ProposalResponse> successful = new LinkedList<>();
        Collection<ProposalResponse> failed = new LinkedList<>();
        TransactionProposalRequest transactionRequest = client.newTransactionProposalRequest();


        ChaincodeID ccId = ChaincodeID.newBuilder().setName(chaincodeName).setVersion(""+currentVersion).build();
        transactionRequest.setChaincodeID(ccId);
        transactionRequest.setFcn("op_end");
        transactionRequest.setArgs(id.toString(), documentName);
        transactionRequest.setArgs(signature);

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
        Utils.serialize(IDHashtable, "IDHashtable");
        return 0;
    }

    public static String op_query(HFClient client, Channel channel, String chaincodeName, String documentName, UUID id) throws Exception{

        Logs.write("Query blockchain for document " + documentName);

        QueryByChaincodeRequest qpr = client.newQueryProposalRequest();
        ChaincodeID ccId = ChaincodeID.newBuilder().setName(chaincodeName).setVersion(""+currentVersion).build();
        qpr.setChaincodeID(ccId);
        qpr.setFcn("op_query");
        qpr.setArgs(id.toString());


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

    public static void install(HFClient client, Channel channel, String chaincodeName, ChaincodeEndorsementPolicy... endorsementPolicies) throws Exception{
        Collection<ProposalResponse> responses;
        Collection<ProposalResponse> successful = new LinkedList<>();
        Collection<ProposalResponse> failed = new LinkedList<>();

        //ChaincodeID chaincodeID = ChaincodeID.newBuilder().setPath("main/resources/chaincode/").setName(chaincodeName).setVersion(""+currentVersion).build();

        ChaincodeID.Builder chaincodeIDBuilder = ChaincodeID.newBuilder().setName(chaincodeName);
        chaincodeIDBuilder.setPath("main/resources/chaincode/");
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


    private static void upgrade(HFClient client, Channel channel, String chaincodeName, ChaincodeEndorsementPolicy... endorsementPolicies) throws Exception{

        Collection<ProposalResponse> responses;
        Collection<ProposalResponse> successful = new LinkedList<>();
        Collection<ProposalResponse> failed = new LinkedList<>();

        ChaincodeID.Builder chaincodeIDBuilder = ChaincodeID.newBuilder().setName(chaincodeName).setVersion(""+currentVersion);
        chaincodeIDBuilder.setPath("main/resources/chaincode/");
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

    public static void instantiate(HFClient client, Channel channel, String chaincodeName, ChaincodeEndorsementPolicy... endorsementPolicies) throws Exception{

        ChaincodeID chaincodeID = ChaincodeID.newBuilder().setPath("main/resources/chaincode/").setName(chaincodeName).setVersion(""+currentVersion).build();
        //ChaincodeID chaincodeID = chaincodeIDHashtable.get(chaincodeName);
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

    public static void requestInfo(HFClient client, Channel channel, String chaincode) throws Exception{

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
