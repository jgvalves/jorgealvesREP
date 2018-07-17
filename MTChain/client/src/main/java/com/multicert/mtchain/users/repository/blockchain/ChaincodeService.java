package com.multicert.mtchain.users.repository.blockchain;

import com.multicert.mtchain.core.Blockchain;
import com.multicert.mtchain.users.repository.app.AppRepository;
import com.multicert.mtchain.users.repository.blockchain.Model.Chaincodes;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.NoSuchElementException;

@Service("ChaincodeService")
public class ChaincodeService {

    @Autowired
    private ChaincodeRepository chaincodeRepository;

    @Autowired
    private AppRepository appRepository;

    @Transactional
    public Blockchain getChaincodeObject(String appName){

        String chaincodeName = appRepository.findByname(appName).getChaincode();

        for(Chaincodes blockchainInfo: chaincodeRepository.findAll()){
            if(blockchainInfo.getName().equals(chaincodeName)){
                return blockchainInfo.getBlockchain();
            }
        }

        throw new NoSuchElementException("Could not found blockchain instantiation. Request the manager an installation.");
    }
}
