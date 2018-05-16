package Plataform.Application;

import org.hyperledger.fabric.sdk.ChaincodeEndorsementPolicy;
import org.hyperledger.fabric.sdk.Channel;
import org.hyperledger.fabric.sdk.HFClient;

public interface Platform {

    public int upgradeChaincode(HFClient client, Channel channel, String chaincodeName, ChaincodeEndorsementPolicy... endorsementPolicies) throws Exception;
    public int instatiateChaincode(HFClient client, Channel channel, String chaincodeName, ChaincodeEndorsementPolicy... endorsementPolicies) throws Exception;
    public String op_query(HFClient client, Channel channel, String chaincodeName, byte[] documentName) throws Exception;

}
