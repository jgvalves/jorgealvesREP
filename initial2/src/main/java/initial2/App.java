package initial2;

import org.hyperledger.fabric.protos.peer.Chaincode;
import org.hyperledger.fabric.sdk.*;
import org.hyperledger.fabric.sdk.exception.InvalidArgumentException;
import org.hyperledger.fabric.sdk.exception.ProposalException;
import org.hyperledger.fabric.sdk.security.CryptoSuite;
import org.hyperledger.fabric.sdk.Channel;
import org.hyperledger.fabric.sdk.NetworkConfig;
import org.hyperledger.fabric.sdk.NetworkConfig.UserInfo;
import org.hyperledger.fabric.sdk.NetworkConfig.*;
import org.hyperledger.fabric_ca.sdk.EnrollmentRequest;
import org.hyperledger.fabric_ca.sdk.HFCAClient;
import org.hyperledger.fabric.sdk.TransactionRequest.Type;
import org.apache.log4j.Logger;
import org.hyperledger.fabric_ca.sdk.RegistrationRequest;

import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.PrivateKey;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import static java.lang.String.format;
import static java.lang.System.exit;
import static org.apache.commons.compress.utils.CharsetNames.UTF_8;
import static org.hyperledger.fabric.sdk.Channel.PeerOptions.createPeerOptions;



public class App
{
    private static NetworkConfig nc;
    private static Peer peer0, peer1;
    private static EventHub eventhub;
    private static Orderer orderer0;
    private static final Logger log = Logger.getLogger(App.class);
    private static final Map<String, Properties> clientTLSProperties = new HashMap< >();


    //Fluxo install: install, instatiate, use
    //Fluxo upgrade: install, upgrade, use
    private static String currentVersion = "1.81";
    private static int query = 1;
    private static int newEmp = 1;
    private static int install = 0;
    private static int upgrade = 0;
    private static int see = 1;
    private static int inst = 1;
    private static int policy = 0;

