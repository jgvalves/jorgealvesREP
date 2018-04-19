package initial;

import org.hyperledger.fabric.protos.peer.Chaincode;
import org.hyperledger.fabric.sdk.*;
import org.hyperledger.fabric.sdk.security.CryptoSuite;
import org.hyperledger.fabric.sdk.Channel;
import org.hyperledger.fabric.sdk.NetworkConfig;
import org.hyperledger.fabric.sdk.NetworkConfig.UserInfo;
import org.hyperledger.fabric.sdk.NetworkConfig.*;
import org.hyperledger.fabric_ca.sdk.EnrollmentRequest;
import org.hyperledger.fabric_ca.sdk.HFCAClient;
import org.hyperledger.fabric.sdk.TransactionRequest.Type;
import org.apache.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.PrivateKey;
import java.util.*;

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

    public static void main( String[] args )
    {


        try {

            CryptoSuite cs = CryptoSuite.Factory.getCryptoSuite();
            nc = NetworkConfig.fromYamlFile(getNetworkConfigFileYAML());

            NetworkConfig.CAInfo caInfo = nc.getClientOrganization().getCertificateAuthorities().get(0);

            //out(caInfo.getUrl().toString()); exit(0);

            HFCAClient caClient = HFCAClient.createNewInstance(caInfo.getUrl(), caInfo.getHttpOptions());
            caClient.setCryptoSuite(cs);

            //AppUser peerAdmin = getAdminUser("org1", caClient);


            //Peer peer = client.newPeer("peer0", "grpc://localhost:7051");
            //Peer peer10 = client.newPeer("peer1.org1.initial.com", "grpc://localhost:8051");
            //EventHub eh = client.newEventHub("eventhub", "grpc://localhost:7053");
            //Orderer ord = client.newOrderer("orderer", "grpc://localhost:7050");

            HFClient client = HFClient.createNewInstance();
            client.setCryptoSuite(CryptoSuite.Factory.getCryptoSuite());

            //User adminInfo = nc.getPeerAdmin();
            //User user = nc.getPeerAdmin("Org1");


            /*final EnrollmentRequest enrollmentRequestTLS = new EnrollmentRequest();
            enrollmentRequestTLS.addHost("localhost");
            enrollmentRequestTLS.setProfile("tls");
            enrollmentRequestTLS.
            Enrollment enr = caClient.enroll("admin", "adminpw", enrollmentRequestTLS);

            AppUser admin = new AppUser("admin", "Org1", adminInfo.getMspId(), enr, caClient);
            */


            //client.setUserContext(adminInfo);


            Channel channel = client.loadChannelFromConfig("mychannel", nc);
            channel.initialize();

            out(channel.toString());

            //Channel channel = createChannel(client, caClient, "mychannel");

            //Channel channel = client.newChannel("mychannel");
            //channel.addPeer(client.newPeer("peer0.org1.initial.com", "grpc://localhost:7051"));
            //channel.addPeer(peer10);
            //channel.addEventHub(eh);
            //channel.addOrderer(client.newOrderer("orderer", "grpc://localhost:7050"));
            //channel.initialize();

            //ccInstall(client, channel);

            System.out.println("nice!");
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private static User xx(String x) throws Exception{
        return null; //nc.getPeerAdmin(x);
    }

    private static void out(String s){
        System.out.println(s);
    }

    /*private static void ccInstall(HFClient client, Channel channel) throws Exception {

        Collection<ProposalResponse> responses;
        Collection<ProposalResponse> successful = new LinkedList<>();
        Collection<ProposalResponse> failed = new LinkedList<>();


        ChaincodeID.Builder chaincodeIDBuilder = ChaincodeID.newBuilder().setName("cc.go")
                .setVersion("1");
        chaincodeIDBuilder.setPath("resources/");
        ChaincodeID chaincodeID = chaincodeIDBuilder.build();

        InstallProposalRequest installProposalRequest = client.newInstallProposalRequest();
        installProposalRequest.setChaincodeID(chaincodeID);
        installProposalRequest.setChaincodeSourceLocation(Paths.get("").toFile());
        installProposalRequest.setChaincodeVersion("1");
        installProposalRequest.setChaincodeLanguage(Type.GO_LANG);

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
    }*/

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
        String pname = "/home/jorge/Documents/initial/src/resources/";
        System.out.println(pname + fname);
        File ret = new File(pname, fname);
        out(ret.isFile() + "");

        return ret;
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
}
