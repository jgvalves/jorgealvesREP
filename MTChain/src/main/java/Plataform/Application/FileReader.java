package Plataform.Application;

import org.hyperledger.fabric.sdk.ChaincodeEndorsementPolicy;
import org.hyperledger.fabric.sdk.NetworkConfig;

import java.io.File;
import java.io.IOException;

public class FileReader {

    private FileReader(){

    }


    /**
     *
     * @param path, size=none goes for default, size=1 is path/file type, size=2
     * is path[0]=path, path[1]=file
     * @default gets "chaincodeendorsementpolicy.yaml" from resources folder
     * @return a ChaincodeEndorsementPolicy type that contains the endorsement policy
     * @throws Exception
     */
    public static ChaincodeEndorsementPolicy getEndorsementPolicyFileYAML(String... path) throws Exception{

        ChaincodeEndorsementPolicy chaincodeEndorsementPolicy = new ChaincodeEndorsementPolicy();
        File ret = null;

        switch(path.length){
            case 0:
                String fname = "chaincodeendorsementpolicy.yaml";
                String pname = "/home/jorge/Documents/MTChain/src/main/resources/";
                ret = new File(pname, fname);
                break;
            case 1:
                ret = new File(path[0]);
                break;
            case 2:
                String fileName = path[1];
                String filePath = path[0];
                ret = new File(filePath, fileName);
                break;
        }

        try {
            chaincodeEndorsementPolicy.fromYamlFile(ret);
        }
        catch(IOException e){
            Logs.write("Error reading Endorsement from file.");
        }

        Logs.write("Reading from file" + ret.getAbsolutePath() + ret.getAbsoluteFile());
        return chaincodeEndorsementPolicy;
    }



    /**
     *
     * @param path, size=none goes for default, size=1 is path/file type, size=2
     * is path[0]=path, path[1]=file
     * @default gets "network-config-tls.yaml" from resources folder
     * @return a NetworkConfig type that contains the configuration of the network (peers, orderers, ...)
     * @throws Exception
     */
    public static NetworkConfig getNetworkConfigFileYAML(String... path) throws Exception{

        NetworkConfig networkConfig = null;
        File ret = null;

        switch(path.length){
            case 0:
                String fname = "network-config-tls.yaml";
                String pname = "/home/jorge/Documents/MTChain/src/main/resources/";
                ret = new File(pname, fname);
                break;
            case 1:
                ret = new File(path[0]);
                break;
            case 2:
                String fileName = path[1];
                String filePath = path[0];
                ret = new File(filePath, fileName);
                break;
        }

        Logs.write("Reading from file" + ret.getAbsolutePath() + ret.getAbsoluteFile());

        try {
            networkConfig = NetworkConfig.fromYamlFile(ret);
        }
        catch(IOException e){
            Logs.write("Error reading Network configuration file.");
        }

        Logs.write("Success reading file!");

        Logs.writeInLine(true,"Fabric CA: { ");
        for(NetworkConfig.OrgInfo org: networkConfig.getOrganizationInfos()){
            int i = 0;
            for(NetworkConfig.CAInfo ca: org.getCertificateAuthorities()){
                i++;
                Logs.writeInLine(false, ca.getName() +  " : " + ca.getUrl());
                if(org.getCertificateAuthorities().size() == i){
                    Logs.writeInLine(false,"}");
                }
                else{ Logs.writeInLine(false," , ");}
            }
        }

        Logs.writeInLine(false,"\n");




        for(NetworkConfig.OrgInfo org: networkConfig.getOrganizationInfos()){
            Logs.write(org.getName() + ":");
            Logs.write("MSPID: " + org.getMspId());
            Logs.writeInLine(true, "Peers: { ");

            int i = 0;
            for(String s: org.getPeerNames()){
                i++;
                Logs.writeInLine(false, s);
                if(org.getPeerNames().size() == i){
                    Logs.writeInLine(false,"}");
                }
                else{ Logs.writeInLine(false," , ");}
            }
            Logs.writeInLine(false,"\n");
            Logs.write("Peer Admin: " + org.getPeerAdmin().getName());
        }

        return networkConfig;
    }


}