    public static void main( String[] args )
    {


        try {

            CryptoSuite cs = CryptoSuite.Factory.getCryptoSuite();
            nc = NetworkConfig.fromYamlFile(getNetworkConfigFileYAML());
            ChaincodeEndorsementPolicy endorsementPolicy = getEndorsementPolicyFileYAML();

            NetworkConfig.CAInfo caInfo = nc.getClientOrganization().getCertificateAuthorities().get(0);


            HFCAClient caClient = HFCAClient.createNewInstance(caInfo.getUrl(), caInfo.getProperties());
            caClient.setCryptoSuite(CryptoSuite.Factory.getCryptoSuite(caInfo.getProperties()));


            UserInfo adminInfo = nc.getPeerAdmin();

            AppUser newAdmin = new AppUser("admin", "org1", "Org1MSP", adminInfo.getEnrollment(), caClient);

            HFClient client = HFClient.createNewInstance();
            client.setCryptoSuite(CryptoSuite.Factory.getCryptoSuite());

            client.setUserContext(adminInfo);

            Channel channel = client.loadChannelFromConfig("mychannel", nc);
            channel.initialize();

//
            Collection<UserInfo> registrars = caInfo.getRegistrars();
            UserInfo registrar = registrars.iterator().next();
            out(caClient.info().toString());exit(0);
//
//          //ADDING USER
            RegistrationRequest rr = new RegistrationRequest("user1", "org1");
            String enrSecret = caClient.register(rr, registrar);
//
            final EnrollmentRequest enrollmentRequestTLS = new EnrollmentRequest();
            enrollmentRequestTLS.addHost("localhost");
            enrollmentRequestTLS.setProfile("tls");
            Enrollment enr = caClient.enroll("user1", enrSecret, enrollmentRequestTLS);
//
            //Enrollment enr = caClient.enroll("user1", enrSecret);
//
            User user1 = new AppUser("user1", "Org1", adminInfo.getMspId(), enr, caClient);








            exit(0);

            out("Peers: " + channel.getPeers().toString());
            //out(channel.queryBlockchainInfo().toString());

            channel.getPeers().iterator().next();

            System.out.println("\n\nVERSION :" + currentVersion + "\n\n");

            if(install == 1) {
                if(policy == 1) ccInstall(client, channel, endorsementPolicy); else ccInstall(client, channel);

            }

            testEndorsement(client, endorsementPolicy);
            //exit(0);




            //if(see == 1) System.out.println(client.queryInstalledChaincodes(channel.getPeers().iterator().next()).toString());


            if(upgrade == 1) {
                if(policy == 1) ccUpgrade(client, channel, endorsementPolicy); else ccUpgrade(client, channel);
            }

            if(inst == 1){
                chaincodeInstantiate(client);
            }

  //          exit(0);
            //out("\n\n\nQUERY:\n");
            //queryEmp(client);

    //        out("\n\n\nNEW:\n");

            if(newEmp == 1) {
                newEmp(client, "2", "Joao", "Empr", "HASH");
            }

      //      out("\n\n\nQUERY2:\n");
            if(query == 1) {
                out(queryEmp(client, "1"));
            }



            System.out.println("\n\nnice!");
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private static void out(String s, Object... o){
        System.out.println(format(s,o));

    }

    private static void ccUpgrade(HFClient client, Channel channel, ChaincodeEndorsementPolicy... endorsementPolicy) throws Exception {
        Collection<ProposalResponse> responses;
        Collection<ProposalResponse> successful = new LinkedList<>();
        Collection<ProposalResponse> failed = new LinkedList<>();

        ChaincodeID.Builder chaincodeIDBuilder = ChaincodeID.newBuilder().setName("cc").setVersion(currentVersion);
        chaincodeIDBuilder.setPath("main/resources/cc/");
        ChaincodeID chaincodeID = chaincodeIDBuilder.build();




        UpgradeProposalRequest upgrade = client.newUpgradeProposalRequest();
        upgrade.setChaincodeID(chaincodeID);
        upgrade.setChaincodeLanguage(Type.GO_LANG);
        upgrade.setArgs("init");
        Map<String, byte[]> tmap = new HashMap<>();
        tmap.put("test", "data".getBytes());
        upgrade.setTransientMap(tmap);

        if(endorsementPolicy.length > 0){
            upgrade.setChaincodeEndorsementPolicy(endorsementPolicy[0]);
        }

        int numUpgradeProposal = 0;
        Collection<Peer> peers = channel.getPeers();
        numUpgradeProposal = numUpgradeProposal + peers.size();
        //upgrade.setProposalWaitTime(100000);
        responses = channel.sendUpgradeProposal(upgrade, peers);


        for (ProposalResponse response : responses) {
            if (response.getStatus() == ProposalResponse.Status.SUCCESS) {
                System.out.println(format("Successful upgrade proposal response Txid: %s from peer %s", response.getTransactionID(), response.getPeer().getName()));
                successful.add(response);
            } else {
                failed.add(response);
            }
        }

        System.out.println(format("Received %d upgrade proposal responses. Successful+verified: %d . Failed: %d", numUpgradeProposal, successful.size(), failed.size()));

        if (failed.size() > 0) {
            ProposalResponse first = failed.iterator().next();
            System.out.println("Not enough endorsers for upgrade :" + successful.size() + ".  " + first.getMessage());
        }

        out("Sending Upgrade Transaction to orderer...");
        CompletableFuture<BlockEvent.TransactionEvent> future = channel.sendTransaction(successful, channel.getOrderers());

        out("calling get...");
        BlockEvent.TransactionEvent event = future.get(100, TimeUnit.SECONDS);
        out("get done...");


        out(format("Finished instantiate transaction with transaction id %s", event.getTransactionID()));


    }

    private static void ccInstall(HFClient client, Channel channel, ChaincodeEndorsementPolicy... endorsementPolicies) throws Exception {

        Collection<ProposalResponse> responses;
        Collection<ProposalResponse> successful = new LinkedList<>();
        Collection<ProposalResponse> failed = new LinkedList<>();


        ChaincodeID.Builder chaincodeIDBuilder = ChaincodeID.newBuilder().setName("cc");
        chaincodeIDBuilder.setPath("main/resources/cc/");
        ChaincodeID chaincodeID = chaincodeIDBuilder.build();

        InstallProposalRequest installProposalRequest = client.newInstallProposalRequest();
        installProposalRequest.setChaincodeID(chaincodeID);
        installProposalRequest.setChaincodeSourceLocation(Paths.get("").toFile());
        installProposalRequest.setChaincodeVersion(currentVersion);
        installProposalRequest.setChaincodeLanguage(Type.GO_LANG);


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
                System.out.println(format("Successful install proposal response Txid: %s from peer %s", response.getTransactionID(), response.getPeer().getName()));
                successful.add(response);
            } else {
                failed.add(response);
            }
        }

        //   }
        System.out.println(format("Received %d install proposal responses. Successful+verified: %d . Failed: %d", numInstallProposal, successful.size(), failed.size()));

        if (failed.size() > 0) {
            ProposalResponse first = failed.iterator().next();
            System.out.println("Not enough endorsers for install :" + successful.size() + ".  " + first.getMessage());
        }

    }

    public static void testEndorsement(HFClient client, ChaincodeEndorsementPolicy endorsementPolicies) throws Exception{
        ChaincodeID chaincodeID = ChaincodeID.newBuilder().setName("cc").setVersion(currentVersion).build();
        Channel channel = client.getChannel("mychannel");

        Collection<ProposalResponse> responses;
        Collection<ProposalResponse> successful = new LinkedList<>();
        Collection<ProposalResponse> failed = new LinkedList<>();

        InstantiateProposalRequest instantiate = client.newInstantiationProposalRequest();
        instantiate.setFcn("init");
        instantiate.setArgs("Init");
        instantiate.setChaincodeID(chaincodeID);

        instantiate.setChaincodeEndorsementPolicy(endorsementPolicies);


        Map<String, byte[]> tm = new HashMap<>();
        tm.put("HyperLedgerFabric", "InstantiateProposalRequest:JavaSDK".getBytes(UTF_8));
        tm.put("method", "InstantiateProposalRequest".getBytes(UTF_8));
        tm.put("method", "InstantiateProposalRequest".getBytes(UTF_8));
        instantiate.setTransientMap(tm);

        out("Sending instantiateProposalRequest to all peers...");

        try {
            responses = channel.sendInstantiationProposal(instantiate);
            out(responses.iterator().next().getProposalResponse().getResponse().getPayload().toString());
        }
        catch(Exception e){
            e.printStackTrace();
        }
    }

    public static void chaincodeInstantiate(HFClient client, ChaincodeEndorsementPolicy... endorsementPolicies) throws Exception{

        ChaincodeID chaincodeID = ChaincodeID.newBuilder().setName("cc").setVersion(currentVersion).build();
        Channel channel = client.getChannel("mychannel");

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

        out("Sending instantiateProposalRequest to all peers...");

        responses = channel.sendInstantiationProposal(instantiate);

        for (ProposalResponse response : responses) {
            if (response.isVerified() && response.getStatus() == ProposalResponse.Status.SUCCESS) {
                successful.add(response);
                out(format("Succesful instantiate proposal response Txid: %s from peer %s", response.getTransactionID(), response.getPeer().getName()));
            } else {
                failed.add(response);
            }
        }
        out(format("Received %d instantiate proposal responses. Successful+verified: %d . Failed: %d", responses.size(), successful.size(), failed.size()));
        if (failed.size() > 0) {
            ProposalResponse first = failed.iterator().next();
            out("Not enough endorsers for instantiate :" + successful.size() + "endorser failed with " + first.getMessage() + ". Was verified:" + first.isVerified());
        }

        ///////////////
        /// Send instantiate transaction to orderer
        out("Sending instantiateTransaction to orderer...");
        CompletableFuture<BlockEvent.TransactionEvent> future = channel.sendTransaction(successful, channel.getOrderers());

        out("calling get...");
        BlockEvent.TransactionEvent event = future.get(100, TimeUnit.SECONDS);
        out("get done...");


        out(format("Finished instantiate transaction with transaction id %s", event.getTransactionID()));


    }

    private static Channel createChannel(HFClient client, HFCAClient caClient, String name) throws Exception {

        System.out.println("Creating new channel named " + name);


        //User peerAdmin = getAdminUser("org1.initial.com", caClient);

        AppUser peerAdmin = getAdminUser("Org1", caClient);
        client.setUserContext(peerAdmin);

        /*
        Peer peer = client.newPeer("peer0", "grpc://localhost:7051");
        //Peer peerN = client.newPeer("peer1.org1.initial.com", "grpc://localhost:7051");


        //EventHub eh = client.newEventHub("eventhub", "grpc://localhost:7053");

        //peer1 = client.newPeer("peer1.org1.initial.com", "grpc://localhost:8051");
        Orderer ord = client.newOrderer("orderer", "grpc://localhost:7050");
        client.newChannel("mychannel");
        Channel channel = client.getChannel("mychannel");


        */

        //initializeNodes(client);

        Orderer orderer = client.newOrderer("orderer0.initial.com", "grpcs://localhost:7050", clientTLSProperties.get("admin"));

        ChannelConfiguration channelConfiguration = new ChannelConfiguration(new File("/home/jorge/Documents/definit/channel-artifacts/channel.tx"));
        Channel newChannel = client.newChannel(name, orderer, channelConfiguration, client.getChannelConfigurationSignature(channelConfiguration, peerAdmin));

        Peer peer = client.newPeer("peer0.org1.initial.com", "grpc://localhost:7051", clientTLSProperties.get("admin"));
        newChannel.joinPeer(peer, createPeerOptions());
        /*Channel channel = client.newChannel("mychannel");
        channel.addPeer(peer0);
        channel.addEventHub(eventhub);
        channel.addOrderer(orderer0);*/
        newChannel.initialize();

        return newChannel;

    }

    /*private static User getAdminUserFile(String orgName, HFCAClient caClient) throws Exception {

        NetworkConfig.UserInfo userInfo = nc.getPeerAdmin(orgName);

        String userName = userInfo.getEnrollId();
        String mspId = userInfo.getMspId();

        PrivateKey privateKey = userInfo.getPrivateKey();
        String signedCert = userInfo.getSignedCert();

        User admin = new AppUser("admin",orgName, mspId, caClient.enroll("admin", "adminpw"), caClient);

        return admin;
    }*/

    private static AppUser getAdminUser(String orgName, HFCAClient caClient) throws Exception{

        //nc = NetworkConfig.fromYamlFile(getNetworkConfigFileYAML());

        //System.out.println("np");

        //exit(0);

        final EnrollmentRequest enrollmentRequestTLS = new EnrollmentRequest();
        enrollmentRequestTLS.addHost("localhost");
        enrollmentRequestTLS.setProfile("tls");

        Enrollment enroll = caClient.enroll("admin", "adminpw", enrollmentRequestTLS);

        final Properties tlsProperties = new Properties();

        AppUser admin = new AppUser("admin", "org1",  "Org1MSP", enroll , caClient);
        serialize(admin);

        //System.out.println("Cert: " + admin.getCert().toString() + "\nPEM: " + admin.getPEMStringPrivKey());

        //exit(0);

        tlsProperties.put("clientKeyBytes", admin.getPEMStringPrivKey().getBytes(UTF_8));
        tlsProperties.put("clientCertBytes", admin.getCert().getBytes(UTF_8));

        clientTLSProperties.put("admin", tlsProperties);

        return admin;
    }

    public static File getNetworkConfigFileYAML() {
        String fname = "network-config-tls.yaml";
        String pname = "/home/jorge/Documents/initial2/src/main/resources/";
        System.out.println(pname + fname);
        File ret = new File(pname, fname);
        out(ret.isFile() + "");

        return ret;
    }

    public static ChaincodeEndorsementPolicy getEndorsementPolicyFileYAML() throws Exception{
        ChaincodeEndorsementPolicy chaincodeEndorsementPolicy = new ChaincodeEndorsementPolicy();

        String fname = "chaincodeendorsementpolicy.yaml";
        String pname = "/home/jorge/Documents/initial2/src/main/resources/";
        System.out.println(pname + fname);
        File ret = new File(pname, fname);

        chaincodeEndorsementPolicy.fromYamlFile(ret);
        return chaincodeEndorsementPolicy;
    }


    public static void initializeNodes(HFClient client) throws Exception{
        peer0 = client.newPeer("peer0.org1.initial.com", "grpc://localhost:7051");
        eventhub = client.newEventHub("eventhub", "grpc://localhost:7053");
        peer1 = client.newPeer("peer1.org1.initial.com", "grpc://localhost:8051");
        orderer0 = client.newOrderer("orderer0.initial.com", "grpc://localhost:7050");
    }

    private static void serialize(AppUser appUser) throws IOException {
        try (ObjectOutputStream oos = new ObjectOutputStream(Files.newOutputStream(Paths.get(appUser.getName() + ".jso")))) {

            oos.writeObject(appUser);
        }
    }

    static AppUser tryDeserialize(String name) throws Exception {
        if (Files.exists(Paths.get(name + ".jso"))) {
            return deserialize(name);
        }
        return null;
    }

    static AppUser deserialize(String name) throws Exception {
        try (ObjectInputStream decoder = new ObjectInputStream(
                Files.newInputStream(Paths.get(name + ".jso")))) {
            return (AppUser) decoder.readObject();
        }
    }

    //static String queryEmp(HFClient client, String arg) throws ProposalException, InvalidArgumentException {
    static String queryEmp(HFClient client, String arg) throws Exception {
        // get channel instance from client
        Channel channel = client.getChannel("mychannel");
        // create chaincode request
        QueryByChaincodeRequest qpr = client.newQueryProposalRequest();
        // build cc id providing the chaincode name. Version is omitted here.
        ChaincodeID fabcarCCId = ChaincodeID.newBuilder().setName("cc").setVersion(currentVersion).build();
        qpr.setChaincodeID(fabcarCCId);
        // CC function to be called
        qpr.setFcn("queryEmp");
        //qpr.setArgs();
        Collection<ProposalResponse> res = channel.queryByChaincode(qpr);
        // display response
        String payload = "nothing";
        for (ProposalResponse pres : res) {

            try {
                if (!pres.getProposalResponse().getResponse().getPayload().toString().equals("")) {
                    payload = pres.getProposalResponse().getResponse().getPayload().toStringUtf8();
                    out("ai: " + pres.getStatus());
                } else {
                    out("Failed to query.");
                }
            }
            catch(Exception e){}

        }

        return payload;

    }

    static void newEmp(HFClient client, String s1, String s2, String s3, String s4) throws Exception {// get channel instance from client

        Collection<ProposalResponse> successful = new LinkedList<>();
        Collection<ProposalResponse> failed = new LinkedList<>();

        Channel channel = client.getChannel("mychannel");
        TransactionProposalRequest transactionRequest = client.newTransactionProposalRequest();

        // build cc id providing the chaincode name. Version is omitted here.
        ChaincodeID fabcarCCId = ChaincodeID.newBuilder().setName("cc").setVersion(currentVersion).build();
        transactionRequest.setChaincodeID(fabcarCCId);
        // CC function to be called
        transactionRequest.setFcn("newEmp");
        transactionRequest.setArgs("newEmp", s1, s2, s3, s4);
        out("sending transaction proposal to all peers with arguments: newEmp");

        Collection<ProposalResponse> res = channel.sendTransactionProposal(transactionRequest);
        // display response
        for (ProposalResponse response : res) {
            if (response.getStatus() == ChaincodeResponse.Status.SUCCESS) {
                out("Successful transaction proposal response Txid: %s from peer %s", response.getTransactionID(), response.getPeer().getName());
                successful.add(response);
            } else {
                failed.add(response);
            }
        }

        out("Received %d transaction proposal responses. Successful+verified: %d . Failed: %d",
                res.size(), successful.size(), failed.size());
        if (failed.size() > 0) {
            ProposalResponse firstTransactionProposalResponse = failed.iterator().next();

            throw new ProposalException(format("Not enough endorsers for newEmp:%d endorser error:%s. Was verified:%b",
                     firstTransactionProposalResponse.getStatus().getStatus(), firstTransactionProposalResponse.getMessage(), firstTransactionProposalResponse.isVerified()));
        }
        out("Successfully received transaction proposal responses.");

        out("Transaction to orderer!");

        CompletableFuture<BlockEvent.TransactionEvent> transactionEvents =  channel.sendTransaction(successful);



    }


}
